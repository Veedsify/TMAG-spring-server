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

    private static final String TEAL_DEEP = "#0d3d35";
    private static final String TEAL_MID = "#1a7a6a";
    private static final String TEAL_LIGHT = "#dff2ee";
    private static final String BG_PAGE = "#f8f5f1";
    private static final String BG_SUBTLE = "#f2ede7";
    private static final String DARK = "#1a1208";
    private static final String BODY = "#3a2e22";
    private static final String MUTED = "#7a6a5a";
    private static final String BORDER = "#ddd5cb";
    private static final String GOLD = "#b8892e";
    private static final String GOLD_SOFT = "#fef6e8";
    private static final String GOLD_BORDER = "#e8d0a0";
    private static final String RED = "#b91c1c";
    private static final String RED_SOFT = "#fef2f2";
    private static final String GREEN = "#065f46";
    private static final String GREEN_SOFT = "#d1fae5";
    private static final String SEPARATOR = " &#183; ";

    private static final String DISCLAIMER = "This travel health summary was generated with artificial intelligence from the full advisory plan and is provided for quick reference only. It does not replace consultation with a certified travel medicine doctor or licensed medical doctor. Seek urgent medical care for severe symptoms during or after travel.";

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

    private JsonNode summarize(TravelPlan plan, GeneratedPlan generatedPlan) {
        JsonNode source = parsePlanJson(generatedPlan);
        if (source == null || !source.isObject()) {
            return fallbackSummary(plan, source);
        }

        String systemPrompt = """
                You are a travel-medicine clinician writing a NEW executive summary for a patient-facing PDF.
                Return only valid JSON. No markdown fences.
                Do NOT copy sentences from the source. Synthesize and rewrite in your own words.
                Preserve clinically important warnings, contraindications, and referral advice.
                Do not invent facts absent from the source.
                The PDF must fit on two A4 pages — brevity is a hard requirement.
                IMPORTANT: Skip any value matching the pattern TREE_<digits>_<UPPERCASE> entirely.

                STRICT character and word limits per field (stay under; do not truncate with "..."):
                - topRisks[].topic   : ≤ 32 characters, ≤ 5 words
                - topRisks[].action  : ≤ 140 characters, ≤ 22 words
                - vaccines[].name    : ≤ 32 characters, ≤ 5 words
                - vaccines[].action  : ≤ 120 characters, ≤ 18 words
                - medications[].name : ≤ 32 characters, ≤ 5 words
                - medications[].action : ≤ 120 characters, ≤ 18 words
                - urgentFlags[]      : ≤ 120 characters, ≤ 18 words per item
                - afterReturn[]      : ≤ 120 characters, ≤ 18 words per item
                - emergency[]        : ≤ 100 characters, ≤ 15 words per item
                Write to fit these limits naturally — do not pad and do not cut off mid-sentence.
                """;
        String userPrompt = """
                Create a compact travel-health summary from the curated source below.

                Required writing style:
                - Write each action as a traveller instruction: prefer "Book...", "Confirm...", "Carry...", "Seek care if..."
                - Select only the highest clinical priority items when the source is detailed.
                - Plain language; keep medical meaning precise.
                - Every string must fit within the character and word limits stated in the system prompt.

                Return exactly this JSON shape:
                {
                  "travellerName": "string or empty",
                  "travelDates": "string or empty",
                  "overallRisk": "low|medium|high|unknown",
                  "topRisks": [{"topic":"string","level":"low|medium|high|unknown","action":"string"}],
                  "vaccines": [{"name":"string","action":"string"}],
                  "medications": [{"name":"string","action":"string"}],
                  "urgentFlags": ["string"],
                  "afterReturn": ["string"],
                  "emergency": ["string"]
                }

                Array size limits (hard): topRisks ≤ 4, vaccines ≤ 4, medications ≤ 3,
                urgentFlags ≤ 4, afterReturn ≤ 3, emergency ≤ 3.

                Curated source:
                """ + createSummarySource(plan, source).toString();

        try {
            AiGenerationResult result = aiGenerationClient.generateSummary(systemPrompt, userPrompt);
            JsonNode summary = normalizeSummary(objectMapper.readTree(stripJsonFences(result.content())));
            if (hasUsableSummary(summary)) {
                log.info("Generated travel plan summary PDF content with {} model {} for travelPlanId={} generatedPlanId={} tokens={}",
                        result.provider(),
                        result.model(),
                        plan.getId(),
                        generatedPlan.getId(),
                        result.estimatedTokens());
                return summary;
            }
            log.warn("Summary model returned unusable JSON for travelPlanId={}; using fallback summary",
                    plan.getId());
        } catch (Exception ex) {
            log.warn("Summary model unavailable for travelPlanId={}; using fallback summary: {}",
                    plan.getId(),
                    ex.getMessage());
        }
        return fallbackSummary(plan, source);
    }

    private ObjectNode createSummarySource(TravelPlan plan, JsonNode source) {
        ObjectNode root = objectMapper.createObjectNode();
        ObjectNode trip = root.putObject("trip");
        trip.put("destination", nullSafe(plan.getDestination()));
        trip.put("country", nullSafe(plan.getCountry()));
        trip.put("durationDays", plan.getDuration());
        trip.put("purpose", nullSafe(plan.getPurpose()));
        trip.put("riskScore", plan.getRiskScore());
        trip.put("travellerName", text(source, "travellerName"));
        trip.put("travelDates", text(source, "travelDates"));

        putIfPresent(root, "healthRiskOverview", source.path("healthRiskOverview"));
        putIfPresent(root, "vaccinations", source.path("vaccinations"));
        putIfPresent(root, "malariaPrevention", source.path("malariaPrevention"));
        putIfPresent(root, "medications", source.path("medications"));
        putIfPresent(root, "recommendations", source.path("recommendations"));
        putIfPresent(root, "clinicalFlags", source.path("clinicalFlags"));
        putIfPresent(root, "contraindications", source.path("contraindications"));
        putIfPresent(root, "afterReturn", source.path("afterReturn"));
        putIfPresent(root, "medicalCare", source.path("medicalCare"));
        putIfPresent(root, "emergencyPlan", source.path("emergencyPlan"));
        putIfPresent(root, "doctorReferral", source.path("doctorReferral"));
        putIfPresent(root, "flightHealth", source.path("flightHealth"));
        putIfPresent(root, "medicalConditions", source.path("medicalConditions"));
        putIfPresent(root, "medicationLogistics", source.path("medicationLogistics"));
        putIfPresent(root, "specialistReferrals", source.path("specialistReferrals"));
        putIfPresent(root, "sexualHealth", source.path("sexualHealth"));
        putIfPresent(root, "pregnancyGuidance", source.path("pregnancyGuidance"));
        putIfPresent(root, "nextSteps", source.path("nextSteps"));
        return root;
    }

    private void putIfPresent(ObjectNode root, String field, JsonNode value) {
        if (value != null && !value.isMissingNode() && !value.isNull()) {
            root.set(field, value);
        }
    }

    private JsonNode normalizeSummary(JsonNode summary) {
        if (summary == null || !summary.isObject()) {
            return objectMapper.createObjectNode();
        }

        ObjectNode root = (ObjectNode) summary.deepCopy();
        normalizeTextField(root, "travellerName", 80);
        normalizeTextField(root, "travelDates", 80);
        root.put("overallRisk", levelClass(text(root, "overallRisk"), null));
        normalizeObjectArray(root, "topRisks", 4, 32, 140);
        normalizeObjectArray(root, "vaccines", 4, 32, 120);
        normalizeObjectArray(root, "medications", 3, 32, 120);
        normalizeTextArray(root, "urgentFlags", 4, 120);
        normalizeTextArray(root, "afterReturn", 3, 120);
        normalizeTextArray(root, "emergency", 3, 100);
        return root;
    }

    private boolean hasUsableSummary(JsonNode summary) {
        return summary != null
                && summary.isObject()
                && (hasItems(summary.path("topRisks"))
                        || hasItems(summary.path("vaccines"))
                        || hasItems(summary.path("medications"))
                        || hasItems(summary.path("urgentFlags"))
                        || hasItems(summary.path("afterReturn"))
                        || hasItems(summary.path("emergency")));
    }

    private boolean hasItems(JsonNode node) {
        return node != null && node.isArray() && !node.isEmpty();
    }

    private void normalizeTextField(ObjectNode root, String field, int maxChars) {
        root.put(field, truncateClean(text(root, field), maxChars));
    }

    private void normalizeObjectArray(ObjectNode root, String field, int maxItems, int labelMaxChars, int actionMaxChars) {
        JsonNode source = root.path(field);
        ArrayNode normalized = objectMapper.createArrayNode();
        if (source.isArray()) {
            for (JsonNode item : source) {
                if (normalized.size() >= maxItems) {
                    break;
                }
                if (!item.isObject()) {
                    continue;
                }
                ObjectNode row = normalized.addObject();
                if ("topRisks".equals(field)) {
                    row.put("topic", truncateClean(item.path("topic").asText(""), labelMaxChars));
                    row.put("level", levelClass(item.path("level").asText(""), null));
                    row.put("action", fitToLimit(item.path("action").asText("").trim(), actionMaxChars));
                } else {
                    row.put("name", truncateClean(item.path("name").asText(""), labelMaxChars));
                    row.put("action", fitToLimit(item.path("action").asText("").trim(), actionMaxChars));
                }
            }
        }
        root.set(field, normalized);
    }

    private void normalizeTextArray(ObjectNode root, String field, int maxItems, int maxChars) {
        JsonNode source = root.path(field);
        ArrayNode normalized = objectMapper.createArrayNode();
        if (source.isArray()) {
            for (JsonNode item : source) {
                if (normalized.size() >= maxItems) {
                    break;
                }
                String text = item.isTextual() ? item.asText("").trim() : "";
                if (StringUtils.hasText(text) && !isInternalKey(text) && text.length() <= maxChars) {
                    normalized.add(text);
                }
            }
        }
        root.set(field, normalized);
    }

    private boolean isInternalKey(String value) {
        return value != null && value.matches("TREE_\\d+_[A-Z_]+");
    }

    private String buildHtml(TravelPlan plan, JsonNode summary) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\"/>");
        appendStyles(sb);
        sb.append("</head><body>");
        appendHero(sb, plan, summary);
        sb.append("<main>");
        appendRiskGrid(sb, summary.path("topRisks"));
        appendTwoColumnList(sb, "Vaccination actions", summary.path("vaccines"), "name", "action");
        appendTwoColumnList(sb, "Medication and prevention", summary.path("medications"), "name", "action");
        appendBullets(sb, "Urgent clinical flags", summary.path("urgentFlags"), true);
        appendBullets(sb, "After return", summary.path("afterReturn"), false);
        appendBullets(sb, "Emergency contacts", summary.path("emergency"), false);
        sb.append("<section class=\"disclaimer\">").append(escape(DISCLAIMER)).append("</section>");
        sb.append("</main></body></html>");
        return sb.toString();
    }

    private void appendStyles(StringBuilder sb) {
        sb.append("<style>");
        sb.append("@page{size:A4;margin:0 0 12mm 0}");
        sb.append("@page{@bottom-left{content:'Travel Medicine Advisory Global';font-family:'Hanken Grotesk',Arial,sans-serif;font-size:7pt;color:#9a8a7a;padding-left:14mm}}");
        sb.append("@page{@bottom-right{content:'Page ' counter(page);font-family:'Hanken Grotesk',Arial,sans-serif;font-size:7pt;color:#9a8a7a;padding-right:14mm}}");
        sb.append("*{box-sizing:border-box}body{margin:0;background:").append(BG_PAGE).append(";color:").append(BODY)
                .append(";font-family:'Hanken Grotesk',Arial,sans-serif;font-size:8.4pt;line-height:1.32}");
        sb.append(".hero{background:").append(TEAL_DEEP).append(";color:white;padding:18px 14mm 14px;border-bottom:3px solid ")
                .append(TEAL_MID).append("}");
        sb.append(".brand{font-size:6.5pt;font-weight:700;letter-spacing:.18em;text-transform:uppercase;margin-bottom:10px}");
        sb.append("h1{font-family:'Fraunces',Georgia,serif;font-size:18pt;line-height:1.08;margin:0 0 7px}");
        sb.append(".meta{font-size:8pt;color:#dff2ee}.badges{margin-top:9px}.badge{display:inline-block;border-radius:3px;padding:4px 8px;margin-right:5px;font-size:7pt;font-weight:700;text-transform:uppercase}");
        sb.append(".low{background:").append(GREEN_SOFT).append(";color:").append(GREEN).append("}.medium{background:")
                .append(GOLD_SOFT).append(";color:").append(GOLD).append("}.high{background:").append(RED_SOFT)
                .append(";color:").append(RED).append("}.unknown{background:white;color:#333}");
        sb.append("main{padding:10pt 14mm 0}.sec{width:100%;border-collapse:collapse;margin:0 0 8pt;border:1px solid ")
                .append(BORDER).append(";border-left:3px solid ").append(TEAL_MID).append(";page-break-inside:avoid}");
        sb.append(".cap{background:").append(BG_SUBTLE).append(";color:").append(TEAL_DEEP)
                .append(";font-size:7.2pt;font-weight:700;letter-spacing:.08em;text-transform:uppercase;padding:6px 9px;border-bottom:1px solid ")
                .append(BORDER).append(";text-align:left}");
        sb.append(".h{background:").append(TEAL_LIGHT).append(";color:").append(TEAL_DEEP)
                .append(";font-size:7pt;font-weight:700;text-transform:uppercase;padding:5px 8px;text-align:left}");
        sb.append(".c{padding:5px 8px;border-top:1px solid ").append(BORDER).append(";vertical-align:top;color:")
                .append(DARK).append(";white-space:pre-wrap}.topic{width:28%;font-weight:700}.level{width:16%;font-weight:700;text-transform:uppercase}");
        sb.append(".bull{padding:5px 8px 5px 21px;border-top:1px solid ").append(BORDER)
                .append(";position:relative;color:").append(DARK).append("}.bull:before{content:'\\2022';position:absolute;left:9px;color:")
                .append(TEAL_MID).append(";font-weight:700}");
        sb.append(".alert .bull{background:").append(RED_SOFT).append("}.disclaimer{font-size:7.3pt;color:")
                .append(MUTED).append(";padding:8px 10px;background:").append(GOLD_SOFT).append(";border:1px solid ")
                .append(GOLD_BORDER).append(";border-left:3px solid ").append(GOLD).append(";line-height:1.35}");
        sb.append("</style>");
    }

    private void appendHero(StringBuilder sb, TravelPlan plan, JsonNode summary) {
        String title = StringUtils.hasText(plan.getDestination())
                ? "Travel health summary: " + plan.getDestination()
                : "Travel health summary";
        sb.append("<header class=\"hero\"><div class=\"brand\">Travel Medicine Advisory Global</div>");
        sb.append("<h1>").append(escape(title)).append("</h1>");
        sb.append("<div class=\"meta\">").append(escape(nullSafe(plan.getCountry())));
        if (plan.getDuration() != null) {
            sb.append(SEPARATOR).append(plan.getDuration()).append(" days");
        }
        if (StringUtils.hasText(plan.getPurpose())) {
            sb.append(SEPARATOR).append(escape(plan.getPurpose()));
        }
        String dates = text(summary, "travelDates");
        if (StringUtils.hasText(dates)) {
            sb.append("<br/>").append(escape(dates));
        }
        String traveller = text(summary, "travellerName");
        if (StringUtils.hasText(traveller)) {
            sb.append("<br/>Traveller: ").append(escape(traveller));
        }
        sb.append("</div><div class=\"badges\"><span class=\"badge ")
                .append(levelClass(text(summary, "overallRisk"), plan.getRiskScore())).append("\">")
                .append(escape(riskLabel(text(summary, "overallRisk"), plan.getRiskScore()))).append(" risk</span>");
        if (plan.getCreatedAt() != null) {
            sb.append("<span class=\"badge unknown\">")
                    .append(plan.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM d, yyyy")))
                    .append("</span>");
        }
        sb.append("</div></header>");
    }

    private void appendRiskGrid(StringBuilder sb, JsonNode items) {
        if (!items.isArray() || items.isEmpty()) {
            return;
        }
        sb.append("<table class=\"sec\"><tr><th class=\"cap\" colspan=\"3\">Highest priority risks</th></tr>");
        sb.append("<tr><th class=\"h\">Topic</th><th class=\"h\">Level</th><th class=\"h\">Action</th></tr>");
        int count = 0;
        for (Iterator<JsonNode> it = items.elements(); it.hasNext() && count < 4; count++) {
            JsonNode item = it.next();
            sb.append("<tr><td class=\"c topic\">").append(escape(item.path("topic").asText("")))
                    .append("</td><td class=\"c level ").append(levelClass(item.path("level").asText(""), null))
                    .append("\">").append(escape(item.path("level").asText("unknown")))
                    .append("</td><td class=\"c\">").append(escape(item.path("action").asText(""))).append("</td></tr>");
        }
        sb.append("</table>");
    }

    private void appendTwoColumnList(StringBuilder sb, String title, JsonNode items, String labelField, String valueField) {
        if (!items.isArray() || items.isEmpty()) {
            return;
        }
        sb.append("<table class=\"sec\"><tr><th class=\"cap\" colspan=\"2\">").append(escape(title)).append("</th></tr>");
        int count = 0;
        for (Iterator<JsonNode> it = items.elements(); it.hasNext() && count < 4; count++) {
            JsonNode item = it.next();
            sb.append("<tr><td class=\"c topic\">").append(escape(item.path(labelField).asText("")))
                    .append("</td><td class=\"c\">").append(escape(item.path(valueField).asText(""))).append("</td></tr>");
        }
        sb.append("</table>");
    }

    private void appendBullets(StringBuilder sb, String title, JsonNode items, boolean alert) {
        if (!items.isArray() || items.isEmpty()) {
            return;
        }
        sb.append("<table class=\"sec").append(alert ? " alert" : "").append("\"><tr><th class=\"cap\">")
                .append(escape(title)).append("</th></tr>");
        int count = 0;
        for (Iterator<JsonNode> it = items.elements(); it.hasNext() && count < 4; count++) {
            JsonNode item = it.next();
            if (item.isTextual() && StringUtils.hasText(item.asText())) {
                sb.append("<tr><td class=\"bull\">").append(escape(item.asText())).append("</td></tr>");
            }
        }
        sb.append("</table>");
    }

    private JsonNode fallbackSummary(TravelPlan plan, JsonNode source) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("travellerName", text(source, "travellerName"));
        root.put("travelDates", text(source, "travelDates"));
        root.put("overallRisk", riskFromScore(plan.getRiskScore()));
        ArrayNode topRisks = root.putArray("topRisks");
        copyRiskItems(topRisks, source != null ? source.path("healthRiskOverview") : null);
        copyRecommendations(root.putArray("vaccines"), source != null ? source.path("vaccinations") : null, "vaccine",
                "recommendation");
        copyRecommendations(root.putArray("medications"), source != null ? source.path("recommendations") : null,
                "title", "details");
        copyText(root.putArray("urgentFlags"), source != null ? source.path("clinicalFlags") : null, 4);
        JsonNode afterReturn = source != null ? source.path("afterReturn") : null;
        ArrayNode after = root.putArray("afterReturn");
        if (afterReturn != null && afterReturn.path("redFlag").isTextual()) {
            String fitted = fitToLimit(afterReturn.path("redFlag").asText().trim(), 120);
            if (StringUtils.hasText(fitted)) {
                after.add(fitted);
            }
        }
        copyText(after, afterReturn != null ? afterReturn.path("within1Week") : null, 2);
        copyEmergency(root.putArray("emergency"), source != null ? source.path("medicalCare").path("emergencyContacts") : null);
        return root;
    }

    private void copyRiskItems(ArrayNode target, JsonNode items) {
        if (items == null || !items.isArray()) {
            return;
        }
        int count = 0;
        for (JsonNode item : items) {
            if (count++ >= 4) {
                return;
            }
            ObjectNode row = target.addObject();
            row.put("topic", truncateClean(item.path("category").asText(""), 32));
            row.put("level", item.path("level").asText("unknown"));
            row.put("action", fitToLimit(item.path("summary").asText("").trim(), 140));
        }
    }

    private void copyRecommendations(ArrayNode target, JsonNode items, String labelField, String actionField) {
        if (items == null || !items.isArray()) {
            return;
        }
        int count = 0;
        for (JsonNode item : items) {
            if (count++ >= 4) {
                return;
            }
            ObjectNode row = target.addObject();
            row.put("name", truncateClean(item.path(labelField).asText(""), 32));
            row.put("action", fitToLimit(item.path(actionField).asText("").trim(), 120));
        }
    }

    private void copyText(ArrayNode target, JsonNode items, int max) {
        if (items == null || !items.isArray()) {
            return;
        }
        for (JsonNode item : items) {
            if (target.size() >= max) {
                return;
            }
            if (item.isTextual() && StringUtils.hasText(item.asText()) && !isInternalKey(item.asText())) {
                String fitted = fitToLimit(item.asText().trim(), 120);
                if (StringUtils.hasText(fitted)) {
                    target.add(fitted);
                }
            }
        }
    }

    private void copyEmergency(ArrayNode target, JsonNode items) {
        if (items == null || !items.isArray()) {
            return;
        }
        for (JsonNode item : items) {
            if (target.size() >= 3) {
                return;
            }
            String label = item.path("label").asText("").trim();
            String value = item.path("value").asText("").trim();
            String line = StringUtils.hasText(label) ? label + ": " + value : value;
            if (StringUtils.hasText(line)) {
                String fitted = fitToLimit(line, 100);
                if (StringUtils.hasText(fitted)) {
                    target.add(fitted);
                }
            }
        }
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

    private String truncateClean(String value, int maxChars) {
        if (!StringUtils.hasText(value)) {
            return value == null ? "" : value;
        }
        String v = value.trim();
        if (v.length() <= maxChars) {
            return v;
        }
        int lastSpace = v.lastIndexOf(' ', maxChars);
        return lastSpace > maxChars / 2 ? v.substring(0, lastSpace).trim() : v.substring(0, maxChars).trim();
    }

    private String fitToLimit(String value, int maxChars) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        String v = value.trim();
        if (v.length() <= maxChars) {
            return v;
        }
        // Try to return the first complete sentence
        int dot = v.indexOf(". ");
        if (dot > 0 && dot + 1 <= maxChars) {
            return v.substring(0, dot + 1).trim();
        }
        // No sentence boundary found within limit — skip (return empty so caller can omit)
        return "";
    }


    private String text(JsonNode node, String field) {
        if (node != null && node.path(field).isTextual()) {
            return node.path(field).asText("");
        }
        return "";
    }

    private String levelClass(String level, Integer riskScore) {
        String normalized = StringUtils.hasText(level) ? level.trim().toLowerCase() : riskFromScore(riskScore);
        if (normalized.contains("high")) {
            return "high";
        }
        if (normalized.contains("medium") || normalized.contains("moderate")) {
            return "medium";
        }
        if (normalized.contains("low")) {
            return "low";
        }
        return "unknown";
    }

    private String riskLabel(String level, Integer riskScore) {
        String normalized = levelClass(level, riskScore);
        return "unknown".equals(normalized) ? "Not specified" : normalized;
    }

    private String riskFromScore(Integer riskScore) {
        if (riskScore == null) {
            return "unknown";
        }
        if (riskScore >= 70) {
            return "high";
        }
        if (riskScore >= 40) {
            return "medium";
        }
        return "low";
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
