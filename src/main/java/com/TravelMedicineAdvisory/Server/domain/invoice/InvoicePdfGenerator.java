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

    @Value("${app.frontend.url}")
    private String frontendUrl;

        // private static final String ACCENT = "#2a7a6a";
        // private static final String ACCENT_LIGHT = "#e8f4f1";
        // private static final String GOLD = "#c4953a";
        // private static final String GOLD_LIGHT = "#fdf3e3";
        // private static final String HEADING = "#3d2c1e";
        // private static final String MUTED = "#7a6a5a";
        // private static final String BODY = "#8a7968";
        // private static final String BORDER = "#d4c4b4";
        // private static final String BORDER_LIGHT = "#e8ddd3";
        // private static final String BG_CREAM = "#f6f0e9";
        // private static final String BG_WHITE = "#fffdf9";
        // private static final String CARD_SHADOW = "rgba(61,44,30,0.08)";

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
        sb.append(
                "body{font-family:'Hanken Grotesk',Arial,sans-serif;font-size:10pt;color:#8a7968;background-color:#f6f0e9;padding:0}");
        sb.append(".page{padding:40px 48px}");
        sb.append(
                ".top-band{background-color:#2a7a6a;padding:28px 48px;border-radius:12px 12px 0 0;margin-bottom:2px}");
        sb.append(".top-band-inner{display:table;width:100%}");
        sb.append(".top-band-inner>div{display:table-cell;vertical-align:middle}");
        sb.append(".top-band-right{text-align:right;vertical-align:bottom}");
        sb.append(
                ".top-band-label{font-size:7pt;color:rgba(255,255,255,0.65);letter-spacing:2px;text-transform:uppercase;margin-bottom:4px}");
        sb.append(
                ".top-band-brand{font-family:'Fraunces',serif;font-size:26pt;font-weight:700;color:#fff;line-height:1}");
        sb.append(
                ".top-band-sub{font-size:7pt;color:rgba(255,255,255,0.65);letter-spacing:2px;text-transform:uppercase;margin-top:4px}");
        sb.append(
                ".top-band-title{font-family:'Fraunces',serif;font-size:28pt;font-weight:700;color:rgb(255,255,255);}");
        sb.append(".top-band-num{font-size:9pt;color:rgba(255,255,255,0.7);margin-top:6px;letter-spacing:1px}");
        sb.append(
                ".invoice-info-strip{background-color:#fff;border-left:3px solid #2a7a6a;border-right:3px solid #2a7a6a;padding:16px 24px;display:table;width:100%}");
        sb.append(".invoice-info-strip>div{display:table-cell;vertical-align:middle}");
        sb.append(".invoice-info-left{width:50%}");
        sb.append(".invoice-info-right{width:50%;text-align:right}");
        sb.append(
                ".info-label{font-size:7pt;font-weight:700;color:#3d2c1e;text-transform:uppercase;letter-spacing:1px;margin-bottom:2px}");
        sb.append(".info-value{font-size:10pt;font-weight:600;color:#3d2c1e}");
        sb.append(".info-row{margin-bottom:8px}");
        sb.append(".info-row:last-child{margin-bottom:0}");
        sb.append(
                ".badge{display:inline-block;padding:3px 10px;border-radius:20px;font-size:7pt;font-weight:700;text-transform:uppercase;letter-spacing:0.5px}");
        sb.append(".badge-paid{background-color:#e8f4f1;color:#2a7a6a}");
        sb.append(".badge-pending{background-color:#fdf3e3;color:#c4953a}");
        sb.append(
                ".body-card{background-color:#fff;border-left:3px solid #2a7a6a;border-right:3px solid #2a7a6a;padding:28px 24px}");
        sb.append(".bill-to-section{margin-bottom:28px}");
        sb.append(
                ".section-eyebrow{font-size:7pt;font-weight:700;color:#7a6a5a;text-transform:uppercase;letter-spacing:2px;margin-bottom:6px;padding-bottom:6px;border-bottom:1px solid #e8ddd3}");
        sb.append(".bill-to-name{font-size:14pt;font-weight:700;color:#3d2c1e;line-height:1.2}");
        sb.append(".bill-to-meta{font-size:8pt;color:#7a6a5a;margin-top:3px}");
        sb.append("table{width:100%;border-collapse:collapse;margin-bottom:24px}");
        sb.append("thead{background-color:#f6f0e9}");
        sb.append(
                "th{padding:10px 14px;text-align:left;font-size:7pt;font-weight:700;color:#3d2c1e;text-transform:uppercase;letter-spacing:1px}");
        sb.append("th:first-child{border-radius:8px 0 0 8px}");
        sb.append("th:last-child{border-radius:0 8px 8px 0}");
        sb.append("th:not(:first-child){text-align:right}");
        sb.append("td{padding:14px;font-size:10pt;border-bottom:1px solid #e8ddd3;color:#3d2c1e}");
        sb.append("td:not(:first-child){text-align:right}");
        sb.append("tr:last-child td{border-bottom:none}");
        sb.append(".item-name{font-weight:700;font-size:11pt;color:#3d2c1e}");
        sb.append(".item-desc{font-size:8pt;color:#7a6a5a;margin-top:3px}");
        sb.append(".summary-section{display:table;width:100%;gap:16px}");
        sb.append(".summary-left{width:100%}");
        sb.append(
                ".summary-card{background-color:#f6f0e9;border-radius:10px;padding:18px 22px;width:42%;margin-left:auto}");
        sb.append(".summary-row{display:table;width:100%;padding:5px 0}");
        sb.append(".summary-row:not(:last-child){border-bottom:1px solid #e8ddd3}");
        sb.append(".s-label{font-size:9pt;color:#7a6a5a;display:table-cell}");
        sb.append(".s-value{font-size:9pt;font-weight:600;color:#3d2c1e;display:table-cell;text-align:right}");
        sb.append(".summary-total{padding-top:10px;margin-top:4px}");
        sb.append(".summary-total .s-label{font-size:11pt;font-weight:700;color:#3d2c1e}");
        sb.append(".summary-total .s-value{font-size:13pt;font-weight:700;color:#2a7a6a}");
        sb.append(".details-grid{display:table;width:100%;margin-top:24px;gap:16px}");
        sb.append(".details-grid>div{display:table-cell;width:50%;vertical-align:top}");
        sb.append(".details-grid>div:first-child{padding-right:12px}");
        sb.append(".details-grid>div:last-child{padding-left:12px;text-align:right}");
        sb.append(
                ".detail-card{background-color:#fcf6ef;border-radius:8px;padding:14px 16px;border:1px solid #e8ddd3}");
        sb.append(
                ".detail-label{font-size:7pt;font-weight:700;color:#3d2c1e;text-transform:uppercase;letter-spacing:1.5px;margin-bottom:6px;padding-bottom:5px;border-bottom:1px solid #e8ddd3}");
        sb.append(".detail-row{font-size:9pt;color:#7a6a5a;line-height:1.9}");
        sb.append(".detail-row span{color:#3d2c1e;font-weight:600}");
        sb.append(".bottom-band{background-color:#2a7a6a;height:4px;border-radius:0 0 12px 12px}");
        sb.append(".footer{padding:28px 0 8px;display:table;width:100%}");
        sb.append(".footer>div{display:table-cell;vertical-align:bottom}");
        sb.append(".footer-right{text-align:right}");
        sb.append(".footer-thanks{font-family:'Fraunces',serif;font-size:16pt;font-weight:700;color:#2a7a6a}");
        sb.append(".footer-sub{font-size:8pt;color:#7a6a5a;margin-top:3px}");
        sb.append(".footer-contact-label{font-size:7pt;color:#7a6a5a;text-transform:uppercase;letter-spacing:1px}");
        sb.append(".footer-contact-value{font-size:9pt;font-weight:600;color:#3d2c1e;margin-top:2px}");
        sb.append(".footer-divider{height:1px;background-color:#e8ddd3;margin:16px 0 12px}");
        sb.append("</style></head><body>");
        sb.append("<div class=\"page\">");
        sb.append("<div class=\"top-band\">");
        sb.append("<div class=\"top-band-inner\">");
        sb.append("<div class=\"top-band-left\">");
        sb.append("<div class=\"top-band-label\">Travel Medicine Advisory</div>");
        sb.append("<div class=\"top-band-brand\">TMAG</div>");
        sb.append("<div class=\"top-band-sub\">Global Advisory Platform</div>");
        sb.append("</div>");
        sb.append("<div class=\"top-band-right\">");
        sb.append("<div class=\"top-band-title\">INVOICE</div>");
        sb.append("<div class=\"top-band-num\">INV-").append(String.format("%06d", invoice.id())).append("</div>");
        sb.append("</div>");
        sb.append("</div>");
        sb.append("</div>");
        boolean isPaid = invoice.status() != null && invoice.status().equalsIgnoreCase("paid");
        String badgeClass = isPaid ? "badge-paid" : "badge-pending";
        String statusLabel = capitalize(invoice.status());
        sb.append("<div class=\"invoice-info-strip\">");
        sb.append("<div class=\"invoice-info-left\">");
        sb.append("<div class=\"info-row\"><div class=\"info-label\">Invoice Date</div><div class=\"info-value\">")
                .append(formatDate(invoice.issuedAt())).append("</div></div>");
        if (invoice.dueDate() != null) {
            sb.append("<div class=\"info-row\"><div class=\"info-label\">Due Date</div><div class=\"info-value\">")
                    .append(formatDate(invoice.dueDate())).append("</div></div>");
        }
        sb.append("</div>");
        sb.append("<div class=\"invoice-info-right\">");
        sb.append(
                "<div class=\"info-row\"><div class=\"info-label\">Status</div><div class=\"info-value\"><span class=\"badge ")
                .append(badgeClass).append("\">").append(statusLabel).append("</span></div></div>");
        sb.append("<div class=\"info-row\"><div class=\"info-label\">Currency</div><div class=\"info-value\">")
                .append(invoice.currency() != null ? invoice.currency() : "USD").append("</div></div>");
        sb.append("</div>");
        sb.append("</div>");
        sb.append("<div class=\"body-card\">");
        sb.append("<div class=\"bill-to-section\">");
        sb.append("<div class=\"section-eyebrow\">Bill To</div>");
        sb.append("<div class=\"bill-to-name\">").append(escapeHtml(companyName != null ? companyName : "Customer"))
                .append("</div>");
        if (invoice.companyId() != null) {
            sb.append("<div class=\"bill-to-meta\">Company #").append(invoice.companyId()).append("</div>");
        }
        sb.append("</div>");
        String price = formatCurrency(invoice.amount(), invoice.currency());
        String zero = formatCurrency(BigDecimal.ZERO, invoice.currency());
        sb.append(
                "<table><thead><tr><th>Description</th><th>Qty</th><th>Unit Price</th><th>Amount</th></tr></thead><tbody>");
        sb.append("<tr>");
        sb.append("<td><div class=\"item-name\">")
                .append(escapeHtml(invoice.description() != null ? invoice.description() : "Credit Purchase"))
                .append("</div>");
        sb.append("<div class=\"item-desc\">TMAG Platform Credits</div></td>");
        sb.append("<td>1</td>");
        sb.append("<td>").append(price).append("</td>");
        sb.append("<td>").append(price).append("</td>");
        sb.append("</tr></tbody></table>");
        sb.append("<div class=\"summary-section\">");
        sb.append("<div class=\"summary-left\">");
        sb.append("<div class=\"summary-card\">");
        sb.append("<div class=\"summary-row\"><div class=\"s-label\">Subtotal</div><div class=\"s-value\">")
                .append(price).append("</div></div>");
        sb.append("<div class=\"summary-row\"><div class=\"s-label\">Tax (0%)</div><div class=\"s-value\">")
                .append(zero).append("</div></div>");
        sb.append(
                "<div class=\"summary-row summary-total\"><div class=\"s-label\">Total Due</div><div class=\"s-value\">")
                .append(price).append("</div></div>");
        sb.append("</div>");
        sb.append("</div>");
        sb.append("</div>");
        sb.append("<div class=\"details-grid\">");
        sb.append("<div>");
        sb.append("<div class=\"detail-card\">");
        sb.append("<div class=\"detail-label\">Payment Details</div>");
        sb.append("<div class=\"detail-row\">Method: <span>")
                .append(invoice.paymentMethod() != null ? invoice.paymentMethod() : "Online Payment")
                .append("</span></div>");
        sb.append("<div class=\"detail-row\">Currency: <span>")
                .append(invoice.currency() != null ? invoice.currency() : "USD").append("</span></div>");
        sb.append("</div>");
        sb.append("</div>");
        sb.append("<div>");
        sb.append("<div class=\"detail-card\">");
        sb.append("<div class=\"detail-label\">Transaction</div>");
        sb.append("<div class=\"detail-row\">Invoice: <span>#").append(invoice.id()).append("</span></div>");
        sb.append("<div class=\"detail-row\">Created: <span>").append(formatDateTime(invoice.createdAt()))
                .append("</span></div>");
        if (invoice.paidAt() != null) {
            sb.append("<div class=\"detail-row\">Paid: <span>").append(formatDateTime(invoice.paidAt()))
                    .append("</span></div>");
        }
        sb.append("</div>");
        sb.append("</div>");
        sb.append("</div>");
        sb.append("</div>");
        sb.append("<div class=\"bottom-band\"></div>");
        sb.append("<div class=\"footer\">");
        sb.append("<div class=\"footer-left\">");
        sb.append("<div class=\"footer-thanks\">Thank you for choosing TMAG</div>");
        sb.append(
                "<div class=\"footer-sub\">Travel Medicine Advisory Global &#8212; Professional travel health solutions</div>");
        sb.append("</div>");
        sb.append("<div class=\"footer-right\">");
        sb.append("<div class=\"footer-contact-label\">Questions?</div>");
        sb.append("<div class=\"footer-contact-value\">support@tmag.com</div>");
        sb.append("<div class=\"footer-contact-value\">www.tmag.com</div>");
        sb.append("</div>");
        sb.append("</div>");
        sb.append("<div class=\"footer-divider\"></div>");
        sb.append("</div></body></html>");
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
