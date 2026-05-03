package com.TravelMedicineAdvisory.Server.domain.travelplan;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.TravelMedicineAdvisory.Server.core.ai.AiGenerationClient;
import com.TravelMedicineAdvisory.Server.core.ai.AiGenerationResult;
import com.TravelMedicineAdvisory.Server.domain.plans.GeneratedPlan;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

@Component
public class TravelPlanSummaryPdfGenerator {

    private static final Logger log = LoggerFactory.getLogger(TravelPlanSummaryPdfGenerator.class);

    static final String SECTION_1_HEADING = "SECTION 1: CRITICAL ACTIONS BEFORE DEPARTURE";
    static final String SECTION_2_HEADING = "SECTION 2: TRIP SNAPSHOT";
    static final String SECTION_3_HEADING = "SECTION 3: VACCINES";
    static final String SECTION_4_HEADING = "SECTION 4: WHAT TO PACK AND DAILY ROUTINE";
    static final String SECTION_5_HEADING = "SECTION 5: RED FLAGS AND EMERGENCY CONTACTS";
    static final String CLOSING_LINE_EXACT =
            "Full clinical dossier attached. Share with your travel medicine clinician at your pre-travel appointment.";

    private static final String TEAL_DEEP = "#0d3d35";
    private static final String TEAL_MID = "#1a7a6a";
    private static final String TEAL_LIGHT = "#dff2ee";
    private static final String BG_PAGE = "#f8f5f1";
    private static final String BG_SUBTLE = "#f2ede7";
    private static final String DARK = "#1a1208";
    private static final String BODY = "#3a2e22";
    private static final String BORDER = "#ddd5cb";
    private static final String RED_SOFT = "#fef2f2";
    private static final String SEPARATOR = " &#183; ";

    private static final String ACTION_SHEET_SYSTEM_PROMPT = """
            You are the TMAG Action Sheet Generator. You operate as the
            second stage of a two-stage report pipeline. Stage 1 has
            already produced a complete TMAG Personalised Travel Health
            Dossier for a specific traveller. Your sole task is to
            summarise that dossier into a one-page Action Sheet.

            ROLE BOUNDARY

            You are a summariser, not a clinician. You do not generate
            clinical recommendations. You extract, condense, and reword
            content that already exists in the input dossier. If a
            recommendation is not in the dossier, it does not appear in
            the Action Sheet. If the dossier is silent on an item, you
            are silent on it. You do not soften, upgrade, or downgrade
            any clinical instruction. Required stays Required. Strongly
            Recommended stays Strongly Recommended. Consider stays
            Consider.

            AUDIENCE

            The traveller themselves, reading on a phone the night
            before a clinic appointment. Plain language. No medical
            jargon where a lay term works. Assume no clinical training.

            OUTPUT STRUCTURE

            Produce exactly five sections in this order. Do not add
            sections. Do not rename sections. Do not reorder sections.

            SECTION 1: CRITICAL ACTIONS BEFORE DEPARTURE
            A maximum of four bullets. Include only items that are
            mandatory for entry, time-sensitive due to vaccine lead
            times, or life-threatening if missed. Each bullet must
            state the action and the deadline or timing. Order bullets
            by urgency, most urgent first. If the dossier flags fewer
            than four such items, list only those.

            SECTION 2: TRIP SNAPSHOT
            One line. Format: Origin to Destination, duration, purpose,
            overall risk level. Pull these directly from the dossier
            header.

            SECTION 3: VACCINES
            A table with three columns: Vaccine, Status, Action.
            Include only vaccines marked Required, Strongly
            Recommended, or Recommended in the input dossier.
            Exclude any vaccine marked Not Indicated, Not Applicable,
            or where the dossier states the vaccine is not required for
            the itinerary.
            The Action column must be a single short instruction, for
            example, Arrange before departure, Confirm status with
            clinician, or Complete accelerated course.
            Do not include rationale text. Do not include schedule
            detail. The clinician handles that.

            SECTION 4: WHAT TO PACK AND DAILY ROUTINE
            A maximum of eight bullets. Must include, when present in
            the dossier:
            Malaria prophylaxis: drug name, when to start, daily
            dosing instruction, when to stop.
            Essential kit drawn from the dossier: insect repellent
            with concentration, sunscreen, oral rehydration salts,
            bed net, condoms if flagged, first aid kit.
            Do not invent kit items. If the dossier does not list
            something, do not add it.

            SECTION 5: RED FLAGS AND EMERGENCY CONTACTS
            One line listing symptoms that require immediate medical
            attention, drawn from the dossier red flags section.
            The top two named clinical facilities at the destination,
            with location.
            The local emergency number.
            A line referencing the traveller's insurance emergency
            line.

            CLOSING LINE

            End the Action Sheet with this exact line on its own:
            Full clinical dossier attached. Share with your travel
            medicine clinician at your pre-travel appointment.

            LENGTH CONSTRAINT

            Total output must fit on one A4 page at 11pt body type.
            Target 350 to 450 words across all five sections combined.
            Do not exceed 450 words.

            STYLE RULES

            Plain language. Short sentences.
            Active voice where possible.
            No dashes anywhere. Use commas, full stops, or rephrase.
            No clinical rationale, no behavioural risk discussion, no
            jet lag content, no general hygiene paragraphs, no
            pregnancy counselling paragraphs, no sexual health
            counselling paragraphs. These live in the full dossier.
            Do not include disclaimers. The full dossier carries the
            disclaimer.
            Do not address the traveller by name in body copy. The
            dossier header already personalises the document.

            PROHIBITED BEHAVIOURS

            Do not generate any clinical content not present in the
            input dossier.
            Do not infer a vaccine status. If the dossier says
            Unknown, the Action Sheet says Unknown.
            Do not change a Required entry into a Recommended entry
            or vice versa.
            Do not merge or split vaccine entries.
            Do not add destinations, activities, or risks not in the
            source dossier.

            INPUT FORMAT

            The dossier is provided as JSON below. It is the single
            source of truth for the completed TMAG Personalised Travel
            Health Dossier. If the dossier contains an internal
            contradiction, defer to the more cautious recommendation
            and do not flag the contradiction in the Action Sheet.

            MACHINE OUTPUT CONTRACT

            Return only valid JSON. No markdown fences. No preamble.
            No commentary. No text outside the JSON object.
            Map your five sections and closing line into this exact
            schema (field names and nesting are fixed):
            {
              "section1CriticalBeforeDeparture": ["string"],
              "section2TripSnapshot": "string",
              "section3Vaccines": [{"vaccine":"string","status":"string","action":"string"}],
              "section4PackAndRoutine": ["string"],
              "section5": {
                "redFlagsLine": "string",
                "facilities": [{"name":"string","location":"string"}],
                "localEmergencyNumber": "string",
                "insuranceEmergencyLine": "string"
              },
              "closingLine": "string"
            }
            Limits: section1CriticalBeforeDeparture at most 4 strings;
            section4PackAndRoutine at most 8 strings;
            section3Vaccines only rows that belong in the table per rules above;
            section5.facilities at most 2 objects (top two facilities).
            closingLine must match the exact closing sentence above.
            Skip any string value matching TREE_<digits>_<UPPERCASE>.
            Remove hyphen, en dash, em dash, and underscore characters
            from all string values in the JSON (use spaces or rephrase).
            """;

    private static final String ACTION_SHEET_USER_PROMPT_PREFIX = """
            Produce the Action Sheet as JSON only, following the system instructions.

            Return exactly this JSON shape (types as shown):
            {
              "section1CriticalBeforeDeparture": ["string"],
              "section2TripSnapshot": "string",
              "section3Vaccines": [{"vaccine":"string","status":"string","action":"string"}],
              "section4PackAndRoutine": ["string"],
              "section5": {
                "redFlagsLine": "string",
                "facilities": [{"name":"string","location":"string"}],
                "localEmergencyNumber": "string",
                "insuranceEmergencyLine": "string"
              },
              "closingLine": "string"
            }

            Personalised Travel Health Dossier (JSON):
            """;

    private final ObjectMapper objectMapper;
    private final AiGenerationClient aiGenerationClient;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    public TravelPlanSummaryPdfGenerator(ObjectMapper objectMapper, AiGenerationClient aiGenerationClient) {
        this.objectMapper = objectMapper;
        this.aiGenerationClient = aiGenerationClient;
    }

    public byte[] generate(TravelPlan plan, GeneratedPlan generatedPlan) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            JsonNode summary = summarize(plan, generatedPlan);
            String html = buildHtml(plan, summary);
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(html, frontendUrl);
            registerFonts(builder);
            builder.toStream(out);
            builder.run();
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Failed to generate travel plan summary PDF: travelPlanId={} generatedPlanId={} destination=\"{}\"",
                    plan != null ? plan.getId() : null,
                    generatedPlan != null ? generatedPlan.getId() : null,
                    plan != null ? plan.getDestination() : null,
                    e);
            throw new RuntimeException("Failed to generate travel plan summary PDF", e);
        }
    }

    /**
     * Builds HTML for the Action Sheet PDF. Package-private for tests in the same package.
     */
    String buildHtml(TravelPlan plan, JsonNode summary) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\"/>");
        appendStyles(sb);
        sb.append("</head><body>");
        appendHero(sb, plan);
        sb.append("<main>");
        appendSection(sb, SECTION_1_HEADING, summary.path("section1CriticalBeforeDeparture"), 4);
        appendSectionOneLine(sb, SECTION_2_HEADING, text(summary, "section2TripSnapshot"));
        appendVaccineTable(sb, summary.path("section3Vaccines"));
        appendSection(sb, SECTION_4_HEADING, summary.path("section4PackAndRoutine"), 8);
        appendSection5(sb, summary.path("section5"));
        sb.append("<p class=\"closing\">").append(escape(text(summary, "closingLine"))).append("</p>");
        sb.append("</main></body></html>");
        return sb.toString();
    }

    private JsonNode summarize(TravelPlan plan, GeneratedPlan generatedPlan) {
        JsonNode source = parsePlanJson(generatedPlan);
        if (source == null || !source.isObject()) {
            return fallbackActionSheet(plan, source);
        }

        String userPrompt = ACTION_SHEET_USER_PROMPT_PREFIX + generatedPlan.getPlanJson();

        try {
            AiGenerationResult result = aiGenerationClient.generateSummary(ACTION_SHEET_SYSTEM_PROMPT, userPrompt);
            JsonNode summary = normalizeActionSheet(objectMapper.readTree(stripJsonFences(result.content())));
            if (hasUsableActionSheet(summary)) {
                generatedPlan.setSummaryGenerationTokensUsed(result.estimatedTokens());
                log.info("Generated travel plan Action Sheet PDF with {} model {} for travelPlanId={} generatedPlanId={} tokens={}",
                        result.provider(),
                        result.model(),
                        plan.getId(),
                        generatedPlan.getId(),
                        result.estimatedTokens());
                return summary;
            }
            log.warn("Summary model returned unusable JSON for travelPlanId={}; using fallback Action Sheet",
                    plan.getId());
        } catch (Exception ex) {
            log.warn("Summary model unavailable for travelPlanId={}; using fallback Action Sheet: {}",
                    plan.getId(),
                    ex.getMessage());
        }
        return fallbackActionSheet(plan, source);
    }

    private JsonNode normalizeActionSheet(JsonNode raw) {
        if (raw == null || !raw.isObject()) {
            return objectMapper.createObjectNode();
        }
        ObjectNode root = (ObjectNode) raw.deepCopy();

        ArrayNode s1 = objectMapper.createArrayNode();
        JsonNode a1 = root.path("section1CriticalBeforeDeparture");
        if (a1.isArray()) {
            for (JsonNode item : a1) {
                if (s1.size() >= 4) {
                    break;
                }
                addTextIfUsable(s1, item);
            }
        }
        root.set("section1CriticalBeforeDeparture", s1);

        root.put("section2TripSnapshot", cleanActionSheetText(text(root, "section2TripSnapshot")));

        ArrayNode vaccines = objectMapper.createArrayNode();
        JsonNode v = root.path("section3Vaccines");
        if (v.isArray()) {
            for (JsonNode item : v) {
                if (!item.isObject()) {
                    continue;
                }
                String vaccine = cleanActionSheetText(item.path("vaccine").asText(""));
                String status = cleanActionSheetText(item.path("status").asText(""));
                String action = cleanActionSheetText(item.path("action").asText(""));
                if (!StringUtils.hasText(vaccine) && !StringUtils.hasText(status) && !StringUtils.hasText(action)) {
                    continue;
                }
                ObjectNode row = vaccines.addObject();
                row.put("vaccine", vaccine);
                row.put("status", status);
                row.put("action", action);
            }
        }
        root.set("section3Vaccines", vaccines);

        ArrayNode s4 = objectMapper.createArrayNode();
        JsonNode a4 = root.path("section4PackAndRoutine");
        if (a4.isArray()) {
            for (JsonNode item : a4) {
                if (s4.size() >= 8) {
                    break;
                }
                addTextIfUsable(s4, item);
            }
        }
        root.set("section4PackAndRoutine", s4);

        ObjectNode s5 = objectMapper.createObjectNode();
        JsonNode raw5 = root.path("section5");
        if (raw5.isObject()) {
            s5.put("redFlagsLine", cleanActionSheetText(raw5.path("redFlagsLine").asText("")));
            s5.put("localEmergencyNumber", cleanActionSheetText(raw5.path("localEmergencyNumber").asText("")));
            s5.put("insuranceEmergencyLine", cleanActionSheetText(raw5.path("insuranceEmergencyLine").asText("")));
            ArrayNode fac = objectMapper.createArrayNode();
            JsonNode fl = raw5.path("facilities");
            if (fl.isArray()) {
                for (JsonNode f : fl) {
                    if (fac.size() >= 2) {
                        break;
                    }
                    if (!f.isObject()) {
                        continue;
                    }
                    ObjectNode fo = fac.addObject();
                    fo.put("name", cleanActionSheetText(f.path("name").asText("")));
                    fo.put("location", cleanActionSheetText(f.path("location").asText("")));
                }
            }
            s5.set("facilities", fac);
        } else {
            s5.put("redFlagsLine", "");
            s5.put("localEmergencyNumber", "");
            s5.put("insuranceEmergencyLine", "");
            s5.set("facilities", objectMapper.createArrayNode());
        }
        root.set("section5", s5);

        String closing = cleanActionSheetText(text(root, "closingLine"));
        if (!CLOSING_LINE_EXACT.equals(closing)) {
            closing = CLOSING_LINE_EXACT;
        }
        root.put("closingLine", closing);

        return root;
    }

    private void addTextIfUsable(ArrayNode target, JsonNode item) {
        String t = item.isTextual() ? item.asText("") : "";
        t = cleanActionSheetText(t);
        if (!StringUtils.hasText(t) || isInternalKey(t)) {
            return;
        }
        target.add(t);
    }

    private boolean hasUsableActionSheet(JsonNode summary) {
        if (summary == null || !summary.isObject()) {
            return false;
        }
        if (hasItems(summary.path("section1CriticalBeforeDeparture"))
                || hasItems(summary.path("section3Vaccines"))
                || hasItems(summary.path("section4PackAndRoutine"))) {
            return true;
        }
        if (StringUtils.hasText(text(summary, "section2TripSnapshot"))) {
            return true;
        }
        JsonNode s5 = summary.path("section5");
        if (s5.isObject()) {
            return StringUtils.hasText(text(s5, "redFlagsLine"))
                    || StringUtils.hasText(text(s5, "localEmergencyNumber"))
                    || StringUtils.hasText(text(s5, "insuranceEmergencyLine"))
                    || hasItems(s5.path("facilities"));
        }
        return false;
    }

    private boolean hasItems(JsonNode node) {
        return node != null && node.isArray() && !node.isEmpty();
    }

    private boolean isInternalKey(String value) {
        return value != null && value.matches("TREE_\\d+_[A-Z_]+");
    }

    private void appendStyles(StringBuilder sb) {
        sb.append("<style>");
        sb.append("@page{size:A4;margin:10mm 12mm 12mm 12mm}");
        sb.append("@page{@bottom-left{content:'Travel Medicine Advisory Global';font-family:'Hanken Grotesk',Arial,sans-serif;font-size:7pt;color:#9a8a7a}}");
        sb.append("@page{@bottom-right{content:'Page ' counter(page);font-family:'Hanken Grotesk',Arial,sans-serif;font-size:7pt;color:#9a8a7a}}");
        sb.append("*{box-sizing:border-box}body{margin:0;background:").append(BG_PAGE).append(";color:").append(BODY)
                .append(";font-family:'Hanken Grotesk',Arial,sans-serif;font-size:11pt;line-height:1.28}");
        sb.append(".hero{background:").append(TEAL_DEEP).append(";color:white;padding:14px 0 12px;border-bottom:3px solid ")
                .append(TEAL_MID).append("}");
        sb.append(".hero-inner{padding:0 12mm}");
        sb.append(".brand{font-size:6.5pt;font-weight:700;letter-spacing:.18em;text-transform:uppercase;margin-bottom:8px}");
        sb.append("h1{font-family:'Fraunces',Georgia,serif;font-size:16pt;line-height:1.08;margin:0 0 6px}");
        sb.append(".meta{font-size:9pt;color:#dff2ee}");
        sb.append("main{padding:8pt 12mm 10pt}");
        sb.append(".sec-head{font-size:9.5pt;font-weight:700;color:").append(TEAL_DEEP).append(";margin:10pt 0 5pt;border-bottom:1px solid ")
                .append(BORDER).append(";padding-bottom:3px}");
        sb.append(".snap{font-size:11pt;margin:0 0 8pt;color:").append(DARK).append("}");
        sb.append(".list{margin:0 0 8pt;padding-left:18px}.list li{margin:0 0 4px}");
        sb.append(".sec{width:100%;border-collapse:collapse;margin:0 0 8pt;border:1px solid ").append(BORDER)
                .append(";border-left:3px solid ").append(TEAL_MID).append(";page-break-inside:avoid}");
        sb.append(".cap{background:").append(BG_SUBTLE).append(";color:").append(TEAL_DEEP)
                .append(";font-size:8pt;font-weight:700;letter-spacing:.06em;text-transform:uppercase;padding:5px 8px;border-bottom:1px solid ")
                .append(BORDER).append(";text-align:left}");
        sb.append(".h{background:").append(TEAL_LIGHT).append(";color:").append(TEAL_DEEP)
                .append(";font-size:8pt;font-weight:700;padding:4px 7px;text-align:left}");
        sb.append(".c{padding:4px 7px;border-top:1px solid ").append(BORDER).append(";vertical-align:top;color:")
                .append(DARK).append(";font-size:10.5pt;white-space:pre-wrap;word-break:break-word;overflow-wrap:anywhere}");
        sb.append(".vac-name{width:32%;font-weight:600}.vac-status{width:24%}.vac-action{width:44%}");
        sb.append(".s5block{font-size:10.5pt;margin:0 0 6px;color:").append(DARK).append("}");
        sb.append(".s5label{font-weight:700;color:").append(TEAL_DEEP).append(";font-size:8.5pt;text-transform:uppercase;margin-top:6px}");
        sb.append(".closing{margin-top:12pt;font-size:10.5pt;font-weight:600;color:").append(TEAL_DEEP).append(";line-height:1.35}");
        sb.append(".alert5{background:").append(RED_SOFT).append(";padding:6px 8px;border-radius:3px;margin-bottom:6px}");
        sb.append("</style>");
    }

    private void appendHero(StringBuilder sb, TravelPlan plan) {
        sb.append("<header class=\"hero\"><div class=\"hero-inner\"><div class=\"brand\">Travel Medicine Advisory Global</div>");
        sb.append("<h1>Travel health Action Sheet</h1>");
        sb.append("<div class=\"meta\">").append(escape(nullSafe(plan.getCountry())));
        if (StringUtils.hasText(plan.getDestination())) {
            sb.append(SEPARATOR).append(escape(plan.getDestination()));
        }
        if (plan.getDuration() != null) {
            sb.append(SEPARATOR).append(plan.getDuration()).append(" days");
        }
        if (StringUtils.hasText(plan.getPurpose())) {
            sb.append(SEPARATOR).append(escape(plan.getPurpose()));
        }
        if (plan.getCreatedAt() != null) {
            sb.append("<br/>").append(escape(plan.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM d, yyyy"))));
        }
        sb.append("</div></div></header>");
    }

    private void appendSection(StringBuilder sb, String heading, JsonNode items, int maxBullets) {
        sb.append("<h2 class=\"sec-head\">").append(escape(heading)).append("</h2>");
        if (!items.isArray() || items.isEmpty()) {
            sb.append("<p class=\"snap\">").append(escape("Not specified in dossier.")).append("</p>");
            return;
        }
        sb.append("<ul class=\"list\">");
        int n = 0;
        for (Iterator<JsonNode> it = items.elements(); it.hasNext() && n < maxBullets; n++) {
            JsonNode item = it.next();
            if (item.isTextual() && StringUtils.hasText(item.asText())) {
                sb.append("<li>").append(escape(item.asText())).append("</li>");
            }
        }
        sb.append("</ul>");
    }

    private void appendSectionOneLine(StringBuilder sb, String heading, String line) {
        sb.append("<h2 class=\"sec-head\">").append(escape(heading)).append("</h2>");
        if (!StringUtils.hasText(line)) {
            sb.append("<p class=\"snap\">").append(escape("Not specified in dossier.")).append("</p>");
        } else {
            sb.append("<p class=\"snap\">").append(escape(line)).append("</p>");
        }
    }

    private void appendVaccineTable(StringBuilder sb, JsonNode items) {
        sb.append("<h2 class=\"sec-head\">").append(escape(SECTION_3_HEADING)).append("</h2>");
        if (!items.isArray() || items.isEmpty()) {
            sb.append("<p class=\"snap\">").append(escape("No qualifying vaccines listed in dossier.")).append("</p>");
            return;
        }
        sb.append("<table class=\"sec\"><tr><th class=\"h vac-name\">Vaccine</th><th class=\"h vac-status\">Status</th><th class=\"h vac-action\">Action</th></tr>");
        for (JsonNode item : items) {
            if (!item.isObject()) {
                continue;
            }
            sb.append("<tr><td class=\"c vac-name\">").append(escape(item.path("vaccine").asText("")))
                    .append("</td><td class=\"c vac-status\">").append(escape(item.path("status").asText("")))
                    .append("</td><td class=\"c vac-action\">").append(escape(item.path("action").asText("")))
                    .append("</td></tr>");
        }
        sb.append("</table>");
    }

    private void appendSection5(StringBuilder sb, JsonNode s5) {
        sb.append("<h2 class=\"sec-head\">").append(escape(SECTION_5_HEADING)).append("</h2>");
        if (s5 == null || !s5.isObject()) {
            sb.append("<p class=\"snap\">").append(escape("Not specified in dossier.")).append("</p>");
            return;
        }
        String red = text(s5, "redFlagsLine");
        if (StringUtils.hasText(red)) {
            sb.append("<div class=\"s5block alert5\">").append(escape(red)).append("</div>");
        }
        JsonNode fac = s5.path("facilities");
        if (fac.isArray() && !fac.isEmpty()) {
            sb.append("<p class=\"s5label\">Clinical facilities</p>");
            for (JsonNode f : fac) {
                if (!f.isObject()) {
                    continue;
                }
                String name = f.path("name").asText("");
                String loc = f.path("location").asText("");
                String line = StringUtils.hasText(name) ? name + (StringUtils.hasText(loc) ? ", " + loc : "") : loc;
                if (StringUtils.hasText(line)) {
                    sb.append("<p class=\"s5block\">").append(escape(line)).append("</p>");
                }
            }
        }
        String em = text(s5, "localEmergencyNumber");
        if (StringUtils.hasText(em)) {
            sb.append("<p class=\"s5label\">Local emergency number</p>");
            sb.append("<p class=\"s5block\">").append(escape(em)).append("</p>");
        }
        String ins = text(s5, "insuranceEmergencyLine");
        if (StringUtils.hasText(ins)) {
            sb.append("<p class=\"s5label\">Insurance</p>");
            sb.append("<p class=\"s5block\">").append(escape(ins)).append("</p>");
        }
    }

    private JsonNode fallbackActionSheet(TravelPlan plan, JsonNode source) {
        ObjectNode root = objectMapper.createObjectNode();
        ArrayNode s1 = root.putArray("section1CriticalBeforeDeparture");
        copyNextSteps(s1, source != null ? source.path("nextSteps") : null, 4);
        if (s1.isEmpty()) {
            copyTextToSection1(s1, source != null ? source.path("clinicalFlags") : null, 4);
        }

        root.put("section2TripSnapshot", buildFallbackTripSnapshot(plan, source));

        ArrayNode vax = root.putArray("section3Vaccines");
        copyVaccinesForActionSheet(vax, source != null ? source.path("vaccinations") : null);

        ArrayNode s4 = root.putArray("section4PackAndRoutine");
        appendMalariaBullets(s4, source != null ? source.path("malariaPrevention") : null);
        copyRecommendationTitles(s4, source != null ? source.path("recommendations") : null, 8 - s4.size());
        trimArray(s4, 8);

        ObjectNode s5 = root.putObject("section5");
        JsonNode afterReturn = source != null ? source.path("afterReturn") : null;
        if (afterReturn != null && afterReturn.path("redFlag").isTextual()) {
            s5.put("redFlagsLine", cleanActionSheetText(afterReturn.path("redFlag").asText("")));
        } else {
            s5.put("redFlagsLine", "");
        }
        ArrayNode fac = s5.putArray("facilities");
        copyClinics(fac, source != null ? source.path("medicalCare").path("clinics") : null, 2);
        s5.put("localEmergencyNumber", findEmergencyNumber(source));
        s5.put("insuranceEmergencyLine", findInsuranceLine(source));
        root.put("closingLine", CLOSING_LINE_EXACT);
        return normalizeActionSheet(root);
    }

    private void copyNextSteps(ArrayNode target, JsonNode steps, int max) {
        if (steps == null || !steps.isArray()) {
            return;
        }
        for (JsonNode item : steps) {
            if (target.size() >= max) {
                return;
            }
            if (item.isTextual()) {
                String t = cleanActionSheetText(item.asText(""));
                if (StringUtils.hasText(t) && !isInternalKey(t)) {
                    target.add(t);
                }
            }
        }
    }

    private void copyTextToSection1(ArrayNode target, JsonNode items, int max) {
        if (items == null || !items.isArray()) {
            return;
        }
        for (JsonNode item : items) {
            if (target.size() >= max) {
                return;
            }
            if (item.isTextual()) {
                String t = cleanActionSheetText(item.asText(""));
                if (StringUtils.hasText(t) && !isInternalKey(t)) {
                    target.add(t);
                }
            }
        }
    }

    private String buildFallbackTripSnapshot(TravelPlan plan, JsonNode source) {
        String travelling = "";
        if (source != null) {
            JsonNode glance = source.path("tripAtGlance");
            if (glance.isObject() && glance.path("travelling").isTextual()) {
                travelling = cleanActionSheetText(glance.path("travelling").asText(""));
            }
        }
        String dest = StringUtils.hasText(plan.getDestination()) ? plan.getDestination() : text(source, "destination");
        String origin = StringUtils.hasText(plan.getCountry()) ? plan.getCountry() : "";
        String durationPart = plan.getDuration() != null ? plan.getDuration() + " days" : "";
        String purpose = nullSafe(plan.getPurpose());
        String risk = source != null ? cleanActionSheetText(source.path("overallRiskLevel").asText("")) : "";
        if (!StringUtils.hasText(risk)) {
            risk = riskFromScore(plan.getRiskScore());
        }
        if (StringUtils.hasText(travelling)) {
            String suffix = "";
            if (StringUtils.hasText(durationPart)) {
                suffix += ", " + durationPart;
            }
            if (StringUtils.hasText(purpose)) {
                suffix += ", " + purpose;
            }
            if (StringUtils.hasText(risk)) {
                suffix += ", overall risk " + risk;
            }
            return cleanActionSheetText(travelling + suffix);
        }
        StringBuilder sb = new StringBuilder();
        if (StringUtils.hasText(origin)) {
            sb.append(origin).append(" to ").append(dest);
        } else {
            sb.append(dest);
        }
        if (StringUtils.hasText(durationPart)) {
            sb.append(", ").append(durationPart);
        }
        if (StringUtils.hasText(purpose)) {
            sb.append(", ").append(purpose);
        }
        if (StringUtils.hasText(risk)) {
            sb.append(", overall risk ").append(risk);
        }
        return cleanActionSheetText(sb.toString());
    }

    private void copyVaccinesForActionSheet(ArrayNode target, JsonNode items) {
        if (items == null || !items.isArray()) {
            return;
        }
        for (JsonNode item : items) {
            if (!item.isObject()) {
                continue;
            }
            String status = item.path("status").asText("");
            if (!includeVaccineStatusForTable(status)) {
                continue;
            }
            ObjectNode row = target.addObject();
            row.put("vaccine", cleanActionSheetText(item.path("vaccine").asText("")));
            row.put("status", cleanActionSheetText(status));
            String action = item.path("action").asText("");
            if (!StringUtils.hasText(action)) {
                action = item.path("recommendation").asText("");
            }
            row.put("action", cleanActionSheetText(action));
        }
    }

    private boolean includeVaccineStatusForTable(String status) {
        if (!StringUtils.hasText(status)) {
            return true;
        }
        String u = status.toUpperCase();
        if (u.contains("NOT INDICATED") || u.contains("NOT APPLICABLE") || u.contains("NOT REQUIRED")
                || u.contains("NOT RECOMMENDED")) {
            return false;
        }
        boolean required = u.contains("REQUIRED");
        boolean strongly = u.contains("STRONGLY");
        boolean recommended = u.contains("RECOMMENDED");
        if (u.contains("CONSIDER") && !required && !strongly && !recommended) {
            return false;
        }
        return required || strongly || recommended;
    }

    private void appendMalariaBullets(ArrayNode target, JsonNode malaria) {
        if (malaria == null || !malaria.isObject() || malaria.isNull()) {
            return;
        }
        String agent = malaria.path("recommendedAgent").asText("");
        if (!StringUtils.hasText(agent)) {
            return;
        }
        StringBuilder line = new StringBuilder();
        line.append("Malaria prophylaxis: ").append(cleanActionSheetText(agent));
        String rationale = malaria.path("rationale").asText("");
        if (StringUtils.hasText(rationale) && target.size() < 8) {
            line.append(". ").append(cleanActionSheetText(rationale));
        }
        target.add(cleanActionSheetText(line.toString()));
    }

    private void copyRecommendationTitles(ArrayNode target, JsonNode items, int maxAdd) {
        if (items == null || !items.isArray() || maxAdd <= 0) {
            return;
        }
        for (JsonNode item : items) {
            if (target.size() >= 8) {
                return;
            }
            if (!item.isObject()) {
                continue;
            }
            String title = cleanActionSheetText(item.path("title").asText(""));
            String details = cleanActionSheetText(item.path("details").asText(""));
            if (!StringUtils.hasText(title) && !StringUtils.hasText(details)) {
                continue;
            }
            String line = StringUtils.hasText(details) ? title + ": " + details : title;
            target.add(cleanActionSheetText(line));
        }
    }

    private void trimArray(ArrayNode arr, int max) {
        while (arr.size() > max) {
            arr.remove(arr.size() - 1);
        }
    }

    private void copyClinics(ArrayNode target, JsonNode clinics, int max) {
        if (clinics == null || !clinics.isArray()) {
            return;
        }
        for (JsonNode c : clinics) {
            if (target.size() >= max) {
                return;
            }
            if (!c.isObject()) {
                continue;
            }
            String name = cleanActionSheetText(c.path("name").asText(""));
            String address = cleanActionSheetText(c.path("address").asText(""));
            if (!StringUtils.hasText(name) && !StringUtils.hasText(address)) {
                continue;
            }
            ObjectNode row = target.addObject();
            row.put("name", name);
            row.put("location", address);
        }
    }

    private String findEmergencyNumber(JsonNode source) {
        if (source == null) {
            return "";
        }
        JsonNode contacts = source.path("medicalCare").path("emergencyContacts");
        if (!contacts.isArray()) {
            return "";
        }
        for (JsonNode c : contacts) {
            if (!c.isObject()) {
                continue;
            }
            String label = c.path("label").asText("").toLowerCase();
            String value = c.path("value").asText("");
            if ((label.contains("emergency") && label.contains("number")) || label.equals("local emergency")) {
                return cleanActionSheetText(value);
            }
        }
        for (JsonNode c : contacts) {
            if (!c.isObject()) {
                continue;
            }
            String label = c.path("label").asText("").toLowerCase();
            if (label.contains("ambulance") || label.contains("police") || label.contains("112")
                    || label.contains("999") || label.contains("911")) {
                return cleanActionSheetText(c.path("value").asText(""));
            }
        }
        return "";
    }

    private String findInsuranceLine(JsonNode source) {
        if (source == null) {
            return "";
        }
        JsonNode glance = source.path("tripAtGlance");
        if (glance.isObject() && glance.path("insurance").isTextual()) {
            String ins = cleanActionSheetText(glance.path("insurance").asText(""));
            if (StringUtils.hasText(ins)) {
                return "Insurance: " + ins;
            }
        }
        JsonNode contacts = source.path("medicalCare").path("emergencyContacts");
        if (contacts.isArray()) {
            for (JsonNode c : contacts) {
                if (!c.isObject()) {
                    continue;
                }
                String label = c.path("label").asText("").toLowerCase();
                if (label.contains("insurance")) {
                    return cleanActionSheetText(c.path("value").asText(""));
                }
            }
        }
        return "";
    }

    private JsonNode parsePlanJson(GeneratedPlan generatedPlan) {
        if (generatedPlan == null || !StringUtils.hasText(generatedPlan.getPlanJson())) {
            return null;
        }
        try {
            return objectMapper.readTree(generatedPlan.getPlanJson());
        } catch (Exception ex) {
            return null;
        }
    }

    private void registerFonts(PdfRendererBuilder builder) {
        useFont(builder, "fonts/HankenGrotesk-Regular.ttf", "Hanken Grotesk", 400);
        useFont(builder, "fonts/HankenGrotesk-SemiBold.ttf", "Hanken Grotesk", 600);
        useFont(builder, "fonts/Fraunces-Bold.ttf", "Fraunces", 700);
    }

    private void useFont(PdfRendererBuilder builder, String path, String family, int weight) {
        try {
            File file = new ClassPathResource(path).getFile();
            builder.useFont(file, family, weight, PdfRendererBuilder.FontStyle.NORMAL, true);
        } catch (Exception ex) {
            log.warn("Could not load PDF font {} from classpath path {}; using fallback fonts: {}",
                    family,
                    path,
                    ex.getMessage());
        }
    }

    private String stripJsonFences(String content) {
        if (!StringUtils.hasText(content)) {
            return "{}";
        }
        String trimmed = content.trim();
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.replaceFirst("^```(?:json)?\\s*", "");
            trimmed = trimmed.replaceFirst("\\s*```$", "");
        }
        return trimmed;
    }

    private String cleanActionSheetText(String value) {
        if (!StringUtils.hasText(value)) {
            return value == null ? "" : "";
        }
        String t = value.trim().replaceAll("\\s+", " ");
        t = t.replace('-', ' ')
                .replace('_', ' ')
                .replace('\u2013', ' ')
                .replace('\u2014', ' ');
        return t.trim().replaceAll("\\s+", " ");
    }

    private String text(JsonNode node, String field) {
        if (node != null && node.path(field).isTextual()) {
            return node.path(field).asText("");
        }
        return "";
    }

    private String riskFromScore(Integer riskScore) {
        if (riskScore == null) {
            return "UNKNOWN";
        }
        if (riskScore >= 70) {
            return "HIGH";
        }
        if (riskScore >= 40) {
            return "MEDIUM";
        }
        return "LOW";
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }

    private String escape(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
