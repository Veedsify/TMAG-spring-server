package com.TravelMedicineAdvisory.Server.domain.travelplan;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.StringJoiner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.TravelMedicineAdvisory.Server.domain.plans.GeneratedPlan;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

/**
 * Travel health plan PDF — full-bleed teal header, accent-bordered sections,
 * alternating rows.
 */
@Component
public class TravelPlanPdfGenerator {

    private static final Logger log = LoggerFactory.getLogger(TravelPlanPdfGenerator.class);

    // Primary teal palette
    private static final String TEAL_DEEP = "#0d3d35";
    private static final String TEAL_MID = "#1a7a6a";
    private static final String TEAL_LIGHT = "#dff2ee";
    private static final String TEAL_BORDER = "#a8ddd5";

    // Warm neutrals
    private static final String BG_PAGE = "#f8f5f1";
    private static final String BG_SUBTLE = "#f2ede7";
    private static final String BG_ALT = "#faf8f5";
    private static final String DARK = "#1a1208";
    private static final String BODY_COLOR = "#3a2e22";
    private static final String MUTED = "#7a6a5a";
    private static final String BORDER = "#ddd5cb";
    private static final String BORDER_LT = "#ede8e2";

    // Status colours
    private static final String GOLD = "#b8892e";
    private static final String GOLD_SOFT = "#fef6e8";
    private static final String GOLD_BORDER = "#e8d0a0";
    private static final String RED = "#b91c1c";
    private static final String RED_SOFT = "#fef2f2";
    private static final String RED_BORDER = "#fca5a5";
    private static final String GREEN = "#065f46";
    private static final String GREEN_SOFT = "#d1fae5";
    private static final String GREEN_BORDER = "#6ee7b7";

    private static final String IMPORTANT_MEDICAL_DISCLAIMER = "This travel health advisory plan was generated with artificial intelligence and reviewed and validated by a licensed medical doctor. It is provided for informational and educational purposes only and does not substitute consultation, diagnosis, or treatment from a certified travel medicine doctor or a licensed medical doctor. Before making decisions about vaccinations, medications, or travel health, consult your doctor or a qualified travel medicine specialist, especially if you are pregnant, have chronic conditions, or take regular medications. If you become ill during travel, seek immediate local medical care.";
    private static final String CLOSING_DISCLAIMER = "This report supports, but does not substitute care from a certified travel medicine doctor or licensed medical doctor.";

    private final ObjectMapper objectMapper;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    public TravelPlanPdfGenerator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public byte[] generate(TravelPlan plan, GeneratedPlan generatedPlan) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            String html = buildHtml(plan, generatedPlan);
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(html, frontendUrl);
            builder.toStream(out);
            builder.run();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate travel plan PDF", e);
        }
    }

    public byte[] generateSummary(TravelPlan plan, GeneratedPlan generatedPlan) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            String html = buildSummaryHtml(plan, generatedPlan);
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(html, frontendUrl);
            builder.toStream(out);
            builder.run();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate travel plan summary PDF", e);
        }
    }

    private String buildHtml(TravelPlan plan, GeneratedPlan generatedPlan) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\"/>");
        appendStyles(sb);
        sb.append("</head><body>");

        // Full-bleed hero wrapper — teal bg bleeds to page edge (page has 0 side
        // margins)
        sb.append("<div class=\"hero-wrap\">");
        appendHero(sb, plan, generatedPlan);
        sb.append("</div>");

        // Body content with standard side padding
        sb.append("<div class=\"body-wrap\">");

        JsonNode structured = parseStructuredJson(generatedPlan);
        if (structured != null && structured.isObject()) {
            appendStructuredBody(sb, structured);
            appendMedicalDisclaimer(sb, structured);
        } else {
            appendLegacyBody(sb, plan);
            appendMedicalDisclaimer(sb, null);
        }

        // appendGenerationFooter(sb, generatedPlan);
        sb.append("<p class=\"closing\">").append(escapeHtml(CLOSING_DISCLAIMER)).append("</p>");
        sb.append("</div>");
        sb.append("</body></html>");
        return sb.toString();
    }

    private String buildSummaryHtml(TravelPlan plan, GeneratedPlan generatedPlan) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\"/>");
        appendStyles(sb);
        sb.append("</head><body>");
        sb.append("<div class=\"hero-wrap\">");
        appendSummaryHero(sb, plan, generatedPlan);
        sb.append("</div>");
        sb.append("<div class=\"body-wrap\">");

        JsonNode structured = parseStructuredJson(generatedPlan);
        if (structured != null && structured.isObject()) {
            appendSummaryBody(sb, structured);
            appendMedicalDisclaimer(sb, structured);
        } else {
            appendLegacyBody(sb, plan);
            appendMedicalDisclaimer(sb, null);
        }

        sb.append("<p class=\"closing\">").append(escapeHtml(CLOSING_DISCLAIMER)).append("</p>");
        sb.append("</div>");
        sb.append("</body></html>");
        return sb.toString();
    }

    // ── Styles ────────────────────────────────────────────────────────────────
    private void appendStyles(StringBuilder sb) {
        sb.append("<style>");

        // Fonts
        sb.append(
                "@font-face{font-family:'Hanken Grotesk';src:url('classpath:/fonts/HankenGrotesk-Regular.ttf') format('truetype');font-weight:400}");
        sb.append(
                "@font-face{font-family:'Hanken Grotesk';src:url('classpath:/fonts/HankenGrotesk-SemiBold.ttf') format('truetype');font-weight:600}");
        sb.append(
                "@font-face{font-family:'Hanken Grotesk';src:url('classpath:/fonts/HankenGrotesk-Bold.ttf') format('truetype');font-weight:700}");
        sb.append(
                "@font-face{font-family:'Fraunces';src:url('classpath:/fonts/Fraunces-Regular.ttf') format('truetype');font-weight:400}");
        sb.append(
                "@font-face{font-family:'Fraunces';src:url('classpath:/fonts/Fraunces-Bold.ttf') format('truetype');font-weight:700}");

        // Reset
        sb.append("*,*::before,*::after{margin:0;padding:0;box-sizing:border-box}");

        // Page — zero side/top margins so hero bleeds edge-to-edge; bottom margin holds
        // page-number box
        sb.append("@page{size:A4;margin:0 0 16mm 0}");
        sb.append("@page{@bottom-left{content:'Travel Medicine Advisory Global';")
                .append("font-family:'Hanken Grotesk',Arial,sans-serif;font-size:7pt;color:#9a8a7a;padding-left:16mm}}");
        sb.append("@page{@bottom-right{content:'Page ' counter(page);")
                .append("font-family:'Hanken Grotesk',Arial,sans-serif;font-size:7pt;color:#9a8a7a;padding-right:16mm}}");

        // Body
        sb.append("body{font-family:'Hanken Grotesk',Arial,sans-serif;font-size:9.5pt;color:").append(BODY_COLOR)
                .append(";background:").append(BG_PAGE)
                .append(";line-height:1.5;-webkit-print-color-adjust:exact;print-color-adjust:exact}");

        // ── Hero ──────────────────────────────────────────────────────────────
        sb.append(".hero-wrap{background:").append(TEAL_DEEP).append("}");
        sb.append(".brand-strip{padding:30px 16mm 5px;border-bottom:1px solid rgba(255,255,255,0.08)}");
        sb.append(
                ".brand-name{font-size:6.5pt;font-weight:700;letter-spacing:0.22em;text-transform:uppercase;color:#ffffff}");
        sb.append(".brand-sub{font-size:6pt;color:#ffffff;letter-spacing:0.07em;margin-top:2px}");

        sb.append("table.hero-tbl{width:100%;border-collapse:collapse}");
        sb.append(".hero-left{padding:18px 0 18px 16mm;width:64%;vertical-align:top}");
        sb.append(".hero-right{padding:18px 16mm 18px 10px;width:36%;vertical-align:top;text-align:right}");
        sb.append(
                ".doc-title{font-family:'Fraunces',Georgia,serif;font-size:22pt;color:#ffffff;font-weight:300;line-height:1.1;margin:0 0 9px}");
        sb.append(".hero-meta{font-size:8.5pt;color:#ffffff;margin:0 0 3px;line-height:1.4}");
        sb.append(".hero-dates{font-size:8.5pt;color:#7dd3c4;font-weight:600;margin:0 0 3px}");
        sb.append(".hero-traveller{font-size:8.5pt;color:#ffffff}");
        sb.append(".hero-traveller strong{color:#ffffff;font-weight:700}");

        // Badges on dark hero bg
        sb.append(
                ".badge{display:block;padding:5px 10px;border-radius:3px;font-size:7.5pt;font-weight:700;text-align:center;margin-bottom:5px;line-height:1.3}");
        sb.append(".b-low{background:").append(GREEN_SOFT).append(";color:").append(GREEN).append(";border:1px solid ")
                .append(GREEN_BORDER).append("}");
        sb.append(".b-mod{background:").append(GOLD_SOFT).append(";color:").append(GOLD).append(";border:1px solid ")
                .append(GOLD_BORDER).append("}");
        sb.append(".b-hi{background:").append(RED_SOFT).append(";color:").append(RED).append(";border:1px solid ")
                .append(RED_BORDER).append("}");
        sb.append(
                ".b-nu{background:rgb(255,255,255);color:rgba(0, 0, 0, 0.75)}");

        // Accent rule at foot of hero
        sb.append(".hero-rule{height:3px;background:").append(TEAL_MID).append("}");

        // ── Body ──────────────────────────────────────────────────────────────
        sb.append(".body-wrap{padding:14pt 16mm 0}");

        // Section tables — teal left accent border
        sb.append("table.sec{width:100%;border-collapse:collapse;margin:0 0 12pt;")
                .append("border:1px solid ").append(BORDER)
                .append(";border-left:3px solid ").append(TEAL_MID).append("}");

        // Section heading
        sb.append("td.cap,th.cap{background:").append(BG_SUBTLE)
                .append(";color:").append(TEAL_DEEP)
                .append(
                        ";padding:8px 12px;font-size:8pt;font-weight:700;text-transform:uppercase;letter-spacing:0.09em;text-align:left;border-bottom:1px solid ")
                .append(BORDER).append("}");

        // Sub-section heading
        sb.append("td.cap-sub{background:").append(TEAL_LIGHT)
                .append(";color:").append(TEAL_DEEP)
                .append(
                        ";padding:6px 12px;font-size:7.5pt;font-weight:700;text-transform:uppercase;letter-spacing:0.06em;border-bottom:1px solid ")
                .append(TEAL_BORDER).append("}");

        // KV label / value
        sb.append("td.lbl{width:26%;background:").append(BG_SUBTLE)
                .append(";color:").append(MUTED)
                .append(
                        ";font-size:7.5pt;font-weight:700;text-transform:uppercase;letter-spacing:0.04em;padding:8px 12px;border-bottom:1px solid ")
                .append(BORDER_LT).append("}");
        sb.append("td.val{color:").append(DARK)
                .append(";padding:8px 12px;border-bottom:1px solid ").append(BORDER_LT)
                .append(";line-height:1.5;white-space:pre-wrap;font-size:9.5pt}");
        sb.append("tr.last td.lbl,tr.last td.val{border-bottom:none}");

        // Column headers + data cells
        sb.append("th.h{background:").append(TEAL_LIGHT)
                .append(";color:").append(TEAL_DEEP)
                .append(
                        ";font-size:7.5pt;font-weight:700;text-transform:uppercase;letter-spacing:0.05em;padding:8px 12px;border:1px solid ")
                .append(TEAL_BORDER).append(";text-align:left}");
        sb.append("td.c{padding:8px 12px;border:1px solid ").append(BORDER)
                .append(";vertical-align:top;line-height:1.5;white-space:pre-wrap;color:").append(DARK)
                .append(";font-size:9.5pt}");
        sb.append("tr.alt td.c{background:").append(BG_ALT).append("}");

        // Risk level cells — coloured backgrounds
        sb.append(".lvl-h{color:").append(RED).append(";font-weight:700;background:").append(RED_SOFT).append("}");
        sb.append(".lvl-m{color:").append(GOLD).append(";font-weight:700;background:").append(GOLD_SOFT).append("}");
        sb.append(".lvl-l{color:").append(GREEN).append(";font-weight:700;background:").append(GREEN_SOFT).append("}");

        // Urgency badge (for specialist referrals)
        sb.append(
                ".urgency{display:inline-block;padding:2px 7px;border-radius:2px;font-size:7pt;font-weight:700;text-transform:uppercase;letter-spacing:0.04em}");
        sb.append(".urgency-urgent{color:").append(RED).append(";background:").append(RED_SOFT)
                .append(";border:1px solid ").append(RED_BORDER).append("}");
        sb.append(".urgency-before{color:").append(GOLD).append(";background:").append(GOLD_SOFT)
                .append(";border:1px solid ").append(GOLD_BORDER).append("}");
        sb.append(".urgency-routine{color:").append(GREEN).append(";background:").append(GREEN_SOFT)
                .append(";border:1px solid ").append(GREEN_BORDER).append("}");

        // Alert box
        sb.append(".alert{background:").append(RED_SOFT)
                .append(";border:1px solid ").append(RED_BORDER)
                .append(";border-left:3px solid ").append(RED)
                .append(";padding:10px 12px;margin:2px 0 8px}");
        sb.append(".alert-t{font-size:7.5pt;font-weight:700;color:").append(RED)
                .append(";text-transform:uppercase;letter-spacing:0.06em;margin-bottom:5px}");
        sb.append(".alert-b{color:").append(DARK).append(";white-space:pre-wrap;line-height:1.5;font-size:9.5pt}");

        // Bullet rows
        sb.append("td.bull{padding:7px 12px 7px 28px;border-bottom:1px solid ").append(BORDER_LT)
                .append(";position:relative;color:").append(DARK).append(";font-size:9.5pt;line-height:1.45}");
        sb.append("td.bull:before{content:'\\2022';position:absolute;left:12px;color:").append(TEAL_MID)
                .append(";font-weight:700;font-size:11pt;line-height:1.2}");

        // Disclaimer (gold left border)
        sb.append(".disclaimer{font-size:8pt;color:").append(MUTED)
                .append(";padding:12px 14px;background:").append(GOLD_SOFT)
                .append(";border:1px solid ").append(GOLD_BORDER)
                .append(";border-left:3px solid ").append(GOLD)
                .append(";line-height:1.5;white-space:pre-wrap;margin:4px 0 14pt}");

        // Footer lines
        sb.append(".gen-foot{font-size:7.5pt;color:").append(MUTED).append(";margin:6px 0 4px;line-height:1.4}");
        sb.append(".closing{text-align:center;font-size:8pt;color:").append(MUTED)
                .append(";margin-top:14px;padding-top:10px;border-top:1px solid ").append(BORDER)
                .append(";line-height:1.4}");

        sb.append("</style>");
    }

    // ── Hero ──────────────────────────────────────────────────────────────────
    private void appendHero(StringBuilder sb, TravelPlan plan, GeneratedPlan gp) {
        JsonNode j = parseStructuredJson(gp);
        String title = plan.getDestination() != null ? plan.getDestination() : "Travel health plan";
        if (j != null && j.path("reportTitle").isTextual() && StringUtils.hasText(j.get("reportTitle").asText())) {
            title = j.get("reportTitle").asText();
        }

        // Brand strip at very top
        sb.append("<div class=\"brand-strip\">")
                .append("<div class=\"brand-name\">Travel Medicine Advisory Global</div>")
                .append("<div class=\"brand-sub\">Personalised Travel Health Dossier</div>")
                .append("</div>");

        // Two-column row: title/meta left, badges right
        sb.append("<table class=\"hero-tbl\" cellspacing=\"0\" cellpadding=\"0\"><tr>");

        sb.append("<td class=\"hero-left\">");
        sb.append("<h1 class=\"doc-title\">").append(escapeHtml(title)).append("</h1>");
        sb.append("<p class=\"hero-meta\">")
                .append(escapeHtml(nullSafe(plan.getCountry())))
                .append(plan.getDuration() != null ? " \u00B7 " + plan.getDuration() + " days" : "")
                .append(StringUtils.hasText(plan.getPurpose()) ? " \u00B7 " + escapeHtml(plan.getPurpose()) : "")
                .append("</p>");
        boolean wroteTravelDates = false;
        if (j != null && j.path("travelDates").isTextual() && StringUtils.hasText(j.get("travelDates").asText())) {
            sb.append("<p class=\"hero-dates\">").append(escapeHtml(j.get("travelDates").asText())).append("</p>");
            wroteTravelDates = true;
        }
        if (!wroteTravelDates) {
            String fallbackDates = formatReturnDatesFromPlan(plan);
            if (StringUtils.hasText(fallbackDates)) {
                sb.append("<p class=\"hero-dates\">").append(escapeHtml(fallbackDates)).append("</p>");
            }
        }
        if (j != null && j.path("travellerName").isTextual() && StringUtils.hasText(j.get("travellerName").asText())) {
            sb.append("<p class=\"hero-traveller\">Traveller: <strong>")
                    .append(escapeHtml(j.get("travellerName").asText()))
                    .append("</strong></p>");
        }
        sb.append("</td>");

        sb.append("<td class=\"hero-right\">");
        sb.append("<span class=\"badge ").append(riskBadgeClass(plan.getRiskScore())).append("\">")
                .append(escapeHtml(riskLabel(plan.getRiskScore()))).append(" risk</span>");
        sb.append("<span class=\"badge b-nu\">")
                .append(escapeHtml(plan.getStatus() != null ? plan.getStatus() : "—"))
                .append("</span>");
        if (plan.getCreatedAt() != null) {
            sb.append("<span class=\"badge b-nu\">")
                    .append(plan.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM d, yyyy")))
                    .append("</span>");
        }
        sb.append("</td>");
        sb.append("</tr></table>");

        // Bright teal accent line
        sb.append("<div class=\"hero-rule\"></div>");
    }

    private void appendSummaryHero(StringBuilder sb, TravelPlan plan, GeneratedPlan gp) {
        JsonNode j = parseStructuredJson(gp);
        String title = "Travel health summary";
        if (plan.getDestination() != null) {
            title = "Travel health summary: " + plan.getDestination();
        }

        sb.append("<div class=\"brand-strip\">")
                .append("<div class=\"brand-name\">Travel Medicine Advisory Global</div>")
                .append("<div class=\"brand-sub\">Condensed Travel Health Summary</div>")
                .append("</div>");
        sb.append("<table class=\"hero-tbl\" cellspacing=\"0\" cellpadding=\"0\"><tr>");
        sb.append("<td class=\"hero-left\">");
        sb.append("<h1 class=\"doc-title\">").append(escapeHtml(title)).append("</h1>");
        sb.append("<p class=\"hero-meta\">")
                .append(escapeHtml(nullSafe(plan.getCountry())))
                .append(plan.getDuration() != null ? " \u00B7 " + plan.getDuration() + " days" : "")
                .append(StringUtils.hasText(plan.getPurpose()) ? " \u00B7 " + escapeHtml(plan.getPurpose()) : "")
                .append("</p>");
        if (j != null && j.path("travelDates").isTextual() && StringUtils.hasText(j.get("travelDates").asText())) {
            sb.append("<p class=\"hero-dates\">").append(escapeHtml(j.get("travelDates").asText())).append("</p>");
        } else {
            String fallbackDates = formatReturnDatesFromPlan(plan);
            if (StringUtils.hasText(fallbackDates)) {
                sb.append("<p class=\"hero-dates\">").append(escapeHtml(fallbackDates)).append("</p>");
            }
        }
        if (j != null && j.path("travellerName").isTextual() && StringUtils.hasText(j.get("travellerName").asText())) {
            sb.append("<p class=\"hero-traveller\">Traveller: <strong>")
                    .append(escapeHtml(j.get("travellerName").asText()))
                    .append("</strong></p>");
        }
        sb.append("</td>");
        sb.append("<td class=\"hero-right\">");
        sb.append("<span class=\"badge ").append(riskBadgeClass(plan.getRiskScore())).append("\">")
                .append(escapeHtml(riskLabel(plan.getRiskScore()))).append(" risk</span>");
        sb.append("<span class=\"badge b-nu\">SUMMARY</span>");
        if (plan.getCreatedAt() != null) {
            sb.append("<span class=\"badge b-nu\">")
                    .append(plan.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM d, yyyy")))
                    .append("</span>");
        }
        sb.append("</td></tr></table>");
        sb.append("<div class=\"hero-rule\"></div>");
    }

    // ── Structured body ───────────────────────────────────────────────────────
    private void appendSummaryBody(StringBuilder sb, JsonNode root) {
        JsonNode risks = root.path("healthRiskOverview");
        if (risks.isArray() && risks.size() > 0) {
            appendTableStart(sb, "Key health risks", 3);
            sb.append("<tr><th class=\"h\">Category</th><th class=\"h\">Level</th><th class=\"h\">Summary</th></tr>");
            int rowIdx = 0;
            for (Iterator<JsonNode> it = risks.elements(); it.hasNext(); rowIdx++) {
                JsonNode r = it.next();
                String lvl = r.path("level").asText("—");
                String alt = (rowIdx % 2 == 1) ? " class=\"alt\"" : "";
                sb.append("<tr").append(alt).append(">")
                        .append("<td class=\"c\">").append(escapeHtml(r.path("category").asText("—"))).append("</td>")
                        .append("<td class=\"c ").append(levelClass(lvl)).append("\">")
                        .append(escapeHtml(lvl.toUpperCase())).append("</td>")
                        .append("<td class=\"c\">").append(escapeHtml(r.path("summary").asText(""))).append("</td>")
                        .append("</tr>");
            }
            sb.append("</table>");
        }

        appendSummaryRecommendations(sb, root.path("vaccinations"), "Vaccination actions", "vaccine", "recommendation", "action");
        appendMalariaSummary(sb, root.path("malariaPrevention"));
        appendSummaryRecommendations(sb, root.path("recommendations"), "Priority medical advice", "title", "details", null);
        appendTextArraySection(sb, root.path("clinicalFlags"), "Clinical flags");
        appendTextArraySection(sb, root.path("contraindications"), "Contraindications");

        JsonNode afterReturn = root.path("afterReturn");
        if (afterReturn.isObject() && afterReturnNonEmpty(afterReturn)) {
            appendTableStart(sb, "After return red flags", 1);
            if (afterReturn.path("redFlag").isTextual() && StringUtils.hasText(afterReturn.get("redFlag").asText())) {
                sb.append("<tr><td class=\"val\">").append(escapeHtml(afterReturn.get("redFlag").asText())).append("</td></tr>");
            }
            appendBulletSubTable(sb, afterReturn.path("within1Week"), "Within 1 week");
            appendBulletSubTable(sb, afterReturn.path("within4Weeks"), "Within 4 weeks");
            sb.append("</table>");
        }

        JsonNode medicalCare = root.path("medicalCare");
        if (medicalCare.isObject() && medicalCare.path("emergencyContacts").isArray()
                && medicalCare.path("emergencyContacts").size() > 0) {
            appendTableStart(sb, "Emergency contacts", 1);
            appendBulletSubTable(sb, medicalCare.path("emergencyContacts"), "Contacts");
            sb.append("</table>");
        }
    }

    private void appendSummaryRecommendations(StringBuilder sb, JsonNode items, String title, String labelField,
            String primaryField, String secondaryField) {
        if (!items.isArray() || items.size() == 0) {
            return;
        }
        appendTableStart(sb, title, 2);
        sb.append("<tr><th class=\"h\">Topic</th><th class=\"h\">Action</th></tr>");
        int rowIdx = 0;
        for (Iterator<JsonNode> it = items.elements(); it.hasNext(); rowIdx++) {
            JsonNode item = it.next();
            StringBuilder details = new StringBuilder(item.path(primaryField).asText(""));
            if (secondaryField != null && item.path(secondaryField).isTextual()
                    && StringUtils.hasText(item.get(secondaryField).asText())) {
                if (!details.isEmpty()) {
                    details.append("\n\n");
                }
                details.append(item.get(secondaryField).asText());
            }
            String alt = (rowIdx % 2 == 1) ? " class=\"alt\"" : "";
            sb.append("<tr").append(alt).append(">")
                    .append("<td class=\"c\" style=\"width:32%\"><strong>")
                    .append(escapeHtml(item.path(labelField).asText("—"))).append("</strong></td>")
                    .append("<td class=\"c\">").append(escapeHtml(details.toString())).append("</td>")
                    .append("</tr>");
        }
        sb.append("</table>");
    }

    private void appendMalariaSummary(StringBuilder sb, JsonNode malaria) {
        if (!malaria.isObject() || !malariaNonEmpty(malaria)) {
            return;
        }
        appendTableStart(sb, "Malaria prevention summary", 2);
        int rows = 3;
        int i = 0;
        i += appendKVRow(sb, "Risk level", textOrNull(malaria, "riskLevel"), i, rows);
        i += appendKVRow(sb, "Recommended chemoprophylaxis", textOrNull(malaria, "recommendedAgent"), i, rows);
        appendKVRow(sb, "Rationale", textOrNull(malaria, "rationale"), i, rows);
        JsonNode mosquito = malaria.path("mosquitoProtection");
        if (mosquito.isArray() && mosquito.size() > 0) {
            sb.append("<tr><td colspan=\"2\" class=\"cap-sub\">Mosquito protection</td></tr>");
            for (Iterator<JsonNode> it = mosquito.elements(); it.hasNext();) {
                JsonNode line = it.next();
                if (line.isTextual()) {
                    sb.append("<tr><td colspan=\"2\" class=\"bull\">").append(escapeHtml(line.asText())).append("</td></tr>");
                }
            }
        }
        sb.append("</table>");
    }

    private void appendTextArraySection(StringBuilder sb, JsonNode items, String title) {
        if (!items.isArray() || items.size() == 0) {
            return;
        }
        appendTableStart(sb, title, 1);
        for (Iterator<JsonNode> it = items.elements(); it.hasNext();) {
            JsonNode item = it.next();
            if (item.isTextual()) {
                sb.append("<tr><td class=\"bull\">").append(escapeHtml(item.asText())).append("</td></tr>");
            }
        }
        sb.append("</table>");
    }

    private void appendStructuredBody(StringBuilder sb, JsonNode root) {
        JsonNode glance = root.path("tripAtGlance");
        if (glance.isObject() && glance.size() > 0) {
            int rows = countGlanceRows(glance);
            if (rows > 0) {
                appendTableStart(sb, "Trip at a glance", 2);
                int i = 0;
                i += appendKVRow(sb, "Duration", glance.path("durationDays").isNumber()
                        ? glance.get("durationDays").asInt() + " days"
                        : null, i, rows);
                i += appendKVRow(sb, "Purpose", textOrNull(glance, "purpose"), i, rows);
                i += appendKVRow(sb, "Travelling", textOrNull(glance, "travelling"), i, rows);
                i += appendKVRow(sb, "Accommodation", textOrNull(glance, "accommodation"), i, rows);
                appendKVRow(sb, "Insurance", textOrNull(glance, "insurance"), i, rows);
                sb.append("</table>");
            }
        }

        JsonNode itinerary = root.path("itineraryGuidance");
        if (itinerary.isObject() && itineraryNonEmpty(itinerary)) {
            appendTableStart(sb, "Itinerary-specific guidance", 1);
            if (itinerary.path("tripType").isTextual() && StringUtils.hasText(itinerary.get("tripType").asText())) {
                sb.append("<tr><td class=\"val\" style=\"border-bottom:1px solid ").append(BORDER_LT).append("\">")
                        .append("<strong>Trip type:</strong> ")
                        .append(escapeHtml(itinerary.get("tripType").asText()))
                        .append("</td></tr>");
            }
            if (itinerary.path("summary").isTextual() && StringUtils.hasText(itinerary.get("summary").asText())) {
                sb.append("<tr><td class=\"val\" style=\"border-bottom:1px solid ").append(BORDER_LT).append("\">")
                        .append(escapeHtml(itinerary.get("summary").asText()))
                        .append("</td></tr>");
            }

            JsonNode routeAdvice = itinerary.path("routeAdvice");
            if (routeAdvice.isArray() && routeAdvice.size() > 0) {
                sb.append("<tr><td class=\"cap-sub\">Route guidance by stop</td></tr>");
                sb.append("<tr><td style=\"padding:0\"><table class=\"sec\" style=\"margin:0;border:none\">");
                sb.append(
                        "<tr><th class=\"h\">Stop</th><th class=\"h\">Country</th><th class=\"h\">Guidance</th></tr>");
                int rowIdx = 0;
                for (Iterator<JsonNode> it = routeAdvice.elements(); it.hasNext(); rowIdx++) {
                    JsonNode stop = it.next();
                    String alt = (rowIdx % 2 == 1) ? " class=\"alt\"" : "";
                    sb.append("<tr").append(alt).append(">")
                            .append("<td class=\"c\">").append(escapeHtml(stop.path("stop").asText("—")))
                            .append("</td>")
                            .append("<td class=\"c\">").append(escapeHtml(stop.path("country").asText("—")))
                            .append("</td>")
                            .append("<td class=\"c\">").append(escapeHtml(stop.path("guidance").asText("")))
                            .append("</td>")
                            .append("</tr>");
                }
                sb.append("</table></td></tr>");
            }

            JsonNode returnGuidance = itinerary.path("returnGuidance");
            if (returnGuidance.isArray() && returnGuidance.size() > 0) {
                sb.append("<tr><td class=\"cap-sub\">Return leg guidance</td></tr>");
                for (Iterator<JsonNode> it = returnGuidance.elements(); it.hasNext();) {
                    JsonNode line = it.next();
                    if (line.isTextual()) {
                        sb.append("<tr><td class=\"bull\">").append(escapeHtml(line.asText())).append("</td></tr>");
                    }
                }
            }
            sb.append("</table>");
        }

        JsonNode risks = root.path("healthRiskOverview");
        if (risks.isArray() && risks.size() > 0) {
            appendTableStart(sb, "Health risk overview", 3);
            sb.append("<tr><th class=\"h\">Category</th><th class=\"h\">Level</th><th class=\"h\">Summary</th></tr>");
            int rowIdx = 0;
            for (Iterator<JsonNode> it = risks.elements(); it.hasNext(); rowIdx++) {
                JsonNode r = it.next();
                String lvl = r.path("level").asText("—");
                String alt = (rowIdx % 2 == 1) ? " class=\"alt\"" : "";
                sb.append("<tr").append(alt).append(">")
                        .append("<td class=\"c\">").append(escapeHtml(r.path("category").asText("—"))).append("</td>")
                        .append("<td class=\"c ").append(levelClass(lvl)).append("\">")
                        .append(escapeHtml(lvl.toUpperCase())).append("</td>")
                        .append("<td class=\"c\">").append(escapeHtml(r.path("summary").asText(""))).append("</td>")
                        .append("</tr>");
            }
            sb.append("</table>");
        }

        // Flight & Journey Health
        JsonNode flight = root.path("flightHealth");
        if (flight.isObject() && flightNonEmpty(flight)) {
            appendTableStart(sb, "Flight &amp; journey health", 2);
            if (flight.path("vteRiskLevel").isTextual() && StringUtils.hasText(flight.get("vteRiskLevel").asText())) {
                String vteLvl = flight.get("vteRiskLevel").asText();
                sb.append("<tr><td colspan=\"3\" class=\"val\" style=\"border-bottom:1px solid ").append(BORDER_LT)
                        .append("\">")
                        .append("<strong>VTE Risk Level:</strong> ")
                        .append("<span class=\"").append(levelClass(vteLvl)).append("\">")
                        .append(escapeHtml(vteLvl.toUpperCase()))
                        .append("</span></td></tr>");
            }
            JsonNode prevention = flight.path("preventionMeasures");
            if (prevention.isArray() && prevention.size() > 0) {
                sb.append("<tr><td colspan=\"3\" class=\"cap-sub\">Prevention measures</td></tr>");
                for (Iterator<JsonNode> pit = prevention.elements(); pit.hasNext();) {
                    JsonNode line = pit.next();
                    if (line.isTextual()) {
                        sb.append("<tr><td colspan=\"3\" class=\"bull\">").append(escapeHtml(line.asText()))
                                .append("</td></tr>");
                    }
                }
            }
            if (flight.path("medifClearanceRequired").asBoolean(false)) {
                sb.append("<tr><td colspan=\"3\" class=\"val\" style=\"border-bottom:1px solid ").append(BORDER_LT)
                        .append("\">")
                        .append("<div class=\"alert\"><div class=\"alert-t\">Airline MEDIF clearance required</div>")
                        .append(
                                "<div class=\"alert-b\">Contact your airline to arrange medical clearance before travel.</div></div>")
                        .append("</td></tr>");
            }
            if (flight.path("medicationTimingGuidance").isTextual()
                    && StringUtils.hasText(flight.get("medicationTimingGuidance").asText())) {
                appendKVRow(sb, "Medication timing guidance", flight.get("medicationTimingGuidance").asText(), 0, 1);
            }
            sb.append("</table>");
        }

        JsonNode vacs = root.path("vaccinations");
        if (vacs.isArray() && vacs.size() > 0) {
            appendTableStart(sb, "Vaccinations", 3);
            sb.append("<tr><th class=\"h\">Vaccine</th><th class=\"h\">Status</th><th class=\"h\">Guidance</th></tr>");
            int rowIdx = 0;
            for (Iterator<JsonNode> it = vacs.elements(); it.hasNext(); rowIdx++) {
                JsonNode v = it.next();
                StringBuilder guide = new StringBuilder();
                if (v.path("recommendation").isTextual()) {
                    guide.append(v.get("recommendation").asText());
                }
                if (v.path("action").isTextual()) {
                    if (!guide.isEmpty()) {
                        guide.append("\n\n");
                    }
                    guide.append(v.get("action").asText());
                }
                String alt = (rowIdx % 2 == 1) ? " class=\"alt\"" : "";
                sb.append("<tr").append(alt).append(">")
                        .append("<td class=\"c\"><strong>").append(escapeHtml(v.path("vaccine").asText("—")))
                        .append("</strong></td>")
                        .append("<td class=\"c\">").append(escapeHtml(v.path("status").asText("—"))).append("</td>")
                        .append("<td class=\"c\">").append(escapeHtml(guide.toString())).append("</td>")
                        .append("</tr>");
            }
            sb.append("</table>");
        }

        // Malaria Prevention
        JsonNode malaria = root.path("malariaPrevention");
        if (malaria.isObject() && malariaNonEmpty(malaria)) {
            appendTableStart(sb, "Malaria prevention", 3);
            if (malaria.path("riskLevel").isTextual() && StringUtils.hasText(malaria.get("riskLevel").asText())) {
                String mrl = malaria.get("riskLevel").asText();
                sb.append("<tr><td colspan=\"3\" class=\"val\" style=\"border-bottom:1px solid ").append(BORDER_LT)
                        .append("\">")
                        .append("<strong>Risk Level:</strong> ")
                        .append("<span class=\"").append(levelClass(mrl)).append("\">")
                        .append(escapeHtml(mrl.toUpperCase()))
                        .append("</span></td></tr>");
            }
            if (malaria.path("recommendedAgent").isTextual()
                    && StringUtils.hasText(malaria.get("recommendedAgent").asText())) {
                StringBuilder agentInfo = new StringBuilder();
                agentInfo.append(malaria.get("recommendedAgent").asText());
                if (malaria.path("rationale").isTextual() && StringUtils.hasText(malaria.get("rationale").asText())) {
                    agentInfo.append("\n\n").append(malaria.get("rationale").asText());
                }
                appendKVRow(sb, "Recommended chemoprophylaxis", agentInfo.toString(), 0, 1);
            }
            JsonNode mosquito = malaria.path("mosquitoProtection");
            if (mosquito.isArray() && mosquito.size() > 0) {
                sb.append("<tr><td colspan=\"3\" class=\"cap-sub\">Mosquito protection measures</td></tr>");
                for (Iterator<JsonNode> mit = mosquito.elements(); mit.hasNext();) {
                    JsonNode line = mit.next();
                    if (line.isTextual()) {
                        sb.append("<tr><td colspan=\"3\" class=\"bull\">").append(escapeHtml(line.asText()))
                                .append("</td></tr>");
                    }
                }
            }
            if (malaria.path("contraindications").isTextual()
                    && StringUtils.hasText(malaria.get("contraindications").asText())) {
                appendKVRow(sb, "Contraindications / alternatives", malaria.get("contraindications").asText(), 0, 1);
            }
            sb.append("</table>");
        }

        JsonNode recs = root.path("recommendations");
        if (recs.isArray() && recs.size() > 0) {
            appendTableStart(sb, "Medical advice &amp; recommendations", 2);
            sb.append("<tr><th class=\"h\">Topic</th><th class=\"h\">Details</th></tr>");
            int rowIdx = 0;
            for (Iterator<JsonNode> it = recs.elements(); it.hasNext(); rowIdx++) {
                JsonNode r = it.next();
                String alt = (rowIdx % 2 == 1) ? " class=\"alt\"" : "";
                sb.append("<tr").append(alt).append(">")
                        .append("<td class=\"c\" style=\"width:32%\"><strong>")
                        .append(escapeHtml(r.path("title").asText("—"))).append("</strong></td>")
                        .append("<td class=\"c\">").append(escapeHtml(r.path("details").asText(""))).append("</td>")
                        .append("</tr>");
            }
            sb.append("</table>");
        }

        // Medical Conditions
        JsonNode conditions = root.path("medicalConditions");
        if (conditions.isArray() && conditions.size() > 0) {
            appendTableStart(sb, "Medical conditions &amp; precautions", 2);
            sb.append("<tr><th class=\"h\">Condition</th><th class=\"h\">Precautions</th></tr>");
            int rowIdx = 0;
            for (Iterator<JsonNode> it = conditions.elements(); it.hasNext(); rowIdx++) {
                JsonNode c = it.next();
                String alt = (rowIdx % 2 == 1) ? " class=\"alt\"" : "";
                sb.append("<tr").append(alt).append(">")
                        .append("<td class=\"c\" style=\"width:32%\"><strong>")
                        .append(escapeHtml(c.path("condition").asText("—"))).append("</strong></td>")
                        .append("<td class=\"c\">").append(escapeHtml(c.path("precautions").asText(""))).append("</td>")
                        .append("</tr>");
            }
            sb.append("</table>");
        }

        // Medication Logistics
        JsonNode medLog = root.path("medicationLogistics");
        if (medLog.isObject() && medicationLogisticsNonEmpty(medLog)) {
            appendTableStart(sb, "Medication logistics", 2);
            int rows = countMedicationLogisticsRows(medLog);
            int i = 0;
            i += appendKVRow(sb, "Packaging", textOrNull(medLog, "packaging"), i, rows);
            i += appendKVRow(sb, "Supply rule", textOrNull(medLog, "supplyRule"), i, rows);
            if (medLog.path("destinationLegalityCheck").asBoolean(false)) {
                i += appendKVRow(sb, "Destination legality verified",
                        "Yes — confirm all medications are legal at destination.", i, rows);
            }
            if (medLog.path("coldChainRequired").asBoolean(false)) {
                i += appendKVRow(sb, "Cold chain required",
                        "Yes — use insulated pouches (e.g. Frio) and verify refrigeration availability.", i, rows);
            }
            sb.append("</table>");
        }

        // Clinical Flags
        JsonNode clinicalFlags = root.path("clinicalFlags");
        if (clinicalFlags.isArray() && clinicalFlags.size() > 0) {
            appendTableStart(sb, "Clinical decision flags", 1);
            for (Iterator<JsonNode> it = clinicalFlags.elements(); it.hasNext();) {
                JsonNode flag = it.next();
                if (flag.isTextual()) {
                    sb.append("<tr><td class=\"bull\">").append(escapeHtml(flag.asText())).append("</td></tr>");
                }
            }
            sb.append("</table>");
        }

        // Contraindications
        JsonNode contraindications = root.path("contraindications");
        if (contraindications.isArray() && contraindications.size() > 0) {
            appendTableStart(sb, "Contraindications (medications &amp; vaccines)", 1);
            for (Iterator<JsonNode> it = contraindications.elements(); it.hasNext();) {
                JsonNode item = it.next();
                if (item.isTextual()) {
                    sb.append("<tr><td class=\"bull\">").append(escapeHtml(item.asText())).append("</td></tr>");
                }
            }
            sb.append("</table>");
        }

        // Specialist Referrals
        JsonNode referrals = root.path("specialistReferrals");
        if (referrals.isArray() && referrals.size() > 0) {
            appendTableStart(sb, "Specialist referrals", 3);
            sb.append(
                    "<tr><th class=\"h\">Condition</th><th class=\"h\">Specialist</th><th class=\"h\">Urgency</th></tr>");
            int rowIdx = 0;
            for (Iterator<JsonNode> it = referrals.elements(); it.hasNext(); rowIdx++) {
                JsonNode ref = it.next();
                String urgency = ref.path("urgency").asText("—");
                String urgencyBadge = urgencyBadgeClass(urgency);
                String alt = (rowIdx % 2 == 1) ? " class=\"alt\"" : "";
                sb.append("<tr").append(alt).append(">")
                        .append("<td class=\"c\">").append(escapeHtml(ref.path("condition").asText("—")))
                        .append("</td>")
                        .append("<td class=\"c\">").append(escapeHtml(ref.path("specialist").asText("—")))
                        .append("</td>")
                        .append("<td class=\"c\" style=\"text-align:center\"><span class=\"urgency ")
                        .append(urgencyBadge).append("\">")
                        .append(escapeHtml(urgency)).append("</span></td>")
                        .append("</tr>");
            }
            sb.append("</table>");
        }

        // Sexual Health
        JsonNode sexualHealth = root.path("sexualHealth");
        if (sexualHealth.isObject() && sexualHealthNonEmpty(sexualHealth)) {
            appendTableStart(sb, "Sexual health &amp; risk behaviours", 3);
            if (sexualHealth.path("riskLevel").isTextual()
                    && StringUtils.hasText(sexualHealth.get("riskLevel").asText())) {
                String shl = sexualHealth.get("riskLevel").asText();
                sb.append("<tr><td colspan=\"3\" class=\"val\" style=\"border-bottom:1px solid ").append(BORDER_LT)
                        .append("\">")
                        .append("<strong>Risk Level:</strong> ")
                        .append("<span class=\"").append(levelClass(shl)).append("\">")
                        .append(escapeHtml(shl.toUpperCase()))
                        .append("</span></td></tr>");
            }
            JsonNode prevention = sexualHealth.path("preventionAdvice");
            if (prevention.isArray() && prevention.size() > 0) {
                sb.append("<tr><td colspan=\"3\" class=\"cap-sub\">Prevention advice</td></tr>");
                for (Iterator<JsonNode> pit = prevention.elements(); pit.hasNext();) {
                    JsonNode line = pit.next();
                    if (line.isTextual()) {
                        sb.append("<tr><td colspan=\"3\" class=\"bull\">").append(escapeHtml(line.asText()))
                                .append("</td></tr>");
                    }
                }
            }
            if (sexualHealth.path("prepPepDiscussion").asBoolean(false)) {
                appendKVRow(sb, "PrEP/PEP", "PrEP/PEP availability discussed for high-risk destination.", 0, 1);
            }
            sb.append("</table>");
        }

        // Pregnancy Guidance
        JsonNode pregnancy = root.path("pregnancyGuidance");
        if (pregnancy.isObject() && pregnancyNonEmpty(pregnancy)) {
            appendTableStart(sb, "Pregnancy &amp; reproductive health", 1);
            if (pregnancy.path("trimesterSpecificAdvice").isTextual()
                    && StringUtils.hasText(pregnancy.get("trimesterSpecificAdvice").asText())) {
                appendKVRow(sb, "Trimester guidance", pregnancy.get("trimesterSpecificAdvice").asText(), 0, 1);
            }
            if (pregnancy.path("antimalarialSafety").isTextual()
                    && StringUtils.hasText(pregnancy.get("antimalarialSafety").asText())) {
                appendKVRow(sb, "Antimalarial safety", pregnancy.get("antimalarialSafety").asText(), 0, 1);
            }
            JsonNode liveVaxCx = pregnancy.path("liveVaccineContraindications");
            if (liveVaxCx.isArray() && liveVaxCx.size() > 0) {
                sb.append("<tr><td class=\"cap-sub\">Live vaccine contraindications</td></tr>");
                for (Iterator<JsonNode> lit = liveVaxCx.elements(); lit.hasNext();) {
                    JsonNode line = lit.next();
                    if (line.isTextual()) {
                        sb.append("<tr><td class=\"bull\">").append(escapeHtml(line.asText())).append("</td></tr>");
                    }
                }
            }
            if (pregnancy.path("airlineRestrictions").isTextual()
                    && StringUtils.hasText(pregnancy.get("airlineRestrictions").asText())) {
                appendKVRow(sb, "Airline restrictions", pregnancy.get("airlineRestrictions").asText(), 0, 1);
            }
            if (pregnancy.path("contraceptionCounselling").isTextual()
                    && StringUtils.hasText(pregnancy.get("contraceptionCounselling").asText())) {
                appendKVRow(sb, "Contraception counselling", pregnancy.get("contraceptionCounselling").asText(), 0, 1);
            }
            sb.append("</table>");
        }

        JsonNode ar = root.path("afterReturn");
        if (ar.isObject() && afterReturnNonEmpty(ar)) {
            appendTableStart(sb, "After you return", 1);
            if (ar.path("redFlag").isTextual() && StringUtils.hasText(ar.get("redFlag").asText())) {
                sb.append("<tr><td class=\"val\" style=\"border:none;padding:6px 12px 4px\">")
                        .append(
                                "<div class=\"alert\"><div class=\"alert-t\">Red flags \u2014 seek immediate care if you experience</div>")
                        .append("<div class=\"alert-b\">").append(escapeHtml(ar.get("redFlag").asText()))
                        .append("</div></div>")
                        .append("</td></tr>");
            }
            appendBulletSubTable(sb, ar.path("within1Week"), "Within 1 week");
            appendBulletSubTable(sb, ar.path("within4Weeks"), "Within 4 weeks");
            appendBulletSubTable(sb, ar.path("beyond4Weeks"), "Beyond 4 weeks");
            sb.append("</table>");
        }

        JsonNode mc = root.path("medicalCare");
        if (mc.isObject() && medicalCareNonEmpty(mc)) {
            appendTableStart(sb, "Medical care &amp; contacts", 1);
            JsonNode clinics = mc.path("clinics");
            if (clinics.isArray() && clinics.size() > 0) {
                sb.append("<tr><td class=\"cap-sub\">Clinics &amp; facilities</td></tr>");
                sb.append("<tr><td style=\"padding:0\"><table class=\"sec\" style=\"margin:0;border:none\">");
                sb.append("<tr><th class=\"h\">Name</th><th class=\"h\">Location &amp; notes</th></tr>");
                int rowIdx = 0;
                for (Iterator<JsonNode> cit = clinics.elements(); cit.hasNext(); rowIdx++) {
                    JsonNode c = cit.next();
                    StringBuilder det = new StringBuilder();
                    if (c.path("address").isTextual()) {
                        det.append(c.get("address").asText());
                    }
                    String phone = c.path("phone").asText("");
                    String dist = c.path("distance").asText("");
                    if (StringUtils.hasText(phone) || StringUtils.hasText(dist)) {
                        if (!det.isEmpty()) {
                            det.append("\n");
                        }
                        StringJoiner sj = new StringJoiner(" · ");
                        if (StringUtils.hasText(phone)) {
                            sj.add(phone);
                        }
                        if (StringUtils.hasText(dist)) {
                            sj.add(dist);
                        }
                        det.append(sj);
                    }
                    if (c.path("notes").isTextual()) {
                        if (!det.isEmpty()) {
                            det.append("\n\n");
                        }
                        det.append(c.get("notes").asText());
                    }
                    String alt = (rowIdx % 2 == 1) ? " class=\"alt\"" : "";
                    sb.append("<tr").append(alt).append(">")
                            .append("<td class=\"c\" style=\"width:34%\"><strong>")
                            .append(escapeHtml(c.path("name").asText("—"))).append("</strong></td>")
                            .append("<td class=\"c\">").append(escapeHtml(det.toString())).append("</td>")
                            .append("</tr>");
                }
                sb.append("</table></td></tr>");
            }
            JsonNode emb = mc.path("embassyContacts");
            if (emb.isArray() && emb.size() > 0) {
                sb.append("<tr><td class=\"cap-sub\">Embassy &amp; consular</td></tr>");
                sb.append("<tr><td style=\"padding:0\"><table class=\"sec\" style=\"margin:0;border:none\">");
                sb.append("<tr><th class=\"h\">Name</th><th class=\"h\">Details</th></tr>");
                int rowIdx = 0;
                for (Iterator<JsonNode> eit = emb.elements(); eit.hasNext(); rowIdx++) {
                    JsonNode e = eit.next();
                    String alt = (rowIdx % 2 == 1) ? " class=\"alt\"" : "";
                    sb.append("<tr").append(alt).append(">")
                            .append("<td class=\"c\"><strong>").append(escapeHtml(e.path("name").asText("—")))
                            .append("</strong></td>")
                            .append("<td class=\"c\">").append(escapeHtml(e.path("details").asText(""))).append("</td>")
                            .append("</tr>");
                }
                sb.append("</table></td></tr>");
            }
            JsonNode ec = mc.path("emergencyContacts");
            if (ec.isArray() && ec.size() > 0) {
                sb.append("<tr><td class=\"cap-sub\">Emergency numbers</td></tr>");
                sb.append(
                        "<tr><td style=\"padding:0\"><table class=\"sec\" style=\"margin:0;border:none;width:100%\">");
                sb.append("<tr><th class=\"h\">Label</th><th class=\"h\">Contact</th></tr>");
                int rowIdx = 0;
                for (Iterator<JsonNode> xit = ec.elements(); xit.hasNext(); rowIdx++) {
                    JsonNode x = xit.next();
                    String alt = (rowIdx % 2 == 1) ? " class=\"alt\"" : "";
                    sb.append("<tr").append(alt).append(">")
                            .append("<td class=\"c\">").append(escapeHtml(x.path("label").asText(""))).append("</td>")
                            .append("<td class=\"c\"><strong>").append(escapeHtml(x.path("value").asText("")))
                            .append("</strong></td>")
                            .append("</tr>");
                }
                sb.append("</table></td></tr>");
            }
            sb.append("</table>");
        }

        JsonNode steps = root.path("nextSteps");
        if (steps.isArray() && steps.size() > 0) {
            appendTableStart(sb, "Next steps", 1);
            for (Iterator<JsonNode> sit = steps.elements(); sit.hasNext();) {
                JsonNode s = sit.next();
                if (s.isTextual()) {
                    sb.append("<tr><td class=\"bull\">").append(escapeHtml(s.asText())).append("</td></tr>");
                }
            }
            sb.append("</table>");
        }

    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private void appendTableStart(StringBuilder sb, String title, int colspan) {
        sb.append("<table class=\"sec\" cellspacing=\"0\" cellpadding=\"0\">")
                .append("<tr><td class=\"cap\" colspan=\"").append(colspan).append("\">")
                .append(title)
                .append("</td></tr>");
    }

    /**
     * Returns 0 or 1 — number of rows actually appended.
     */
    private int appendKVRow(StringBuilder sb, String label, String value, int index, int totalRows) {
        if (!StringUtils.hasText(value)) {
            return 0;
        }
        boolean last = (index + 1 >= totalRows);
        sb.append("<tr").append(last ? " class=\"last\"" : "").append(">")
                .append("<td class=\"lbl\">").append(escapeHtml(label)).append("</td>")
                .append("<td class=\"val\">").append(escapeHtml(value)).append("</td>")
                .append("</tr>");
        return 1;
    }

    private int countGlanceRows(JsonNode glance) {
        int n = 0;
        if (glance.path("durationDays").isNumber()) {
            n++;
        }
        if (StringUtils.hasText(textOrNull(glance, "purpose"))) {
            n++;
        }
        if (StringUtils.hasText(textOrNull(glance, "travelling"))) {
            n++;
        }
        if (StringUtils.hasText(textOrNull(glance, "accommodation"))) {
            n++;
        }
        if (StringUtils.hasText(textOrNull(glance, "insurance"))) {
            n++;
        }
        return n;
    }

    private void appendBulletSubTable(StringBuilder sb, JsonNode arr, String subTitle) {
        if (!arr.isArray() || arr.isEmpty()) {
            return;
        }
        sb.append("<tr><td class=\"cap-sub\">").append(escapeHtml(subTitle)).append("</td></tr>");
        for (Iterator<JsonNode> it = arr.elements(); it.hasNext();) {
            JsonNode n = it.next();
            if (n.isTextual()) {
                sb.append("<tr><td class=\"bull\">").append(escapeHtml(n.asText())).append("</td></tr>");
            }
        }
    }

    private void appendLegacyBody(StringBuilder sb, TravelPlan plan) {
        appendLegacySection(sb, "Medical considerations", plan.getMedicalConsiderations());
        appendLegacySection(sb, "Vaccinations", plan.getVaccinations());
        appendLegacySection(sb, "Health alerts", plan.getHealthAlerts());
        appendLegacySection(sb, "Safety advisories", plan.getSafetyAdvisories());
        appendLegacySection(sb, "Medications", plan.getMedications());
        appendLegacySection(sb, "Water &amp; food safety", plan.getWaterFood());
        appendLegacySection(sb, "Emergency contacts", plan.getEmergencyContacts());
    }

    private void appendLegacySection(StringBuilder sb, String title, String raw) {
        if (!StringUtils.hasText(raw)) {
            return;
        }
        sb.append("<table class=\"sec\" cellspacing=\"0\" cellpadding=\"0\">")
                .append("<tr><td class=\"cap\">").append(title).append("</td></tr>")
                .append("<tr><td class=\"val\" style=\"border:none\">").append(escapeHtml(raw.trim()))
                .append("</td></tr>")
                .append("</table>");
    }

    private void appendMedicalDisclaimer(StringBuilder sb, JsonNode root) {
        sb.append("<div class=\"disclaimer\">")
                .append("<strong>Important Medical Disclaimer</strong>\n")
                .append(escapeHtml(resolveMedicalDisclaimer(root)))
                .append("</div>");
    }

    private String resolveMedicalDisclaimer(JsonNode root) {
        if (root != null && root.path("medicalDisclaimer").isTextual()) {
            String structured = root.get("medicalDisclaimer").asText();
            if (StringUtils.hasText(structured)) {
                return structured;
            }
        }
        return IMPORTANT_MEDICAL_DISCLAIMER;
    }

    // private void appendGenerationFooter(StringBuilder sb, GeneratedPlan gp) {
    // if (gp == null || !"active".equalsIgnoreCase(gp.getStatus())) {
    // return;
    // }
    // StringBuilder line = new StringBuilder();
    // if (StringUtils.hasText(gp.getProvider())) {
    // line.append(gp.getProvider());
    // }
    // if (StringUtils.hasText(gp.getModelUsed())) {
    // if (!line.isEmpty()) {
    // line.append(" · ");
    // }
    // line.append(gp.getModelUsed());
    // }
    // if (gp.getTokensUsed() != null) {
    // if (!line.isEmpty()) {
    // line.append(" · ");
    // }
    // line.append("~").append(gp.getTokensUsed()).append(" tokens");
    // }
    // if (gp.getProcessingTimeMs() != null) {
    // if (!line.isEmpty()) {
    // line.append(" · ");
    // }
    // line.append(String.format("%.1fs", gp.getProcessingTimeMs() / 1000.0));
    // }
    // if (!line.isEmpty()) {
    // sb.append("<p class=\"gen-foot\">Generated with
    // ").append(escapeHtml(line.toString())).append("</p>");
    // }
    // }

    // ── Pure utility ──────────────────────────────────────────────────────────
    private JsonNode parseStructuredJson(GeneratedPlan gp) {
        if (gp == null || !StringUtils.hasText(gp.getPlanJson())) {
            return null;
        }
        if (!"active".equalsIgnoreCase(gp.getStatus())) {
            return null;
        }
        try {
            JsonNode n = objectMapper.readTree(gp.getPlanJson().trim());
            return (n != null && n.isObject()) ? n : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String formatReturnDatesFromPlan(TravelPlan plan) {
        if (!StringUtils.hasText(plan.getTripDetailsJson())) {
            return "";
        }
        try {
            JsonNode root = objectMapper.readTree(plan.getTripDetailsJson());
            if (!root.path("tripType").isTextual()) {
                return "";
            }
            String tt = root.get("tripType").asText("");
            if (!"return".equalsIgnoreCase(tt)) {
                return "";
            }
            String dep = root.path("departureDate").isTextual() ? root.get("departureDate").asText().trim() : "";
            String ret = root.path("returnDate").isTextual() ? root.get("returnDate").asText().trim() : "";
            if (!StringUtils.hasText(dep) || !StringUtils.hasText(ret)) {
                return "";
            }
            return dep + " \u2013 " + ret;
        } catch (Exception e) {
            return "";
        }
    }

    private static String textOrNull(JsonNode o, String field) {
        if (!o.path(field).isTextual()) {
            return null;
        }
        String t = o.get(field).asText();
        return StringUtils.hasText(t) ? t : null;
    }

    private static String levelClass(String level) {
        String u = level != null ? level.toUpperCase() : "";
        if (u.contains("HIGH")) {
            return "lvl-h";
        }
        if (u.contains("MODERATE")) {
            return "lvl-m";
        }
        return "lvl-l";
    }

    private static boolean afterReturnNonEmpty(JsonNode ar) {
        return (ar.path("redFlag").isTextual() && StringUtils.hasText(ar.get("redFlag").asText()))
                || (ar.path("within1Week").isArray() && ar.get("within1Week").size() > 0)
                || (ar.path("within4Weeks").isArray() && ar.get("within4Weeks").size() > 0)
                || (ar.path("beyond4Weeks").isArray() && ar.get("beyond4Weeks").size() > 0);
    }

    private static boolean medicalCareNonEmpty(JsonNode mc) {
        return (mc.path("clinics").isArray() && mc.get("clinics").size() > 0)
                || (mc.path("embassyContacts").isArray() && mc.get("embassyContacts").size() > 0)
                || (mc.path("emergencyContacts").isArray() && mc.get("emergencyContacts").size() > 0);
    }

    private static boolean itineraryNonEmpty(JsonNode itinerary) {
        return (itinerary.path("tripType").isTextual() && StringUtils.hasText(itinerary.get("tripType").asText()))
                || (itinerary.path("summary").isTextual() && StringUtils.hasText(itinerary.get("summary").asText()))
                || (itinerary.path("routeAdvice").isArray() && itinerary.get("routeAdvice").size() > 0)
                || (itinerary.path("returnGuidance").isArray() && itinerary.get("returnGuidance").size() > 0);
    }

    private static boolean flightNonEmpty(JsonNode flight) {
        return (flight.path("vteRiskLevel").isTextual() && StringUtils.hasText(flight.get("vteRiskLevel").asText()))
                || (flight.path("preventionMeasures").isArray() && flight.get("preventionMeasures").size() > 0)
                || flight.path("medifClearanceRequired").asBoolean(false)
                || (flight.path("medicationTimingGuidance").isTextual()
                        && StringUtils.hasText(flight.get("medicationTimingGuidance").asText()));
    }

    private static boolean malariaNonEmpty(JsonNode malaria) {
        return (malaria.path("riskLevel").isTextual() && StringUtils.hasText(malaria.get("riskLevel").asText()))
                || (malaria.path("recommendedAgent").isTextual()
                        && StringUtils.hasText(malaria.get("recommendedAgent").asText()))
                || (malaria.path("mosquitoProtection").isArray() && malaria.get("mosquitoProtection").size() > 0)
                || (malaria.path("contraindications").isTextual()
                        && StringUtils.hasText(malaria.get("contraindications").asText()));
    }

    private static boolean medicationLogisticsNonEmpty(JsonNode medLog) {
        return (medLog.path("packaging").isTextual() && StringUtils.hasText(medLog.get("packaging").asText()))
                || (medLog.path("supplyRule").isTextual() && StringUtils.hasText(medLog.get("supplyRule").asText()))
                || medLog.path("destinationLegalityCheck").asBoolean(false)
                || medLog.path("coldChainRequired").asBoolean(false);
    }

    private static int countMedicationLogisticsRows(JsonNode medLog) {
        int n = 0;
        if (StringUtils.hasText(textOrNull(medLog, "packaging")))
            n++;
        if (StringUtils.hasText(textOrNull(medLog, "supplyRule")))
            n++;
        if (medLog.path("destinationLegalityCheck").asBoolean(false))
            n++;
        if (medLog.path("coldChainRequired").asBoolean(false))
            n++;
        return n;
    }

    private static boolean sexualHealthNonEmpty(JsonNode sh) {
        return (sh.path("riskLevel").isTextual() && StringUtils.hasText(sh.get("riskLevel").asText()))
                || (sh.path("preventionAdvice").isArray() && sh.get("preventionAdvice").size() > 0)
                || sh.path("prepPepDiscussion").asBoolean(false);
    }

    private static boolean pregnancyNonEmpty(JsonNode pg) {
        return (pg.path("trimesterSpecificAdvice").isTextual()
                && StringUtils.hasText(pg.get("trimesterSpecificAdvice").asText()))
                || (pg.path("antimalarialSafety").isTextual()
                        && StringUtils.hasText(pg.get("antimalarialSafety").asText()))
                || (pg.path("liveVaccineContraindications").isArray()
                        && pg.get("liveVaccineContraindications").size() > 0)
                || (pg.path("airlineRestrictions").isTextual()
                        && StringUtils.hasText(pg.get("airlineRestrictions").asText()))
                || (pg.path("contraceptionCounselling").isTextual()
                        && StringUtils.hasText(pg.get("contraceptionCounselling").asText()));
    }

    private static String urgencyBadgeClass(String urgency) {
        String u = urgency != null ? urgency.toUpperCase() : "";
        if (u.contains("URGENT")) {
            return "urgency-urgent";
        }
        if (u.contains("BEFORE")) {
            return "urgency-before";
        }
        return "urgency-routine";
    }

    private static String riskLabel(Integer score) {
        if (score == null) {
            return "Unknown";
        }
        if (score <= 1) {
            return "Low";
        }
        if (score == 2) {
            return "Moderate";
        }
        return "High";
    }

    private static String riskBadgeClass(Integer score) {
        if (score == null) {
            return "b-nu";
        }
        if (score <= 1) {
            return "b-low";
        }
        if (score == 2) {
            return "b-mod";
        }
        return "b-hi";
    }

    private static String nullSafe(String s) {
        return s != null ? s : "";
    }

    public byte[] generateSignedPdf(TravelPlan plan, GeneratedPlan generatedPlan,
            com.TravelMedicineAdvisory.Server.domain.user.User doctor,
            com.TravelMedicineAdvisory.Server.domain.usersetting.UserSetting doctorSettings) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            String baseHtml = buildHtml(plan, generatedPlan);
            String signedHtml = baseHtml.replace(
                    "</body></html>",
                    buildDoctorVerificationSection(doctor, doctorSettings) + "</body></html>");
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(signedHtml, frontendUrl);
            builder.toStream(out);
            builder.run();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate signed travel plan PDF", e);
        }
    }

    private String buildDoctorVerificationSection(com.TravelMedicineAdvisory.Server.domain.user.User doctor,
            com.TravelMedicineAdvisory.Server.domain.usersetting.UserSetting doctorSettings) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div style='page-break-before:always;padding:14pt 16mm 0;'>");
        sb.append("<table class='sec' cellspacing='0' cellpadding='0'><tr>");
        sb.append("<td class='cap' style='background:").append(TEAL_DEEP)
                .append(";color:#ffffff;'>Doctor Verification &amp; Digital Signature</td>");
        sb.append("</tr><tr><td style='padding:0;'>");
        sb.append("<div style='padding:12px;position:relative;'>");

        boolean hasStamp = StringUtils.hasText(doctorSettings.getStampUrl());
        boolean hasSignature = StringUtils.hasText(doctorSettings.getSignatureUrl());

        // Stamp as watermark behind content
        if (hasStamp) {
            sb.append("<div style='position:absolute;top:12px;right:12px;opacity:0.2;'>");
            sb.append("<img src='").append(resolveImageSrc(doctorSettings.getStampUrl()))
                    .append("' style='max-height:220px;max-width:200px;' alt='Official Stamp'/>");
            sb.append("</div>");
        }

        sb.append("<p style='margin:0 0 8px;font-size:10pt;color:").append(DARK).append(";'>");
        sb.append(
                "This travel health plan has been reviewed and approved by a licensed physician on the TMAG Doctor Network.");
        sb.append("</p>");
        sb.append("<table style='width:100%;border-collapse:collapse;margin-top:10px;'>");
        sb.append("<tr><td style='width:30%;font-size:8pt;color:").append(MUTED)
                .append(
                        ";font-weight:700;text-transform:uppercase;letter-spacing:0.04em;padding:6px 0;border-bottom:1px solid ")
                .append(BORDER_LT).append(";'>Physician Name</td>");
        sb.append("<td style='font-size:9.5pt;color:").append(DARK).append(";padding:6px 0;border-bottom:1px solid ")
                .append(BORDER_LT).append(";'>").append(escapeHtml(doctor.getFullName())).append("</td></tr>");
        sb.append("<tr><td style='font-size:8pt;color:").append(MUTED)
                .append(
                        ";font-weight:700;text-transform:uppercase;letter-spacing:0.04em;padding:6px 0;border-bottom:1px solid ")
                .append(BORDER_LT).append(";'>License Number</td>");
        sb.append("<td style='font-size:9.5pt;color:").append(DARK).append(";padding:6px 0;border-bottom:1px solid ")
                .append(BORDER_LT).append(";'>").append(escapeHtml(doctorSettings.getMedicalLicenseNumber()))
                .append("</td></tr>");
        sb.append("<tr><td style='font-size:8pt;color:").append(MUTED)
                .append(
                        ";font-weight:700;text-transform:uppercase;letter-spacing:0.04em;padding:6px 0;border-bottom:1px solid ")
                .append(BORDER_LT).append(";'>Validation Date</td>");
        sb.append("<td style='font-size:9.5pt;color:").append(DARK).append(";padding:6px 0;border-bottom:1px solid ")
                .append(BORDER_LT).append(";'>")
                .append(java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")))
                .append("</td></tr>");
        sb.append("<tr><td style='font-size:8pt;color:").append(MUTED)
                .append(";font-weight:700;text-transform:uppercase;letter-spacing:0.04em;padding:6px 0;'>Status</td>");
        sb.append("<td style='font-size:9.5pt;color:").append(GREEN)
                .append(";font-weight:700;padding:6px 0;'>TMAG Verified &amp; Approved</td></tr>");
        sb.append("</table>");

        // Signature below doctor info
        if (hasSignature) {
            sb.append("<table style='width:100%;margin-top:16px;border-collapse:collapse;'>");
            sb.append("<tr><td style='text-align:center;padding:5px;'>");
            sb.append("<img src='").append(resolveImageSrc(doctorSettings.getSignatureUrl()))
                    .append("' style='height:250px;width:250px;' alt='Doctor Signature'/>");
            sb.append("<p style='margin-top:4px;font-size:8pt;color:").append(MUTED).append(";'>Digital Signature</p>");
            sb.append("</td></tr>");
            sb.append("</table>");
        }

        sb.append("</div>");
        sb.append("</td></tr></table>");
        sb.append("<div style='margin-top:14pt;text-align:center;padding:12px;background:").append(GOLD_SOFT)
                .append(";border:1px solid ").append(GOLD_BORDER).append(";border-radius:4px;'>");
        sb.append("<p style='margin:0;font-size:9pt;color:").append(DARK)
                .append(";font-weight:600;'>TMAG Verified Seal</p>");
        sb.append("<p style='margin:4px 0 0;font-size:7.5pt;color:").append(MUTED).append(
                ";'>This document was generated by Travel Medicine Advisory Global and approved by a licensed physician.</p>");
        sb.append("</div>");
        sb.append("</div>");
        return sb.toString();
    }

    private String resolveImageSrc(String url) {
        if (!StringUtils.hasText(url))
            return null;
        try (java.io.InputStream in = new java.net.URI(url).toURL().openStream()) {
            byte[] bytes = in.readAllBytes();
            String lower = url.toLowerCase();
            String mime = lower.endsWith(".jpg") || lower.endsWith(".jpeg") ? "image/jpeg"
                    : lower.endsWith(".webp") ? "image/webp"
                            : lower.endsWith(".gif") ? "image/gif"
                                    : "image/png";
            return "data:" + mime + ";base64," + java.util.Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            log.warn("Could not load image for PDF embed, falling back to URL: {}", url);
            return url;
        }
    }

    private static String escapeHtml(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
