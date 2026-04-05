package com.TravelMedicineAdvisory.Server.domain.plans;

import com.TravelMedicineAdvisory.Server.core.ai.AiGenerationClient;
import com.TravelMedicineAdvisory.Server.core.ai.AiGenerationResult;
import com.TravelMedicineAdvisory.Server.core.queue.JobType;
import com.TravelMedicineAdvisory.Server.core.queue.QueueService;
import com.TravelMedicineAdvisory.Server.domain.airequestlog.AiRequestLog;
import com.TravelMedicineAdvisory.Server.domain.airequestlog.AiRequestLogRepository;
import com.TravelMedicineAdvisory.Server.domain.plangenerationcontext.PlanGenerationContext;
import com.TravelMedicineAdvisory.Server.domain.plangenerationcontext.PlanGenerationContextService;
import com.TravelMedicineAdvisory.Server.domain.travelplan.TravelPlan;
import com.TravelMedicineAdvisory.Server.domain.travelplan.TravelPlanRepository;
import com.TravelMedicineAdvisory.Server.domain.user.User;
import com.TravelMedicineAdvisory.Server.domain.useronboarding.UserOnboarding;
import com.TravelMedicineAdvisory.Server.domain.useronboarding.UserOnboardingRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * AI generation pipeline for travel health plans created by end users (dashboard / HR flows).
 * Uses {@link UserOnboarding#getResponsesJson()} for traveller health personalisation; trip destination
 * and itinerary always come from the {@link TravelPlan} request (onboarding trip fields are stripped).
 * Admins curate {@link PlanGenerationContext} reference materials for prompts.
 */
@Service
@Transactional
public class PlanGenerationService {

    private static final Logger log = LoggerFactory.getLogger(PlanGenerationService.class);

    private final TravelPlanRepository travelPlanRepository;
    private final GeneratedPlanRepository generatedPlanRepository;
    private final PlanGenerationContextService contextService;
    private final UserOnboardingRepository userOnboardingRepository;
    private final AiGenerationClient aiGenerationClient;
    private final AiRequestLogRepository aiRequestLogRepository;
    private final ObjectMapper objectMapper;
    private final QueueService queueService;

    public PlanGenerationService(
            TravelPlanRepository travelPlanRepository,
            GeneratedPlanRepository generatedPlanRepository,
            PlanGenerationContextService contextService,
            UserOnboardingRepository userOnboardingRepository,
            AiGenerationClient aiGenerationClient,
            AiRequestLogRepository aiRequestLogRepository,
            ObjectMapper objectMapper,
            QueueService queueService
    ) {
        this.travelPlanRepository = travelPlanRepository;
        this.generatedPlanRepository = generatedPlanRepository;
        this.contextService = contextService;
        this.userOnboardingRepository = userOnboardingRepository;
        this.aiGenerationClient = aiGenerationClient;
        this.aiRequestLogRepository = aiRequestLogRepository;
        this.objectMapper = objectMapper;
        this.queueService = queueService;
    }

    public void enqueueGeneration(Long travelPlanId, Long userId) {
        log.info("Travel plan generation requested: travelPlanId={} userId={} (queued for async processing)", travelPlanId, userId);
        queueService.dispatch(JobType.GENERATE_TRAVEL_PLAN, Map.of(
                "travelPlanId", travelPlanId,
                "userId", userId
        ));
    }

    public void processQueuedGeneration(Long travelPlanId) {
        TravelPlan travelPlan = travelPlanRepository.findById(travelPlanId)
                .orElseThrow(() -> new NoSuchElementException("Travel plan not found"));

        GeneratedPlan generatedPlan = generatedPlanRepository.findByTravelPlanId(travelPlanId)
                .orElseGet(GeneratedPlan::new);
        generatedPlan.setTravelPlan(travelPlan);
        generatedPlan.setUser(travelPlan.getUser());
        generatedPlan.setCompany(travelPlan.getCompany());
        generatedPlan.setDestination(travelPlan.getDestination());
        generatedPlan.setDuration(travelPlan.getDuration());
        generatedPlan.setPurpose(travelPlan.getPurpose());
        generatedPlan.setRiskScore(travelPlan.getRiskScore());
        generatedPlan.setStatus("processing");
        generatedPlanRepository.save(generatedPlan);

        travelPlan.setStatus("PROCESSING");
        travelPlanRepository.save(travelPlan);

        Long userId = travelPlan.getUser() != null ? travelPlan.getUser().getId() : null;
        log.info(
                "Travel plan generation in progress: travelPlanId={} userId={} destination=\"{}\" country=\"{}\"",
                travelPlanId,
                userId,
                travelPlan.getDestination(),
                travelPlan.getCountry());

        Instant startedAt = Instant.now();
        AiRequestLog aiLog = buildAiLog(travelPlan);

        try {
            List<PlanGenerationContext> contexts = contextService.findEnabled();
            UserOnboarding onboarding = resolveOnboarding(travelPlan);
            String systemPrompt = buildSystemPrompt();
            String userPrompt = buildUserPrompt(travelPlan, contexts, onboarding);

            log.info(
                    "Calling AI for travelPlanId={} (admin context files={}, onboarding={})",
                    travelPlanId,
                    contexts.size(),
                    onboarding != null);

            AiGenerationResult result = aiGenerationClient.generate(systemPrompt, userPrompt);
            JsonNode structuredOutput = parseJson(result.content());

            long elapsedMs = Duration.between(startedAt, Instant.now()).toMillis();

            generatedPlan.setPlanJson(writeJson(structuredOutput));
            generatedPlan.setStatus("active");
            generatedPlan.setProvider(result.provider());
            generatedPlan.setModelUsed(result.model());
            generatedPlan.setTokensUsed(result.estimatedTokens());
            generatedPlan.setProcessingTimeMs(elapsedMs);
            generatedPlan.setErrorMessage(null);
            generatedPlanRepository.save(generatedPlan);

            travelPlan.setStatus("COMPLETED");
            travelPlan.setMedicalConsiderations(joinArray(structuredOutput, "recommendations"));
            travelPlan.setVaccinations(joinArray(structuredOutput, "vaccinations"));
            travelPlan.setHealthAlerts(joinArray(structuredOutput, "healthRiskOverview"));
            travelPlan.setSafetyAdvisories(joinArray(structuredOutput, "nextSteps"));
            travelPlan.setEmergencyContacts(joinArray(structuredOutput.path("medicalCare"), "emergencyContacts"));
            travelPlanRepository.save(travelPlan);

            aiLog.setStatus("success");
            aiLog.setOutputSummary(compactSummary(structuredOutput));
            aiLog.setTokensUsed(result.estimatedTokens());
            aiLog.setModelUsed(result.model());
            aiLog.setProcessingTimeMs(elapsedMs);
            aiRequestLogRepository.save(aiLog);

            log.info(
                    "Travel plan generation completed: travelPlanId={} provider={} model={} tokens≈{} durationMs={}",
                    travelPlanId,
                    result.provider(),
                    result.model(),
                    result.estimatedTokens(),
                    elapsedMs);

            sendReadyEmail(travelPlan);
        } catch (Exception ex) {
            long elapsedMs = Duration.between(startedAt, Instant.now()).toMillis();

            log.error(
                    "Travel plan generation failed: travelPlanId={} userId={} destination=\"{}\" afterMs={} — {}",
                    travelPlanId,
                    userId,
                    travelPlan.getDestination(),
                    elapsedMs,
                    ex.getMessage(),
                    ex);

            generatedPlan.setStatus("failed");
            generatedPlan.setErrorMessage(ex.getMessage());
            generatedPlan.setProcessingTimeMs(elapsedMs);
            generatedPlanRepository.save(generatedPlan);

            travelPlan.setStatus("FAILED");
            travelPlanRepository.save(travelPlan);

            aiLog.setStatus("error");
            aiLog.setErrorMessage(ex.getMessage());
            aiLog.setProcessingTimeMs(elapsedMs);
            aiRequestLogRepository.save(aiLog);

            throw new RuntimeException("Travel plan generation failed", ex);
        }
    }

    private AiRequestLog buildAiLog(TravelPlan travelPlan) {
        AiRequestLog log = new AiRequestLog();
        log.setDestination(travelPlan.getDestination());
        log.setPromptSummary("Generate structured travel health report (current trip + onboarding health JSON)");
        log.setRiskLevel((travelPlan.getRiskScore() != null && travelPlan.getRiskScore() >= 60) ? "high" : "medium");
        log.setCreditConsumed(BigDecimal.ONE);
        log.setCompany(travelPlan.getCompany());
        log.setUser(travelPlan.getUser());
        return log;
    }

    private void sendReadyEmail(TravelPlan travelPlan) {
        User user = travelPlan.getUser();
        if (user == null || user.getEmail() == null) {
            return;
        }
        String firstName = user.getFirstName() != null ? user.getFirstName() : "there";
        String companyName = travelPlan.getCompany() != null ? travelPlan.getCompany().getName() : "TMAG";

        queueService.dispatch(JobType.EMAIL_TRAVEL_PLAN_CREATED, Map.of(
                "to", user.getEmail(),
                "subject", "Your travel health plan for " + travelPlan.getDestination() + " is ready",
                "variables", Map.of(
                        "firstName", firstName,
                        "destination", travelPlan.getDestination(),
                        "companyName", companyName
                )
        ));
    }

    private String buildSystemPrompt() {
        return """
                You are TMAG's travel medicine advisory engine.
                Return ONLY valid JSON matching this exact schema:
                {
                  "reportTitle": "string",
                  "travellerName": "string",
                  "destination": "string",
                  "travelDates": "string",
                  "tripAtGlance": {
                    "durationDays": number,
                    "purpose": "string",
                    "travelling": "string",
                    "accommodation": "string",
                    "insurance": "string"
                  },
                  "healthRiskOverview": [
                    {"category":"string","level":"LOW|MODERATE|HIGH","summary":"string"}
                  ],
                  "vaccinations": [
                    {"vaccine":"string","status":"string","recommendation":"string","action":"string"}
                  ],
                  "recommendations": [
                    {"title":"string","details":"string"}
                  ],
                  "afterReturn": {
                    "within1Week":["string"],
                    "within4Weeks":["string"],
                    "beyond4Weeks":["string"],
                    "redFlag":"string"
                  },
                  "medicalCare": {
                    "clinics":[{"name":"string","address":"string","phone":"string","distance":"string","notes":"string"}],
                    "embassyContacts":[{"name":"string","details":"string"}],
                    "emergencyContacts":[{"label":"string","value":"string"}]
                  },
                  "itineraryGuidance": {
                    "tripType":"ONE_WAY|RETURN|MULTI_STOP",
                    "summary":"string",
                    "routeAdvice":[{"stop":"string","country":"string","guidance":"string"}],
                    "returnGuidance":["string"]
                  },
                  "nextSteps": ["string"],
                  "medicalDisclaimer":"string"
                }
                The user prompt has a "Current trip" section: that is the ONLY authoritative source for
                destination, country, trip length, purpose, and travel dates in your JSON output.
                For RETURN trips, when Departure date and Return date appear in that section, set travelDates
                to a clear human-readable range using those exact dates, and set tripAtGlance.durationDays
                to the inclusive calendar day count (must match "Trip length" in the user prompt).
                A separate "Traveller health context" block contains onboarding questionnaire JSON for
                medical personalisation only; never infer this trip's destination or itinerary from it
                (server removes trip_itinerary from that JSON; still treat the Current trip block as sole truth).
                Use concise, practical medical-travel guidance. Do not include markdown fences.

                COVERAGE (mandatory — do not omit categories to save space):
                1) healthRiskOverview: Include EXACTLY one object per category below, in this order, using these
                exact category strings. Every category must appear even if risk is minimal — use level LOW and a
                short summary (1–3 sentences) explaining low concern, "not applicable to this itinerary", or routine
                baseline. Only use MODERATE or HIGH when truly warranted.
                - "Food and water safety"
                - "Vector-borne diseases"
                - "Respiratory infections"
                - "Environmental health (heat, sun, air quality)"
                - "Injuries and road traffic safety"
                - "Rabies and animal contact"
                - "Blood-borne and sexual health"
                - "Altitude-related illness"

                2) vaccinations: Include one object per vaccine topic below (same vaccine string labels where
                possible). Do not skip a topic because it seems unnecessary — for low-relevance or not-indicated
                vaccines, set status to e.g. "Low priority — not destination-specific" or "Not routinely indicated
                for this itinerary", recommendation to a brief evidence-based line, and action to routine follow-up
                (e.g. confirm records with GP) or "None specific". Topics (in order):
                - "Routine immunizations (e.g. MMR, varicella, dTdap, polio/IPV)"
                - "Influenza"
                - "COVID-19"
                - "Hepatitis A"
                - "Hepatitis B"
                - "Typhoid"
                - "Yellow fever"
                - "Japanese encephalitis"
                - "Meningococcal"
                - "Rabies pre-exposure"
                - "Cholera (oral vaccine)"

                3) recommendations: Include at least one object per topic below. For areas of low relevance to this
                trip, keep the title and use details to state clearly that risk is low or the topic is standard
                baseline care — still include the row. Order as listed; tailor details to destination and traveller
                context.
                - "Pre-travel review & vaccination records"
                - "Food and water hygiene"
                - "Vector bite prevention"
                - "Sun, heat, and environmental precautions"
                - "Injury and road safety"
                - "Sexual health and blood exposure"
                - "Jet lag, sleep, and mental wellbeing"
                - "Malaria and other chemoprophylaxis (state if not indicated)"
                - "Traveller-specific considerations (from health context)"

                4) itineraryGuidance (mandatory):
                - tripType must exactly mirror the Current trip "Trip Type" value.
                - routeAdvice:
                  - ONE_WAY: include exactly one stop guidance row.
                  - RETURN: include outbound stop guidance and include practical return-phase reminders in returnGuidance.
                  - MULTI_STOP: include one guidance row per stop from Trip Stops JSON, in listed order.
                - returnGuidance:
                  - RETURN: include at least 4 actionable bullets for the return leg and immediate post-return period.
                  - ONE_WAY or MULTI_STOP: returnGuidance may be an empty array unless there is explicit return information.
                """;
    }

    private UserOnboarding resolveOnboarding(TravelPlan travelPlan) {
        User user = travelPlan.getUser();
        if (user == null || user.getId() == null) {
            return null;
        }
        return userOnboardingRepository.findByUser_Id(user.getId()).orElse(null);
    }

    private String buildUserPrompt(
            TravelPlan travelPlan,
            List<PlanGenerationContext> contexts,
            UserOnboarding onboarding
    ) {
        StringBuilder builder = new StringBuilder();
        builder.append("Generate a travel health report for this trip.\n\n");

        builder.append("### Current trip (authoritative — use ONLY this section for destination, country, ")
                .append("trip duration, purpose, and dates in the report JSON)\n");
        builder.append("Destination: ").append(nullSafe(travelPlan.getDestination())).append("\n");
        builder.append("Country: ").append(nullSafe(travelPlan.getCountry())).append("\n");
        builder.append("Duration Days: ").append(travelPlan.getDuration() != null ? travelPlan.getDuration() : 0).append("\n");
        builder.append("Purpose: ").append(nullSafe(travelPlan.getPurpose())).append("\n");
        builder.append("Trip Type: ").append(resolveTripType(travelPlan.getTripType())).append("\n");
        appendReturnScheduleLines(travelPlan, builder);
        builder.append("Trip Stops JSON: ").append(extractTripStopsJson(travelPlan)).append("\n");
        builder.append("Risk Score (app): ").append(travelPlan.getRiskScore() != null ? travelPlan.getRiskScore() : 0).append("\n");
        builder.append("Traveller display name: ").append(resolveTravellerName(travelPlan.getUser())).append("\n");
        if (StringUtils.hasText(travelPlan.getMedicalConsiderations())) {
            builder.append("User notes for this trip: ").append(travelPlan.getMedicalConsiderations().trim()).append("\n");
        }
        builder.append("\n");

        builder.append("### Traveller health context (from UserOnboarding questionnaire — personalisation only)\n");
        builder.append("Use for vaccines, conditions, allergies, prior travel immunity, activities preferences, etc.\n");
        builder.append("Do NOT use this block for this trip's destination, cities, or travel dates; the Current trip ")
                .append("section above always wins.\n");
        if (onboarding == null) {
            builder.append("(No onboarding row linked to this user.)\n\n");
        } else {
            if (StringUtils.hasText(onboarding.getNationality())) {
                builder.append("Nationality: ").append(onboarding.getNationality().trim()).append("\n");
            }
            if (StringUtils.hasText(onboarding.getUserType())) {
                builder.append("Onboarding user type: ").append(onboarding.getUserType().trim()).append("\n");
            }
            String sanitized = OnboardingResponsesSanitizer.stripItineraryFields(onboarding.getResponsesJson(), objectMapper);
            boolean hasResponses = StringUtils.hasText(sanitized) && !"{}".equals(sanitized);
            if (hasResponses) {
                builder.append("Questionnaire responses JSON (trip_itinerary removed server-side):\n")
                        .append(sanitized)
                        .append("\n");
            } else if (Boolean.TRUE.equals(onboarding.getQuestionnaireCompleted())) {
                builder.append("(Questionnaire marked complete but no usable responses JSON.)\n");
            } else {
                builder.append("(Questionnaire not completed or responses empty.)\n");
            }
            builder.append("\n");
        }

        if (!contexts.isEmpty()) {
            builder.append("### Platform reference materials (admin-curated)\n");
            builder.append("Use when relevant alongside the traveller health context:\n");
            for (PlanGenerationContext context : contexts) {
                builder.append("- ").append(context.getTitle()).append(":\n")
                        .append(trimContext(context.getSynthesizedText())).append("\n\n");
            }
        }
        return builder.toString();
    }

    private String resolveTripType(String rawTripType) {
        if (!StringUtils.hasText(rawTripType)) {
            return "ONE_WAY";
        }
        String normalized = rawTripType.trim().toLowerCase();
        return switch (normalized) {
            case "return", "round-trip", "round_trip", "roundtrip" -> "RETURN";
            case "multi", "multi-stop", "multi_stop", "multistop" -> "MULTI_STOP";
            default -> "ONE_WAY";
        };
    }

    private void appendReturnScheduleLines(TravelPlan travelPlan, StringBuilder builder) {
        if (!"RETURN".equals(resolveTripType(travelPlan.getTripType()))) {
            return;
        }
        if (!StringUtils.hasText(travelPlan.getTripDetailsJson())) {
            return;
        }
        try {
            JsonNode root = objectMapper.readTree(travelPlan.getTripDetailsJson());
            String dep = root.path("departureDate").isTextual() ? root.get("departureDate").asText().trim() : "";
            String ret = root.path("returnDate").isTextual() ? root.get("returnDate").asText().trim() : "";
            if (StringUtils.hasText(dep)) {
                builder.append("Departure date (outbound): ").append(dep).append("\n");
            }
            if (StringUtils.hasText(ret)) {
                builder.append("Return date: ").append(ret).append("\n");
            }
            if (travelPlan.getDuration() != null) {
                builder.append("Trip length (inclusive calendar days): ")
                        .append(travelPlan.getDuration())
                        .append("\n");
            }
        } catch (Exception ignored) {
            // Malformed JSON — stops line still carries itinerary; omit schedule lines.
        }
    }

    private String extractTripStopsJson(TravelPlan travelPlan) {
        if (StringUtils.hasText(travelPlan.getTripDetailsJson())) {
            try {
                JsonNode root = objectMapper.readTree(travelPlan.getTripDetailsJson());
                JsonNode stops = root.path("stops");
                if (stops.isArray() && !stops.isEmpty()) {
                    return objectMapper.writeValueAsString(stops);
                }
            } catch (Exception ignored) {
                // Fall back to a single stop when malformed.
            }
        }

        List<Map<String, String>> fallbackStops = new ArrayList<>();
        fallbackStops.add(Map.of(
                "city", nullSafe(travelPlan.getDestination()),
                "country", nullSafe(travelPlan.getCountry())));
        try {
            return objectMapper.writeValueAsString(fallbackStops);
        } catch (JsonProcessingException ex) {
            return "[]";
        }
    }

    private JsonNode parseJson(String content) {
        String normalized = content.trim();
        if (normalized.startsWith("```")) {
            normalized = normalized.replaceFirst("^```(?:json)?\\s*", "").replaceFirst("\\s*```$", "");
        }
        int firstBrace = normalized.indexOf("{");
        int lastBrace = normalized.lastIndexOf("}");
        if (firstBrace >= 0 && lastBrace > firstBrace) {
            normalized = normalized.substring(firstBrace, lastBrace + 1);
        }
        try {
            return objectMapper.readTree(normalized);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("AI returned invalid JSON output", ex);
        }
    }

    private String writeJson(JsonNode jsonNode) {
        try {
            return objectMapper.writeValueAsString(jsonNode);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to store generated JSON", ex);
        }
    }

    private String compactSummary(JsonNode jsonNode) {
        String destination = jsonNode.path("destination").asText("");
        int recommendationCount = jsonNode.path("recommendations").isArray()
                ? jsonNode.path("recommendations").size() : 0;
        return "Generated structured plan for " + destination + " with " + recommendationCount + " recommendations";
    }

    private String joinArray(JsonNode root, String fieldName) {
        JsonNode arr = root.path(fieldName);
        if (!arr.isArray()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (JsonNode item : arr) {
            if (item.isTextual()) {
                appendWithComma(builder, item.asText(""));
            } else if (item.has("title")) {
                appendWithComma(builder, item.path("title").asText(""));
            } else if (item.has("category")) {
                appendWithComma(builder, item.path("category").asText("") + ": " + item.path("summary").asText(""));
            } else if (item.has("label")) {
                appendWithComma(builder, item.path("label").asText("") + " " + item.path("value").asText(""));
            } else if (item.has("vaccine")) {
                appendWithComma(builder, item.path("vaccine").asText(""));
            }
        }
        return builder.toString();
    }

    private void appendWithComma(StringBuilder builder, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        if (!builder.isEmpty()) {
            builder.append(", ");
        }
        builder.append(value.trim());
    }

    private String resolveTravellerName(User user) {
        if (user == null) {
            return "Traveller";
        }
        if (user.getName() != null && !user.getName().isBlank()) {
            return user.getName();
        }
        String fullName = ((user.getFirstName() != null ? user.getFirstName() : "") + " "
                + (user.getLastName() != null ? user.getLastName() : "")).trim();
        if (!fullName.isBlank()) {
            return fullName;
        }
        return user.getEmail() != null ? user.getEmail() : "Traveller";
    }

    private String trimContext(String text) {
        if (text == null) {
            return "";
        }
        if (text.length() <= 2000) {
            return text;
        }
        return text.substring(0, 2000) + "...";
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }
}
