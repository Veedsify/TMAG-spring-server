package com.TravelMedicineAdvisory.Server.domain.plans;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.TravelMedicineAdvisory.Server.core.ai.AiCallOptions;
import com.TravelMedicineAdvisory.Server.core.ai.StructuredAiResult;
import com.TravelMedicineAdvisory.Server.core.ai.StructuredAiRouter;
import com.TravelMedicineAdvisory.Server.core.queue.JobType;
import com.TravelMedicineAdvisory.Server.core.queue.QueueService;
import com.TravelMedicineAdvisory.Server.core.storage.StorageService;
import com.TravelMedicineAdvisory.Server.core.websocket.DoctorWebSocketService;
import com.TravelMedicineAdvisory.Server.domain.airequestlog.AiRequestLog;
import com.TravelMedicineAdvisory.Server.domain.airequestlog.AiRequestLogRepository;
import com.TravelMedicineAdvisory.Server.domain.doctor.DoctorValidationStatus;
import com.TravelMedicineAdvisory.Server.domain.doctor.TravelPlanDoctorAssignment;
import com.TravelMedicineAdvisory.Server.domain.doctor.TravelPlanDoctorAssignmentRepository;
import com.TravelMedicineAdvisory.Server.domain.familytrip.FamilyTripMemberRepository;
import com.TravelMedicineAdvisory.Server.domain.plangenerationcontext.PlanGenerationContext;
import com.TravelMedicineAdvisory.Server.domain.plangenerationcontext.PlanGenerationContextService;
import com.TravelMedicineAdvisory.Server.domain.role.Roles;
import com.TravelMedicineAdvisory.Server.domain.travelplan.TravelPlan;
import com.TravelMedicineAdvisory.Server.domain.travelplan.TravelPlanRepository;
import com.TravelMedicineAdvisory.Server.domain.travelplan.TravelPlanSummaryPdfGenerator;
import com.TravelMedicineAdvisory.Server.domain.travelplanquestionnaire.TravelPlanQuestionnaire;
import com.TravelMedicineAdvisory.Server.domain.travelplanquestionnaire.TravelPlanQuestionnaireRepository;
import com.TravelMedicineAdvisory.Server.domain.user.User;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;
import com.TravelMedicineAdvisory.Server.domain.useronboarding.UserOnboarding;
import com.TravelMedicineAdvisory.Server.domain.useronboarding.UserOnboardingRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * AI generation pipeline for travel health plans created by end users
 * (dashboard / HR flows).
 * For a user's first active travel plan, traveller health context comes from
 * {@link UserOnboarding#getResponsesJson()} (onboarding questionnaire). For
 * later plans, context comes from
 * {@link com.TravelMedicineAdvisory.Server.domain.travelplanquestionnaire.TravelPlanQuestionnaire}
 * when present,
 * with onboarding as fallback. Trip destination and itinerary always come from
 * the {@link TravelPlan} request
 * (onboarding trip fields are stripped). Admins curate
 * {@link PlanGenerationContext} reference materials for prompts.
 */
@Service
@Transactional
public class PlanGenerationService {

    private static final Logger log = LoggerFactory.getLogger(PlanGenerationService.class);

    private final TravelPlanRepository travelPlanRepository;
    private final GeneratedPlanRepository generatedPlanRepository;
    private final PlanGenerationContextService contextService;
    private final UserOnboardingRepository userOnboardingRepository;
    private final TravelPlanQuestionnaireRepository travelPlanQuestionnaireRepository;
    private final StructuredAiRouter structuredAiRouter;
    private final AiRequestLogRepository aiRequestLogRepository;
    private final ObjectMapper objectMapper;
    private final QueueService queueService;
    private final DoctorWebSocketService doctorWebSocketService;
    private final UserRepository userRepository;
    private final ClinicalContextExtractor clinicalContextExtractor;
    private final SystemPromptBuilder systemPromptBuilder;
    private final TravelPlanSummaryPdfGenerator travelPlanSummaryPdfGenerator;
    private final StorageService storageService;
    private final TravelPlanDoctorAssignmentRepository assignmentRepository;
    private final FamilyTripMemberRepository familyTripMemberRepository;

    public PlanGenerationService(
            TravelPlanRepository travelPlanRepository,
            GeneratedPlanRepository generatedPlanRepository,
            PlanGenerationContextService contextService,
            UserOnboardingRepository userOnboardingRepository,
            TravelPlanQuestionnaireRepository travelPlanQuestionnaireRepository,
            StructuredAiRouter structuredAiRouter,
            AiRequestLogRepository aiRequestLogRepository,
            ObjectMapper objectMapper,
            QueueService queueService,
            DoctorWebSocketService doctorWebSocketService,
            UserRepository userRepository,
            ClinicalContextExtractor clinicalContextExtractor,
            SystemPromptBuilder systemPromptBuilder,
            TravelPlanSummaryPdfGenerator travelPlanSummaryPdfGenerator,
            StorageService storageService,
            TravelPlanDoctorAssignmentRepository assignmentRepository,
            FamilyTripMemberRepository familyTripMemberRepository) {
        this.travelPlanRepository = travelPlanRepository;
        this.generatedPlanRepository = generatedPlanRepository;
        this.contextService = contextService;
        this.userOnboardingRepository = userOnboardingRepository;
        this.travelPlanQuestionnaireRepository = travelPlanQuestionnaireRepository;
        this.structuredAiRouter = structuredAiRouter;
        this.aiRequestLogRepository = aiRequestLogRepository;
        this.objectMapper = objectMapper;
        this.queueService = queueService;
        this.doctorWebSocketService = doctorWebSocketService;
        this.userRepository = userRepository;
        this.clinicalContextExtractor = clinicalContextExtractor;
        this.systemPromptBuilder = systemPromptBuilder;
        this.travelPlanSummaryPdfGenerator = travelPlanSummaryPdfGenerator;
        this.storageService = storageService;
        this.assignmentRepository = assignmentRepository;
        this.familyTripMemberRepository = familyTripMemberRepository;
    }

    public void enqueueGeneration(Long travelPlanId, Long userId) {
        log.info("Travel plan generation requested: travelPlanId={} userId={} (queued for async processing)",
                travelPlanId, userId);
        queueService.dispatch(JobType.GENERATE_TRAVEL_PLAN, Map.of(
                "travelPlanId", travelPlanId,
                "userId", userId));
    }

    @Transactional(noRollbackFor = PlanGenerationException.class)
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

        // Family trip aggregate plan — dispatch to dedicated handler
        if (travelPlan.getFamilyTrip() != null && travelPlan.getFamilyTripMember() == null) {
            processFamilyGeneration(travelPlan, generatedPlan, aiLog, startedAt);
            return;
        }

        try {
            List<PlanGenerationContext> contexts = contextService.findEnabled();
            UserOnboarding onboarding = resolveOnboarding(travelPlan);
            boolean firstTravelPlanForUser = isFirstActiveTravelPlanForUser(travelPlan);
            String travelPlanQuestionnaire = resolveTravelPlanQuestionnaire(travelPlan, firstTravelPlanForUser);

            // Extract clinical context from questionnaire
            String questionnaireJson = resolveQuestionnaireJson(onboarding, travelPlanQuestionnaire);
            ClinicalContext clinicalContext = clinicalContextExtractor.extract(travelPlan, questionnaireJson);

            // Check for hard stop - if triggered, skip AI and return structured hard stop
            // response
            if (clinicalContext.hardStop().triggered()) {
                log.warn("Hard stop triggered for travelPlanId={}: {}", travelPlanId,
                        clinicalContext.hardStop().condition());
                handleHardStop(travelPlan, generatedPlan, aiLog, clinicalContext, startedAt);
                return;
            }

            // Build enriched system prompt with clinical intelligence
            String systemPrompt = systemPromptBuilder.build(clinicalContext);
            String userPrompt = buildUserPrompt(travelPlan, contexts, onboarding, travelPlanQuestionnaire,
                    firstTravelPlanForUser);

            log.info(
                    "Calling AI for travelPlanId={} (admin context files={}, onboarding={}, firstPlanForUser={}, triggeredTrees={}, overallRisk={})",
                    travelPlanId,
                    contexts.size(),
                    onboarding != null,
                    firstTravelPlanForUser,
                    clinicalContext.triggeredTrees().size(),
                    clinicalContext.overallRiskLevel());

            AiCallOptions planOpts = structuredAiRouter.resolvePlanOptions(
                    travelPlan.getPlanTier() != null ? travelPlan.getPlanTier().name() : "FREE");
                    
            boolean isAnthropic = planOpts.providerOverride() != null
                    && planOpts.providerOverride().toLowerCase().contains("anthropic");


            TravelPlanOutputSchemas.TravelHealthPlanOutput structuredValue;
            String planJson;
            String provider;
            String model;
            Integer estimatedTokens;
            // Anthropic derives its grammar from the Java class, not the Gemini Schema;
            // use a genuinely smaller output type to avoid the compiled-grammar limit.
            if (isAnthropic) {
                // Split into two calls to stay under Anthropic grammar size limits
                var coreResult = structuredAiRouter.generate(anthropicSingleSystemPrompt(systemPrompt),
                        anthropicSingleUserPrompt(userPrompt),
                        TravelPlanOutputSchemas.SINGLE_TRAVELLER_ANTHROPIC_CORE, planOpts);
                TravelPlanOutputSchemas.AnthropicTravelHealthPlanSupplemental suppValue = null;
                int suppTokens = 0;
                try {
                    var suppResult = structuredAiRouter.generate(anthropicSingleSystemPrompt(systemPrompt),
                            anthropicSupplementalUserPrompt(userPrompt),
                            TravelPlanOutputSchemas.SINGLE_TRAVELLER_ANTHROPIC_SUPP, planOpts);
                    suppValue = suppResult.value();
                    suppTokens = suppResult.estimatedTokens() != null ? suppResult.estimatedTokens() : 0;
                } catch (Exception e) {
                    log.warn("Anthropic supplemental call failed for travelPlanId={}; continuing with core sections only: {}",
                            travelPlanId, e.getMessage());
                }
                structuredValue = TravelPlanOutputSchemas.expandAnthropic(coreResult.value(), suppValue);
                structuredValue = TravelPlanOutputSchemas.withMandatoryDisclaimer(structuredValue);
                structuredValue = TravelPlanOutputSchemas.withoutDecisionFlags(structuredValue);
                TravelPlanOutputValidator.validateLite(structuredValue);
                planJson = objectMapper.valueToTree(structuredValue).toString();
                provider = coreResult.provider();
                model = coreResult.model();
                estimatedTokens = (coreResult.estimatedTokens() != null ? coreResult.estimatedTokens() : 0)
                        + suppTokens;
            } else {
                StructuredAiResult<TravelPlanOutputSchemas.TravelHealthPlanOutput> result
                        = structuredAiRouter.generate(systemPrompt, userPrompt,
                                TravelPlanOutputSchemas.SINGLE_TRAVELLER, planOpts);
                structuredValue = TravelPlanOutputSchemas.withMandatoryDisclaimer(result.value());
                structuredValue = TravelPlanOutputSchemas.withoutDecisionFlags(structuredValue);
                TravelPlanOutputValidator.validate(structuredValue);
                planJson = objectMapper.valueToTree(structuredValue).toString();
                provider = result.provider();
                model = result.model();
                estimatedTokens = result.estimatedTokens();
            }
            JsonNode structuredOutput = objectMapper.valueToTree(structuredValue);

            long elapsedMs = Duration.between(startedAt, Instant.now()).toMillis();

            generatedPlan.setPlanJson(planJson);
            generatedPlan.setStatus("active");
            generatedPlan.setSystemPrompt("");
            generatedPlan.setUserPrompt(userPrompt);
            generatedPlan.setProvider(provider);
            generatedPlan.setModelUsed(model);
            generatedPlan.setTokensUsed(estimatedTokens);
            generatedPlan.setPlanGenerationTokensUsed(estimatedTokens);
            generatedPlan.setProcessingTimeMs(elapsedMs);
            generatedPlan.setErrorMessage(null);

            travelPlan.setStatus("COMPLETED");
            travelPlan.setMedicalConsiderations(joinArray(structuredOutput, "recommendations"));
            travelPlan.setVaccinations(joinArray(structuredOutput, "vaccinations"));
            travelPlan.setHealthAlerts(joinArray(structuredOutput, "healthRiskOverview"));
            travelPlan.setSafetyAdvisories(joinArray(structuredOutput, "nextSteps"));
            travelPlan.setEmergencyContacts(joinArray(structuredOutput.path("medicalCare"), "emergencyContacts"));
            travelPlanRepository.save(travelPlan);

            attachSummaryPdfIfEligible(travelPlan, generatedPlan);
            generatedPlanRepository.save(generatedPlan);

            aiLog.setStatus("success");
            aiLog.setOutputSummary(compactSummary(structuredOutput));
            aiLog.setTokensUsed(estimatedTokens);
            aiLog.setPlanGenerationTokensUsed(estimatedTokens);
            aiLog.setSummaryGenerationTokensUsed(generatedPlan.getSummaryGenerationTokensUsed());
            aiLog.setTokensUsed((estimatedTokens != null ? estimatedTokens : 0)
                    + (generatedPlan.getSummaryGenerationTokensUsed() != null
                            ? generatedPlan.getSummaryGenerationTokensUsed()
                            : 0));
            aiLog.setModelUsed(model);
            aiLog.setProcessingTimeMs(elapsedMs);
            aiRequestLogRepository.save(aiLog);

            log.info(
                    "Travel plan generation completed: travelPlanId={} provider={} model={} tokens≈{} durationMs={}",
                    travelPlanId,
                    provider,
                    model,
                    estimatedTokens,
                    elapsedMs);

            handlePostGeneration(travelPlan);
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

            throw new PlanGenerationException("Travel plan generation failed", ex);
        }
    }

    private AiRequestLog buildAiLog(TravelPlan travelPlan) {
        AiRequestLog aiLog = new AiRequestLog();
        aiLog.setDestination(travelPlan.getDestination());
        aiLog.setPromptSummary("Generate structured travel health report (current trip + onboarding health JSON)");
        aiLog.setRiskLevel((travelPlan.getRiskScore() != null && travelPlan.getRiskScore() >= 60) ? "high" : "medium");
        aiLog.setCreditConsumed(BigDecimal.ONE);
        aiLog.setCompany(travelPlan.getCompany());
        aiLog.setUser(travelPlan.getUser());
        return aiLog;
    }

    private void handlePostGeneration(TravelPlan travelPlan) {
        if (travelPlan.getPlanTier() == PlanTier.FREE) {
            travelPlan.setDoctorValidationStatus(DoctorValidationStatus.NOT_REQUIRED);
            travelPlanRepository.save(travelPlan);
            sendReadyEmail(travelPlan);
            return;
        }

        // STANDARD or PREMIUM: notify doctors
        travelPlan.setDoctorValidationStatus(DoctorValidationStatus.PENDING);
        travelPlanRepository.save(travelPlan);

        doctorWebSocketService.broadcastNewPlanPending(travelPlan.getId(), travelPlan.getDestination());

        List<TravelPlanDoctorAssignment> assignments = assignmentRepository
                .findByTravelPlanIdAndDeletedAtIsNull(travelPlan.getId());
        List<User> doctors = assignments.isEmpty()
                ? userRepository.findByRoleName(Roles.Doctor.name())
                : assignments.stream().map(TravelPlanDoctorAssignment::getDoctor).toList();
        for (User doctor : doctors) {
            if (doctor.getEmail() == null)
                continue;
            String docFirstName = doctor.getFirstName() != null ? doctor.getFirstName() : "there";
            queueService.dispatch(JobType.EMAIL_DOCTOR_PLAN_READY, Map.of(
                    "to", doctor.getEmail(),
                    "subject", "New travel plan pending review: " + travelPlan.getDestination(),
                    "variables", Map.of(
                            "firstName", docFirstName,
                            "destination", travelPlan.getDestination(),
                            "planId", String.valueOf(travelPlan.getId()))));
        }
    }

    private void attachSummaryPdfIfEligible(TravelPlan travelPlan, GeneratedPlan generatedPlan) {
        if (travelPlan.getPlanTier() != PlanTier.STANDARD && travelPlan.getPlanTier() != PlanTier.PREMIUM) {
            return;
        }
        queueService.dispatch(JobType.GENERATE_SUMMARY_PDF, Map.of(
                "travelPlanId", travelPlan.getId(),
                "generatedPlanId", generatedPlan.getId()));
        log.info("Summary PDF generation queued: travelPlanId={} generatedPlanId={}",
                travelPlan.getId(), generatedPlan.getId());
    }

    @Transactional
    public void processSummaryPdf(Long travelPlanId, Long generatedPlanId) {
        TravelPlan travelPlan = travelPlanRepository.findById(travelPlanId)
                .orElseThrow(() -> new NoSuchElementException("Travel plan not found: " + travelPlanId));
        GeneratedPlan generatedPlan = generatedPlanRepository.findById(generatedPlanId)
                .orElseThrow(() -> new NoSuchElementException("Generated plan not found: " + generatedPlanId));

        log.info("Summary PDF generation starting: travelPlanId={} generatedPlanId={} destination=\"{}\"",
                travelPlanId, generatedPlanId, travelPlan.getDestination());

        byte[] summaryPdf = travelPlanSummaryPdfGenerator.generate(travelPlan, generatedPlan);
        String filename = "summary-plan-" + travelPlanId + "-" + UUID.randomUUID() + ".pdf";
        String storagePath = storageService.storeBytes(summaryPdf, "travel-plan-summaries", filename,
                "application/pdf");
        generatedPlan.setSummaryPdfUrl(storageService.getUrl(storagePath));
        generatedPlanRepository.save(generatedPlan);

        log.info("Summary PDF generation completed: travelPlanId={} generatedPlanId={}", travelPlanId, generatedPlanId);
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
                        "companyName", companyName)));
    }

    private String resolveQuestionnaireJson(UserOnboarding onboarding, String travelPlanQuestionnaire) {
        if (StringUtils.hasText(travelPlanQuestionnaire)) {
            return travelPlanQuestionnaire;
        }
        if (onboarding != null && StringUtils.hasText(onboarding.getResponsesJson())) {
            return OnboardingResponsesSanitizer.stripItineraryFields(onboarding.getResponsesJson(), objectMapper);
        }
        return null;
    }

    private void handleHardStop(TravelPlan travelPlan, GeneratedPlan generatedPlan, AiRequestLog aiLog,
            ClinicalContext clinicalContext, Instant startedAt) {
        long elapsedMs = Duration.between(startedAt, Instant.now()).toMillis();

        // Build hard stop JSON response
        String hardStopJson = buildHardStopJson(clinicalContext);

        generatedPlan.setPlanJson(hardStopJson);
        generatedPlan.setStatus("hard_stop");
        generatedPlan.setProcessingTimeMs(elapsedMs);
        generatedPlan.setErrorMessage(null);
        generatedPlanRepository.save(generatedPlan);

        travelPlan.setStatus("HARD_STOP");
        travelPlan.setMedicalConsiderations("HARD STOP: " + clinicalContext.hardStop().condition());
        travelPlan.setHealthAlerts(clinicalContext.hardStop().reason());
        travelPlan.setSafetyAdvisories(
                "Specialist clearance required: " + clinicalContext.hardStop().recommendedSpecialist());
        travelPlanRepository.save(travelPlan);

        aiLog.setStatus("hard_stop");
        aiLog.setOutputSummary("Hard stop triggered: " + clinicalContext.hardStop().condition());
        aiLog.setProcessingTimeMs(elapsedMs);
        aiRequestLogRepository.save(aiLog);

        log.info("Travel plan hard stop completed: travelPlanId={} condition=\"{}\" durationMs={}",
                travelPlan.getId(), clinicalContext.hardStop().condition(), elapsedMs);
    }

    private String buildHardStopJson(ClinicalContext clinicalContext) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "reportTitle", "Travel Health Advisory - Specialist Clearance Required",
                    "hardStop", Map.of(
                            "conditionTriggered", clinicalContext.hardStop().condition(),
                            "reason", clinicalContext.hardStop().reason(),
                            "recommendedSpecialist", clinicalContext.hardStop().recommendedSpecialist()),
                    "overallRiskLevel", "VERY_HIGH",
                    "medicalDisclaimer", ClinicalRules.MANDATORY_DISCLAIMER,
                    "nextSteps", List.of(
                            "Do not proceed with travel at this time without specialist clearance",
                            "Consult with: " + clinicalContext.hardStop().recommendedSpecialist(),
                            "Obtain written clearance before rebooking travel",
                            "Contact TMAG support if you have questions about this advisory")));
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to build hard stop JSON", ex);
        }
    }

    // private String buildSystemPrompt() {
    // return """
    // You are TMAG's travel medicine advisory engine.
    // Return ONLY valid JSON matching this exact schema:
    // {
    // "reportTitle": "string",
    // "travellerName": "string",
    // "destination": "string",
    // "travelDates": "string",
    // "tripAtGlance": {
    // "durationDays": number,
    // "purpose": "string",
    // "travelling": "string",
    // "accommodation": "string",
    // "insurance": "string"
    // },
    // "healthRiskOverview": [
    // {"category":"string","level":"LOW|MODERATE|HIGH","summary":"string"}
    // ],
    // "vaccinations": [
    // {"vaccine":"string","status":"string","recommendation":"string","action":"string"}
    // ],
    // "recommendations": [
    // {"title":"string","details":"string"}
    // ],
    // "afterReturn": {
    // "within1Week":["string"],
    // "within4Weeks":["string"],
    // "beyond4Weeks":["string"],
    // "redFlag":"string"
    // },
    // "medicalCare": {
    // "clinics":[{"name":"string","address":"string","phone":"string","distance":"string","notes":"string"}],
    // "embassyContacts":[{"name":"string","details":"string"}],
    // "emergencyContacts":[{"label":"string","value":"string"}]
    // },
    // "itineraryGuidance": {
    // "tripType":"ONE_WAY|RETURN|MULTI_STOP",
    // "summary":"string",
    // "routeAdvice":[{"stop":"string","country":"string","guidance":"string"}],
    // "returnGuidance":["string"]
    // },
    // "nextSteps": ["string"],
    // "medicalDisclaimer":"string"
    // }
    // The user prompt has a "Current trip" section: that is the ONLY authoritative
    // source for
    // destination, country, trip length, purpose, and travel dates in your JSON
    // output.
    // For RETURN trips, when Departure date and Return date appear in that section,
    // set travelDates
    // to a clear human-readable range using those exact dates, and set
    // tripAtGlance.durationDays
    // to the inclusive calendar day count (must match "Trip length" in the user
    // prompt).
    // A separate "Traveller health context" block contains questionnaire JSON for
    // medical
    // personalisation only; never infer this trip's destination or itinerary from
    // it
    // (server still treats the Current trip block as sole truth).
    // Use concise, practical medical-travel guidance. Do not include markdown
    // fences.

    // COVERAGE (mandatory — do not omit categories to save space):
    // 1) healthRiskOverview: Include EXACTLY one object per category below, in this
    // order, using these
    // exact category strings. Every category must appear even if risk is minimal —
    // use level LOW and a
    // short summary (1–3 sentences) explaining low concern, "not applicable to this
    // itinerary", or routine
    // baseline. Only use MODERATE or HIGH when truly warranted.
    // - "Food and water safety"
    // - "Vector-borne diseases"
    // - "Respiratory infections"
    // - "Environmental health (heat, sun, air quality)"
    // - "Injuries and road traffic safety"
    // - "Rabies and animal contact"
    // - "Blood-borne and sexual health"
    // - "Altitude-related illness"

    // 2) vaccinations: Include one object per vaccine topic below (same vaccine
    // string labels where
    // possible). Do not skip a topic because it seems unnecessary — for
    // low-relevance or not-indicated
    // vaccines, set status to e.g. "Low priority — not destination-specific" or
    // "Not routinely indicated
    // for this itinerary", recommendation to a brief evidence-based line, and
    // action to routine follow-up
    // (e.g. confirm records with GP) or "None specific". Topics (in order):
    // - "Routine immunizations (e.g. MMR, varicella, dTdap, polio/IPV)"
    // - "Influenza"
    // - "COVID-19"
    // - "Hepatitis A"
    // - "Hepatitis B"
    // - "Typhoid"
    // - "Yellow fever"
    // - "Japanese encephalitis"
    // - "Meningococcal"
    // - "Rabies pre-exposure"
    // - "Cholera (oral vaccine)"

    // 3) recommendations: Include at least one object per topic below. For areas of
    // low relevance to this
    // trip, keep the title and use details to state clearly that risk is low or the
    // topic is standard
    // baseline care — still include the row. Order as listed; tailor details to
    // destination and traveller
    // context.
    // - "Pre-travel review & vaccination records"
    // - "Food and water hygiene"
    // - "Vector bite prevention"
    // - "Sun, heat, and environmental precautions"
    // - "Injury and road safety"
    // - "Sexual health and blood exposure"
    // - "Jet lag, sleep, and mental wellbeing"
    // - "Malaria and other chemoprophylaxis (state if not indicated)"
    // - "Traveller-specific considerations (from health context)"

    // 4) itineraryGuidance (mandatory):
    // - tripType must exactly mirror the Current trip "Trip Type" value.
    // - routeAdvice:
    // - ONE_WAY: include exactly one stop guidance row.
    // - RETURN: include outbound stop guidance and include practical return-phase
    // reminders in returnGuidance.
    // - MULTI_STOP: include one guidance row per stop from Trip Stops JSON, in
    // listed order.
    // - returnGuidance:
    // - RETURN: include at least 4 actionable bullets for the return leg and
    // immediate post-return period.
    // - ONE_WAY or MULTI_STOP: returnGuidance may be an empty array unless there is
    // explicit return information.
    // """;
    // }

    private UserOnboarding resolveOnboarding(TravelPlan travelPlan) {
        User user = travelPlan.getUser();
        if (user == null || user.getId() == null) {
            return null;
        }
        return userOnboardingRepository.findByUser_Id(user.getId()).orElse(null);
    }

    /**
     * When this is the user's first active plan, onboarding JSON is the
     * authoritative health questionnaire;
     * create-plan responses are still stored but not fed to the model for that
     * generation.
     */
    private String resolveTravelPlanQuestionnaire(TravelPlan travelPlan, boolean firstTravelPlanForUser) {
        if (travelPlan == null || travelPlan.getId() == null) {
            return null;
        }
        if (firstTravelPlanForUser) {
            return null;
        }
        TravelPlanQuestionnaire questionnaire = travelPlanQuestionnaireRepository
                .findByTravelPlan_Id(travelPlan.getId())
                .orElse(null);
        if (questionnaire == null || !StringUtils.hasText(questionnaire.getResponsesJson())) {
            return null;
        }
        return questionnaire.getResponsesJson().trim();
    }

    /**
     * True when this plan is the only non-deleted {@link TravelPlan} row for its
     * user (the typical first plan case).
     */
    private boolean isFirstActiveTravelPlanForUser(TravelPlan travelPlan) {
        User user = travelPlan.getUser();
        if (user == null || user.getId() == null) {
            return false;
        }
        return travelPlanRepository.countByUserId(user.getId()) == 1;
    }

    private String buildUserPrompt(
            TravelPlan travelPlan,
            List<PlanGenerationContext> contexts,
            UserOnboarding onboarding,
            String travelPlanQuestionnaire,
            boolean firstTravelPlanForUser) {
        StringBuilder builder = new StringBuilder();
        builder.append("Generate a travel health report for this trip.\n\n");

        builder.append("### Current trip (authoritative — use ONLY this section for destination, country, ")
                .append("trip duration, purpose, and dates in the report JSON)\n");
        builder.append("Destination: ").append(nullSafe(travelPlan.getDestination())).append("\n");
        builder.append("Country: ").append(nullSafe(travelPlan.getCountry())).append("\n");
        builder.append("Duration Days: ").append(travelPlan.getDuration() != null ? travelPlan.getDuration() : 0)
                .append("\n");
        builder.append("Purpose: ").append(nullSafe(travelPlan.getPurpose())).append("\n");
        builder.append("Trip Type: ").append(resolveTripType(travelPlan.getTripType())).append("\n");
        appendReturnScheduleLines(travelPlan, builder);
        builder.append("Trip Stops JSON: ").append(extractTripStopsJson(travelPlan)).append("\n");
        builder.append("Risk Score (app): ").append(travelPlan.getRiskScore() != null ? travelPlan.getRiskScore() : 0)
                .append("\n");
        builder.append("Traveller display name: ").append(resolveTravellerName(travelPlan.getUser())).append("\n");
        if (StringUtils.hasText(travelPlan.getMedicalConsiderations())) {
            builder.append("User notes for this trip: ").append(travelPlan.getMedicalConsiderations().trim())
                    .append("\n");
        }
        builder.append("\n");

        builder.append("### Traveller health context (personalisation only)\n");
        builder.append(
                "Use for vaccines, conditions, allergies, prior travel immunity, activities preferences, etc.\n");
        builder.append("Do NOT use this block for this trip's destination, cities, or travel dates; the Current trip ")
                .append("section above always wins.\n");

        if (StringUtils.hasText(travelPlanQuestionnaire)) {
            builder.append("Primary questionnaire source: travel_plan_questionnaires (create-plan responses)\n");
            builder.append("Questionnaire responses JSON:\n")
                    .append(travelPlanQuestionnaire)
                    .append("\n\n");
        } else if (onboarding == null) {
            builder.append("(No questionnaire context linked to this user.)\n\n");
        } else {
            if (firstTravelPlanForUser) {
                builder.append(
                        "Primary questionnaire source: user_onboardings (onboarding questionnaire; first travel plan)\n");
            } else {
                builder.append(
                        "Primary questionnaire source: user_onboardings (fallback; no create-plan questionnaire)\n");
            }
            if (StringUtils.hasText(onboarding.getNationality())) {
                builder.append("Nationality: ").append(onboarding.getNationality().trim()).append("\n");
            }
            if (StringUtils.hasText(onboarding.getUserType())) {
                builder.append("Onboarding user type: ").append(onboarding.getUserType().trim()).append("\n");
            }
            String sanitized = OnboardingResponsesSanitizer.stripItineraryFields(onboarding.getResponsesJson(),
                    objectMapper);
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

    private String compactSummary(JsonNode jsonNode) {
        String destination = jsonNode.path("destination").asText("");
        int recommendationCount = jsonNode.path("recommendations").isArray()
                ? jsonNode.path("recommendations").size()
                : 0;
        return "Generated structured plan for " + destination + " with " + recommendationCount + " recommendations";
    }

    private String anthropicSingleSystemPrompt(String basePrompt) {
        return basePrompt + """
 
 ANTHROPIC STRUCTURED OUTPUT CONTRACT — CORE PLAN
 The response schema is flattened to stay under Claude's grammar limit.
 Populate parallel arrays by index (same length for related arrays):
 - healthRiskCategories, healthRiskLevels, healthRiskSummaries
 - vaccinationVaccines, vaccinationStatuses, vaccinationRecommendations, vaccinationActions
 - recommendationTitles, recommendationDetails
 - routeAdviceStops, routeAdviceCountries, routeAdviceGuidance
 - emergencyContactLabels, emergencyContactValues (parallel)
 
 A separate supplemental call handles remaining sections (flight, malaria, medical conditions,
 medications, specialist referrals, sexual health, pregnancy, after-return, clinics, embassies).
 Focus this call on the core sections listed above.
 Do not emit TREE_<number>_<NAME> identifiers. Keep entries brief.
 """;
    }

    private String anthropicSingleUserPrompt(String basePrompt) {
        return basePrompt + """
 
 CORE PLAN CALL: include essential dossier content only — health risks, vaccinations,
 recommendations, itinerary guidance, emergency contacts, next steps, trip snapshot, and
 medical disclaimer. Supplementary sections (flight, malaria, medical conditions, medications,
 specialist referrals, sexual health, pregnancy, after-return clinics) come in a second call.
 Keep entries concise to stay within the token budget.
 """;
    }

    private String anthropicSupplementalUserPrompt(String basePrompt) {
        return basePrompt + """
 
 SUPPLEMENTAL CALL: provide the following additional dossier sections only.
 Do NOT repeat the core sections (foreword, health risks, vaccinations, recommendations,
 itinerary, emergency contacts, next steps, disclaimer) — those were already generated.
 Produce only: flightHealth, malariaPrevention, medicalConditions, medicationLogistics,
 specialistReferrals, sexualHealth, pregnancyGuidance, afterReturn,
 clinics, embassyContacts. Keep entries brief.
 """;
    }

    private String anthropicFamilySystemPrompt(String basePrompt) {
        return basePrompt + """
 
 ANTHROPIC STRUCTURED OUTPUT CONTRACT
 The response schema is flattened to keep Claude's compiled grammar small.
 Populate all member array fields by index. Each index describes one family member:
 memberIds, memberNames, relationships, relationshipToMainApplicants, displayLabels,
 ageAtDepartures, executiveSummaries, vaccinationSummaries, medicationSummaries,
 healthConsiderationSummaries, travellerSpecific, hardStops.
 Keep each member summary concise and preserve memberIds and displayLabels exactly.
 """;
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

    private void processFamilyGeneration(TravelPlan travelPlan, GeneratedPlan generatedPlan, AiRequestLog aiLog,
            Instant startedAt) {
        Long tripId = travelPlan.getFamilyTrip().getId();

        // Load all member questionnaire data from the aggregate questionnaire
        TravelPlanQuestionnaire questionnaire = travelPlanQuestionnaireRepository
                .findByTravelPlan_Id(travelPlan.getId())
                .orElse(null);

        String membersJson = null;
        if (questionnaire != null && org.springframework.util.StringUtils.hasText(questionnaire.getResponsesJson())) {
            try {
                JsonNode root = objectMapper.readTree(questionnaire.getResponsesJson());
                if (root.has("members")) {
                    membersJson = objectMapper.writeValueAsString(root.get("members"));
                }
            } catch (Exception e) {
                log.warn("Failed to parse family aggregate questionnaire for planId={}", travelPlan.getId(), e);
            }
        }

        if (membersJson == null || membersJson.equals("null")) {
            log.warn("No family member data found for planId={}, aborting generation", travelPlan.getId());
            generatedPlan.setStatus("failed");
            generatedPlan.setErrorMessage("No member questionnaire data");
            generatedPlanRepository.save(generatedPlan);
            travelPlan.setStatus("FAILED");
            travelPlanRepository.save(travelPlan);
            return;
        }

        List<PlanGenerationContext> contexts = contextService.findEnabled();
        String systemPrompt = systemPromptBuilder.buildFamily();
        String userPrompt = buildFamilyUserPrompt(travelPlan, membersJson, contexts);

        AiCallOptions planOpts = structuredAiRouter.resolvePlanOptions(
                travelPlan.getPlanTier() != null ? travelPlan.getPlanTier().name() : "FREE");
        boolean isAnthropic = planOpts.providerOverride() != null
                && planOpts.providerOverride().toLowerCase().contains("anthropic");
        TravelPlanOutputSchemas.FamilyTravelHealthPlanOutput structuredValue;
        String provider;
        String model;
        Integer estimatedTokens;
        if (isAnthropic) {
            StructuredAiResult<TravelPlanOutputSchemas.AnthropicFamilyTravelHealthPlanOutput> liteResult
                    = structuredAiRouter.generate(anthropicFamilySystemPrompt(systemPrompt), userPrompt,
                            TravelPlanOutputSchemas.FAMILY_LITE, planOpts);
            structuredValue = TravelPlanOutputSchemas.expandAnthropic(liteResult.value());
            provider = liteResult.provider();
            model = liteResult.model();
            estimatedTokens = liteResult.estimatedTokens();
        } else {
            StructuredAiResult<TravelPlanOutputSchemas.FamilyTravelHealthPlanOutput> result
                    = structuredAiRouter.generate(systemPrompt, userPrompt,
                            TravelPlanOutputSchemas.FAMILY, planOpts);
            structuredValue = result.value();
            provider = result.provider();
            model = result.model();
            estimatedTokens = result.estimatedTokens();
        }
        structuredValue = TravelPlanOutputSchemas.withMandatoryDisclaimer(structuredValue);
        TravelPlanOutputValidator.validate(structuredValue);
        JsonNode structuredOutput = objectMapper.valueToTree(structuredValue);
        structuredOutput = applyFamilyDisplayLabels(structuredOutput, membersJson);
        String planJson = structuredOutput.toString();

        long elapsedMs = Duration.between(startedAt, Instant.now()).toMillis();

        generatedPlan.setPlanJson(planJson);
        generatedPlan.setStatus("active");
        generatedPlan.setProvider(provider);
        generatedPlan.setModelUsed(model);
        generatedPlan.setTokensUsed(estimatedTokens);
        generatedPlan.setPlanGenerationTokensUsed(estimatedTokens);
        generatedPlan.setProcessingTimeMs(elapsedMs);
        generatedPlan.setErrorMessage(null);
        generatedPlanRepository.save(generatedPlan);

        travelPlan.setStatus("COMPLETED");
        travelPlanRepository.save(travelPlan);

        log.info("Family plan generation completed: planId={} tripId={} elapsedMs={}", travelPlan.getId(), tripId,
                elapsedMs);

        aiLog.setStatus("success");
        aiLog.setTokensUsed(estimatedTokens);
        aiLog.setProcessingTimeMs(elapsedMs);
        aiRequestLogRepository.save(aiLog);

        attachSummaryPdfIfEligible(travelPlan, generatedPlan);
        handlePostGeneration(travelPlan);
    }

    private JsonNode applyFamilyDisplayLabels(JsonNode structuredOutput, String membersJson) {
        if (structuredOutput == null || !structuredOutput.isObject()) {
            return structuredOutput;
        }
        try {
            JsonNode inputMembers = objectMapper.readTree(membersJson);
            if (!inputMembers.isArray()) {
                return structuredOutput;
            }
            Map<Long, JsonNode> inputByMemberId = new java.util.HashMap<>();
            for (JsonNode inputMember : inputMembers) {
                if (inputMember.path("memberId").isNumber()) {
                    inputByMemberId.put(inputMember.path("memberId").asLong(), inputMember);
                }
            }

            JsonNode outputMembers = structuredOutput.path("members");
            if (!outputMembers.isArray()) {
                return structuredOutput;
            }
            for (JsonNode outputMember : outputMembers) {
                if (!outputMember.isObject() || !outputMember.path("memberId").isNumber()) {
                    continue;
                }
                JsonNode inputMember = inputByMemberId.get(outputMember.path("memberId").asLong());
                if (inputMember == null) {
                    continue;
                }
                ObjectNode outputObject = (ObjectNode) outputMember;
                copyTextField(inputMember, outputObject, "displayLabel", true);
                copyTextField(inputMember, outputObject, "relationshipToMainApplicant", false);
            }
        } catch (Exception ex) {
            log.warn("Failed to apply family display labels to generated plan JSON", ex);
        }
        return structuredOutput;
    }

    private void copyTextField(JsonNode source, ObjectNode target, String field, boolean overwrite) {
        String value = source.path(field).asText("");
        if (!StringUtils.hasText(value)) {
            return;
        }
        if (overwrite || !StringUtils.hasText(target.path(field).asText(""))) {
            target.put(field, value);
        }
    }

    private String buildFamilyUserPrompt(TravelPlan plan, String membersJson, List<PlanGenerationContext> contexts) {
        StringBuilder builder = new StringBuilder();
        builder.append("Generate a family travel health plan for this trip.\n\n");

        builder.append("### Trip details (authoritative for destination, country, duration, dates)\n");
        builder.append("Destination: ").append(nullSafe(plan.getDestination())).append("\n");
        builder.append("Country: ").append(nullSafe(plan.getCountry())).append("\n");
        builder.append("Duration Days: ").append(plan.getDuration() != null ? plan.getDuration() : 0).append("\n");
        builder.append("Purpose: ").append(nullSafe(plan.getPurpose())).append("\n");
        builder.append("Trip Type: ").append(resolveTripType(plan.getTripType())).append("\n");
        appendReturnScheduleLines(plan, builder);
        builder.append("Trip Stops JSON: ").append(extractTripStopsJson(plan)).append("\n\n");

        builder.append("### Family members with individual health questionnaires\n");
        builder.append(
                "Each entry has: memberId, firstName, lastName, relationship, relationshipToMainApplicant, displayLabel, writingPerspective, ageAtDeparture, responses (health questionnaire).\n");
        builder.append(
                "The main applicant is the reader of the plan. Write member-specific advice in second person, relative to the main applicant.\n");
        builder.append(
                "Use each member's displayLabel when introducing or referring to them, e.g. 'Your son Bob should...' or 'For your spouse Sarah...'.\n");
        builder.append(
                "Do not use detached third-person biography language such as 'Bob is a 15 year old male' or 'the traveller is'.\n");
        builder.append("Generate a full, personalised medical section for each member in the `members` array.\n");
        builder.append("Use the memberId value exactly as given — do NOT change it.\n\n");
        builder.append("Members JSON:\n").append(membersJson).append("\n\n");

        if (!contexts.isEmpty()) {
            builder.append("### Platform reference materials\n");
            for (PlanGenerationContext ctx : contexts) {
                builder.append("- ").append(ctx.getTitle()).append(":\n")
                        .append(trimContext(ctx.getSynthesizedText())).append("\n\n");
            }
        }
        return builder.toString();
    }
}
