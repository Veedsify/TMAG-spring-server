package com.TravelMedicineAdvisory.Server.domain.invoice;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class InvoicePdfGenerator {

    /** Aligns with client `index.css` @theme tokens */
    private static final String BG_PAGE = "#f6f0e9";
    private static final String BG_CARD = "#fffdf9";
    private static final String BG_SUBTLE = "#fcf6ef";
    private static final String BG_MUTED_ROW = "#efe7dd";
    private static final String DARK = "#2a1e14";
    private static final String HEADING = "#3d2c1e";
    private static final String MUTED = "#7a6a5a";
    private static final String BODY = "#8a7968";
    private static final String BORDER_LIGHT = "#e8ddd3";
    private static final String ACCENT = "#2a7a6a";
    private static final String ACCENT_SOFT = "#e8f4f1";
    private static final String GOLD = "#c4953a";
    private static final String GOLD_SOFT = "#fdf3e3";

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public byte[] generateInvoicePdf(InvoiceResponse invoice, String companyName) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            String html = buildHtml(invoice, companyName);

            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(html, frontendUrl);
            builder.toStream(out);
            builder.run();

            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate invoice PDF", e);
        }
    }

    private String buildHtml(InvoiceResponse invoice, String companyName) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\" />");
        sb.append("<style>");
        sb.append(
                "@font-face{font-family:'Hanken Grotesk';src:url('classpath:/fonts/HankenGrotesk-Regular.ttf') format('truetype');font-weight:400;font-style:normal}");
        sb.append(
                "@font-face{font-family:'Hanken Grotesk';src:url('classpath:/fonts/HankenGrotesk-SemiBold.ttf') format('truetype');font-weight:600;font-style:normal}");
        sb.append(
                "@font-face{font-family:'Hanken Grotesk';src:url('classpath:/fonts/HankenGrotesk-Bold.ttf') format('truetype');font-weight:700;font-style:normal}");
        sb.append(
                "@font-face{font-family:'Fraunces';src:url('classpath:/fonts/Fraunces-Regular.ttf') format('truetype');font-weight:400;font-style:normal}");
        sb.append(
                "@font-face{font-family:'Fraunces';src:url('classpath:/fonts/Fraunces-Bold.ttf') format('truetype');font-weight:700;font-style:normal}");
        sb.append("*,*::before,*::after{margin:0;padding:0;box-sizing:border-box}");
        sb.append("@page{size:A4;margin:28pt}");
        sb.append("body{font-family:'Hanken Grotesk',Arial,sans-serif;font-size:10pt;color:")
                .append(BODY)
                .append(";background-color:")
                .append(BG_PAGE)
                .append(";line-height:1.45;-webkit-print-color-adjust:exact;print-color-adjust:exact}");
        sb.append(".wrap{padding:8px 0 24px}");
        sb.append(".shell{background-color:")
                .append(BG_CARD)
                .append(";border:1px solid ")
                .append(BORDER_LIGHT)
                .append(";border-radius:14px;overflow:hidden}");
        sb.append(".head{background-color:")
                .append(DARK)
                .append(";padding:26px 36px 0}");
        sb.append(".head-row{display:table;width:100%}");
        sb.append(".head-row>div{display:table-cell;vertical-align:bottom}");
        sb.append(".head-left{width:58%}");
        sb.append(".head-right{width:42%;text-align:right;padding-bottom:4px}");
        sb.append(".head-eyebrow{font-size:7pt;letter-spacing:2.5px;text-transform:uppercase;color:rgba(255,255,255,0.5);margin-bottom:6px}");
        sb.append(".head-logo{font-family:'Fraunces',serif;font-size:24pt;font-weight:400;color:#fff;line-height:1.05}");
        sb.append(".head-tag{font-size:7pt;letter-spacing:2px;text-transform:uppercase;color:rgba(255,255,255,0.45);margin-top:6px}");
        sb.append(".head-doc{font-family:'Fraunces',serif;font-size:22pt;font-weight:400;color:#fff;letter-spacing:0.02em}");
        sb.append(".head-inv{font-size:9pt;color:rgba(255,255,255,0.65);margin-top:8px;font-weight:600;letter-spacing:0.08em}");
        sb.append(".accent-bar{height:3px;background-color:")
                .append(ACCENT)
                .append(";margin-top:22px;border-radius:2px}");
        sb.append(".meta{padding:20px 36px;background-color:")
                .append(BG_SUBTLE)
                .append(";border-bottom:1px solid ")
                .append(BORDER_LIGHT)
                .append("}");
        sb.append(".meta-table{width:100%;border-collapse:collapse}");
        sb.append(".meta-table td{width:25%;vertical-align:top;padding:4px 12px 4px 0}");
        sb.append(".meta-table td:last-child{padding-right:0}");
        sb.append(".mk{font-size:7pt;font-weight:700;color:")
                .append(MUTED)
                .append(";text-transform:uppercase;letter-spacing:1.2px;margin-bottom:4px}");
        sb.append(".mv{font-size:10pt;font-weight:600;color:")
                .append(HEADING)
                .append("}");
        sb.append(".badge{display:inline-block;padding:4px 12px;border-radius:999px;font-size:7pt;font-weight:700;text-transform:uppercase;letter-spacing:0.6px}");
        sb.append(".badge-paid{background-color:")
                .append(ACCENT_SOFT)
                .append(";color:")
                .append(ACCENT)
                .append("}");
        sb.append(".badge-pending{background-color:")
                .append(GOLD_SOFT)
                .append(";color:")
                .append(GOLD)
                .append("}");
        sb.append(".body{padding:28px 36px 32px}");
        sb.append(".bill{margin-bottom:26px}");
        sb.append(".section-label{font-size:7pt;font-weight:700;color:")
                .append(MUTED)
                .append(";text-transform:uppercase;letter-spacing:2px;margin-bottom:8px}");
        sb.append(".section-label-line{height:2px;width:36px;background-color:")
                .append(ACCENT)
                .append(";border-radius:1px;margin-bottom:12px}");
        sb.append(".bill-name{font-size:13pt;font-weight:700;color:")
                .append(HEADING)
                .append(";line-height:1.25}");
        sb.append(".bill-meta{font-size:8pt;color:")
                .append(MUTED)
                .append(";margin-top:4px}");
        sb.append("table.line-items{width:100%;border-collapse:separate;border-spacing:0;margin-bottom:22px;border:1px solid ")
                .append(BORDER_LIGHT)
                .append(";border-radius:10px;overflow:hidden}");
        sb.append("thead th{padding:11px 16px;text-align:left;font-size:7pt;font-weight:700;color:")
                .append(HEADING)
                .append(";text-transform:uppercase;letter-spacing:1px;background-color:")
                .append(BG_MUTED_ROW)
                .append(";border-bottom:1px solid ")
                .append(BORDER_LIGHT)
                .append("}");
        sb.append("thead th:not(:first-child){text-align:right}");
        sb.append("tbody td{padding:15px 16px;font-size:10pt;border-bottom:1px solid ")
                .append(BORDER_LIGHT)
                .append(";color:")
                .append(HEADING)
                .append("}");
        sb.append("tbody td:not(:first-child){text-align:right;font-weight:600}");
        sb.append("tbody tr:last-child td{border-bottom:none}");
        sb.append(".item-title{font-weight:700;font-size:10.5pt;color:")
                .append(HEADING)
                .append("}");
        sb.append(".item-sub{font-size:8pt;color:")
                .append(MUTED)
                .append(";margin-top:3px;font-weight:400}");
        sb.append(".totals-wrap{display:table;width:100%}");
        sb.append(".totals-spacer{display:table-cell;width:52%}");
        sb.append(".totals-card{display:table-cell;width:48%;vertical-align:top}");
        sb.append(".totals-inner{border:1px solid ")
                .append(BORDER_LIGHT)
                .append(";border-radius:10px;overflow:hidden;background-color:")
                .append(BG_CARD)
                .append("}");
        sb.append(".tot-row{display:table;width:100%;padding:10px 18px}");
        sb.append(".tot-row:not(:last-of-type){border-bottom:1px solid ")
                .append(BORDER_LIGHT)
                .append("}");
        sb.append(".tot-label{display:table-cell;font-size:9pt;color:")
                .append(MUTED)
                .append("}");
        sb.append(".tot-value{display:table-cell;text-align:right;font-size:9pt;font-weight:600;color:")
                .append(HEADING)
                .append("}");
        sb.append(".tot-row-total{background-color:")
                .append(ACCENT_SOFT)
                .append(";padding:14px 18px;display:table;width:100%}");
        sb.append(".tot-row-total .tot-label{font-size:10pt;font-weight:700;color:")
                .append(HEADING)
                .append(";display:table-cell}");
        sb.append(".tot-row-total .tot-value{font-family:'Fraunces',serif;font-size:14pt;font-weight:400;color:")
                .append(ACCENT)
                .append(";display:table-cell;text-align:right}");
        sb.append(".details{margin-top:26px;display:table;width:100%}");
        sb.append(".details>div{display:table-cell;width:50%;vertical-align:top}");
        sb.append(".details>div:first-child{padding-right:10px}");
        sb.append(".details>div:last-child{padding-left:10px}");
        sb.append(".detail-box{border:1px solid ")
                .append(BORDER_LIGHT)
                .append(";border-radius:10px;padding:14px 16px;background-color:")
                .append(BG_CARD)
                .append(";min-height:88px}");
        sb.append(".detail-h{font-size:7pt;font-weight:700;color:")
                .append(HEADING)
                .append(";text-transform:uppercase;letter-spacing:1.2px;margin-bottom:10px;padding-bottom:8px;border-bottom:1px solid ")
                .append(BORDER_LIGHT)
                .append("}");
        sb.append(".detail-line{font-size:9pt;color:")
                .append(MUTED)
                .append(";line-height:1.85}");
        sb.append(".detail-line strong{color:")
                .append(HEADING)
                .append(";font-weight:600}");
        sb.append(".foot{margin-top:28px;padding-top:22px;border-top:1px solid ")
                .append(BORDER_LIGHT)
                .append(";display:table;width:100%}");
        sb.append(".foot>div{display:table-cell;vertical-align:bottom}");
        sb.append(".foot-right{text-align:right}");
        sb.append(".thanks{font-family:'Fraunces',serif;font-size:15pt;font-weight:400;color:")
                .append(ACCENT)
                .append(";line-height:1.2}");
        sb.append(".thanks-sub{font-size:8pt;color:")
                .append(MUTED)
                .append(";margin-top:6px;max-width:280px}");
        sb.append(".contact-label{font-size:7pt;color:")
                .append(MUTED)
                .append(";text-transform:uppercase;letter-spacing:1px}");
        sb.append(".contact-val{font-size:9pt;font-weight:600;color:")
                .append(HEADING)
                .append(";margin-top:3px}");
        sb.append("</style></head><body>");
        sb.append("<div class=\"wrap\"><div class=\"shell\">");

        sb.append("<div class=\"head\"><div class=\"head-row\">");
        sb.append("<div class=\"head-left\">");
        sb.append("<div class=\"head-eyebrow\">Travel Medicine Advisory</div>");
        sb.append("<div class=\"head-logo\">TMAG</div>");
        sb.append("<div class=\"head-tag\">Global Advisory Platform</div>");
        sb.append("</div>");
        sb.append("<div class=\"head-right\">");
        sb.append("<div class=\"head-doc\">Invoice</div>");
        sb.append("<div class=\"head-inv\">INV-").append(String.format("%06d", invoice.id())).append("</div>");
        sb.append("</div></div>");
        sb.append("<div class=\"accent-bar\"></div></div>");

        boolean isPaid = invoice.status() != null && invoice.status().equalsIgnoreCase("paid");
        String badgeClass = isPaid ? "badge-paid" : "badge-pending";
        String statusLabel = capitalize(invoice.status());
        String currency = invoice.currency() != null ? invoice.currency() : "USD";

        sb.append("<div class=\"meta\"><table class=\"meta-table\"><tr>");
        sb.append("<td><div class=\"mk\">Invoice date</div><div class=\"mv\">")
                .append(formatDate(invoice.issuedAt()))
                .append("</div></td>");
        if (invoice.dueDate() != null) {
            sb.append("<td><div class=\"mk\">Due date</div><div class=\"mv\">")
                    .append(formatDate(invoice.dueDate()))
                    .append("</div></td>");
        } else {
            sb.append("<td><div class=\"mk\">Due date</div><div class=\"mv\">—</div></td>");
        }
        sb.append("<td><div class=\"mk\">Status</div><div class=\"mv\"><span class=\"badge ")
                .append(badgeClass)
                .append("\">")
                .append(statusLabel)
                .append("</span></div></td>");
        sb.append("<td><div class=\"mk\">Currency</div><div class=\"mv\">").append(currency).append("</div></td>");
        sb.append("</tr></table></div>");

        sb.append("<div class=\"body\">");
        sb.append("<div class=\"bill\">");
        sb.append("<div class=\"section-label\">Bill to</div>");
        sb.append("<div class=\"section-label-line\"></div>");
        sb.append("<div class=\"bill-name\">").append(escapeHtml(companyName != null ? companyName : "Customer")).append("</div>");
        if (invoice.companyId() != null) {
            sb.append("<div class=\"bill-meta\">Company #").append(invoice.companyId()).append("</div>");
        }
        sb.append("</div>");

        String price = formatCurrency(invoice.amount(), invoice.currency());
        String zero = formatCurrency(BigDecimal.ZERO, invoice.currency());

        sb.append(
                "<table class=\"line-items\"><thead><tr><th>Description</th><th>Qty</th><th>Unit price</th><th>Amount</th></tr></thead><tbody>");
        sb.append("<tr>");
        sb.append("<td><div class=\"item-title\">")
                .append(escapeHtml(invoice.description() != null ? invoice.description() : "Credit purchase"))
                .append("</div>");
        sb.append("<div class=\"item-sub\">TMAG platform credits</div></td>");
        sb.append("<td>1</td>");
        sb.append("<td>").append(price).append("</td>");
        sb.append("<td>").append(price).append("</td>");
        sb.append("</tr></tbody></table>");

        sb.append("<div class=\"totals-wrap\"><div class=\"totals-spacer\"></div><div class=\"totals-card\">");
        sb.append("<div class=\"totals-inner\">");
        sb.append("<div class=\"tot-row\"><span class=\"tot-label\">Subtotal</span><span class=\"tot-value\">")
                .append(price)
                .append("</span></div>");
        sb.append("<div class=\"tot-row\"><span class=\"tot-label\">Tax (0%)</span><span class=\"tot-value\">")
                .append(zero)
                .append("</span></div>");
        sb.append("<div class=\"tot-row-total\"><span class=\"tot-label\">Total due</span><span class=\"tot-value\">")
                .append(price)
                .append("</span></div>");
        sb.append("</div></div></div>");

        sb.append("<div class=\"details\">");
        sb.append("<div><div class=\"detail-box\">");
        sb.append("<div class=\"detail-h\">Payment</div>");
        sb.append("<div class=\"detail-line\">Method: <strong>")
                .append(escapeHtml(invoice.paymentMethod() != null ? invoice.paymentMethod() : "Online payment"))
                .append("</strong></div>");
        sb.append("<div class=\"detail-line\">Currency: <strong>").append(currency).append("</strong></div>");
        sb.append("</div></div>");
        sb.append("<div><div class=\"detail-box\">");
        sb.append("<div class=\"detail-h\">Transaction</div>");
        sb.append("<div class=\"detail-line\">Reference: <strong>#").append(invoice.id()).append("</strong></div>");
        sb.append("<div class=\"detail-line\">Created: <strong>").append(formatDateTime(invoice.createdAt())).append("</strong></div>");
        if (invoice.paidAt() != null) {
            sb.append("<div class=\"detail-line\">Paid: <strong>").append(formatDateTime(invoice.paidAt())).append("</strong></div>");
        }
        sb.append("</div></div></div>");

        sb.append("<div class=\"foot\">");
        sb.append("<div class=\"foot-left\">");
        sb.append("<div class=\"thanks\">Thank you for choosing TMAG</div>");
        sb.append("<div class=\"thanks-sub\">Travel Medicine Advisory Global — professional travel health planning for your team.</div>");
        sb.append("</div>");
        sb.append("<div class=\"foot-right\">");
        sb.append("<div class=\"contact-label\">Questions?</div>");
        sb.append("<div class=\"contact-val\">support@tmag.com</div>");
        sb.append("<div class=\"contact-val\">www.tmag.com</div>");
        sb.append("</div></div>");

        sb.append("</div></div></div></body></html>");
        return sb.toString();
    }

    private String formatDate(LocalDateTime dateTime) {
        if (dateTime == null)
            return "—";
        return dateTime.format(DateTimeFormatter.ofPattern("MMM d, yyyy"));
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null)
            return "—";
        return dateTime.format(DateTimeFormatter.ofPattern("MMM d, yyyy • h:mm a"));
    }

    private String formatCurrency(BigDecimal amount, String currency) {
        if (amount == null)
            amount = BigDecimal.ZERO;
        String symbol = switch (currency) {
            case "NGN" -> "₦";
            case "EUR" -> "€";
            case "GBP" -> "£";
            default -> "$";
        };
        return symbol + String.format("%,.2f", amount);
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty())
            return "—";
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    private String escapeHtml(String str) {
        if (str == null)
            return "";
        return str.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
