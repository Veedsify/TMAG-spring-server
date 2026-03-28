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

    private static final String ACCENT = "#2a7a6a";
    private static final String GOLD = "#c4953a";
    private static final String HEADING = "#3d2c1e";
    private static final String MUTED = "#7a6a5a";
    private static final String BODY = "#8a7968";
    private static final String BORDER_LIGHT = "#e8ddd3";
    private static final String BG_CREAM = "#f6f0e9";

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
        sb.append("@font-face { font-family: 'Hanken Grotesk'; src: url('classpath:/fonts/HankenGrotesk-Regular.ttf') format('truetype'); font-weight: 400; font-style: normal; }");
        sb.append("@font-face { font-family: 'Hanken Grotesk'; src: url('classpath:/fonts/HankenGrotesk-SemiBold.ttf') format('truetype'); font-weight: 600; font-style: normal; }");
        sb.append("@font-face { font-family: 'Hanken Grotesk'; src: url('classpath:/fonts/HankenGrotesk-Bold.ttf') format('truetype'); font-weight: 700; font-style: normal; }");
        sb.append("@font-face { font-family: 'Fraunces'; src: url('classpath:/fonts/Fraunces-Regular.ttf') format('truetype'); font-weight: 400; font-style: normal; }");
        sb.append("@font-face { font-family: 'Fraunces'; src: url('classpath:/fonts/Fraunces-Bold.ttf') format('truetype'); font-weight: 700; font-style: normal; }");
        sb.append("*{margin:0;padding:0;box-sizing:border-box}");
        sb.append("body{font-family:'Hanken Grotesk',Arial,sans-serif;font-size:10pt;color:").append(BODY).append(";background-color:").append(BG_CREAM).append(";padding:40px}");

        sb.append(".header{display:table;width:100%;margin-bottom:8px}");
        sb.append(".header>div{display:table-cell;vertical-align:bottom}");
        sb.append(".brand-left{width:60%}");
        sb.append(".brand-right{width:40%;text-align:right}");
        sb.append(".brand-label{font-size:8pt;color:").append(MUTED).append(";letter-spacing:2px;text-transform:uppercase}");
        sb.append(".brand-name{font-family:'Fraunces',serif;font-size:24pt;font-weight:700;color:").append(ACCENT).append("}");
        sb.append(".brand-tagline{font-size:8pt;color:").append(MUTED).append(";letter-spacing:2px;text-transform:uppercase}");
        sb.append(".invoice-title{font-family:'Fraunces',serif;font-size:32pt;font-weight:700;color:").append(HEADING).append("}");
        sb.append(".invoice-id{font-size:10pt;font-weight:600;color:").append(HEADING).append("}");
        sb.append(".invoice-id-label{font-size:8pt;color:").append(MUTED).append(";text-transform:uppercase;letter-spacing:1px}");
        sb.append(".accent-bar{height:3px;background-color:").append(ACCENT).append(";border-radius:2px;margin:20px 0}");

        sb.append(".meta{display:table;width:100%;margin-bottom:20px}");
        sb.append(".meta>div{display:table-cell;vertical-align:bottom}");
        sb.append(".meta-left{width:55%}");
        sb.append(".meta-right{width:45%;text-align:right}");
        sb.append(".meta-label{font-size:7pt;font-weight:700;color:").append(HEADING).append(";text-transform:uppercase;letter-spacing:1px;margin-bottom:2px}");
        sb.append(".meta-value{font-size:10pt;font-weight:600;color:").append(HEADING).append("}");
        sb.append(".meta-row{margin-bottom:4px}");
        sb.append(".bill-to-label{font-size:7pt;font-weight:700;color:").append(HEADING).append(";text-transform:uppercase;letter-spacing:1px;margin-bottom:4px}");
        sb.append(".bill-to-name{font-size:12pt;font-weight:700;color:").append(HEADING).append("}");
        sb.append(".bill-to-company-id{font-size:7pt;color:").append(MUTED).append(";margin-top:2px}");
        sb.append(".status-badge{display:inline-block;padding:2px 8px;border-radius:10px;font-size:7pt;font-weight:600;text-transform:uppercase}");
        sb.append(".status-paid{background-color:rgba(42,122,106,0.1);color:").append(ACCENT).append("}");
        sb.append(".status-pending{background-color:rgba(196,149,58,0.1);color:").append(GOLD).append("}");

        sb.append("table{width:100%;border-collapse:collapse;margin-bottom:20px}");
        sb.append("th{background-color:").append(BG_CREAM).append(";padding:10px 12px;text-align:left;font-size:7pt;font-weight:700;color:").append(HEADING).append(";text-transform:uppercase;letter-spacing:1px}");
        sb.append("th:first-child{border-radius:6px 0 0 6px}");
        sb.append("th:last-child{border-radius:0 6px 6px 0}");
        sb.append("th:not(:first-child){text-align:right}");
        sb.append("td{padding:12px;border-bottom:0.5px solid ").append(BORDER_LIGHT).append(";font-size:10pt}");
        sb.append("td:not(:first-child){text-align:right}");
        sb.append(".item-name{font-weight:600;color:").append(HEADING).append("}");
        sb.append(".item-desc{font-size:7pt;color:").append(MUTED).append(";margin-top:2px}");

        sb.append(".totals{width:40%;margin-left:auto}");
        sb.append(".totals-row{display:table;width:100%;padding:6px 0}");
        sb.append(".totals-label{display:table-cell;font-size:9pt;color:").append(BODY).append("}");
        sb.append(".totals-value{display:table-cell;text-align:right;font-size:9pt;color:").append(HEADING).append("}");
        sb.append(".totals-total{display:table;width:100%;padding:8px 0;border-top:2px solid ").append(HEADING).append("}");
        sb.append(".totals-total .totals-label{font-size:11pt;color:").append(HEADING).append("}");
        sb.append(".totals-total .totals-value{font-size:11pt;font-weight:700;color:").append(ACCENT).append("}");

        sb.append(".payment{display:table;width:100%;margin-top:30px}");
        sb.append(".payment>div{display:table-cell;width:50%}");
        sb.append(".payment-right{text-align:right}");
        sb.append(".section-label{font-size:7pt;font-weight:700;color:").append(HEADING).append(";text-transform:uppercase;letter-spacing:1px;margin-bottom:4px}");
        sb.append(".section-value{font-size:9pt;color:").append(BODY).append(";line-height:1.6}");

        sb.append(".footer-bar{height:3px;background-color:").append(ACCENT).append(";border-radius:0 0 6px 6px;margin-top:40px}");
        sb.append(".footer{display:table;width:100%;padding:20px 0}");
        sb.append(".footer>div{display:table-cell;vertical-align:bottom}");
        sb.append(".footer-right{text-align:right}");
        sb.append(".footer-thanks{font-family:'Fraunces',serif;font-size:14pt;font-weight:700;color:").append(ACCENT).append("}");
        sb.append(".footer-company{font-size:7pt;color:").append(MUTED).append(";margin-top:2px}");
        sb.append(".footer-tagline{font-size:7pt;color:").append(MUTED).append("}");
        sb.append(".footer-contact-label{font-size:7pt;color:").append(MUTED).append("}");
        sb.append(".footer-email{font-size:9pt;font-weight:600;color:").append(HEADING).append("}");
        sb.append(".footer-website{font-size:7pt;color:").append(MUTED).append("}");

        sb.append("</style></head><body>");

        sb.append("<div class=\"header\">");
        sb.append("<div class=\"brand-left\">");
        sb.append("<div class=\"brand-label\">Travel Medicine</div>");
        sb.append("<div class=\"brand-name\">TMAG</div>");
        sb.append("<div class=\"brand-tagline\">Global Advisory</div>");
        sb.append("</div>");
        sb.append("<div class=\"brand-right\">");
        sb.append("<div class=\"invoice-title\">INVOICE</div>");
        sb.append("</div>");
        sb.append("</div>");

        sb.append("<div class=\"header\">");
        sb.append("<div class=\"brand-left\">");
        sb.append("<div class=\"invoice-id-label\">www.tmag.com</div>");
        sb.append("</div>");
        sb.append("<div class=\"brand-right\">");
        sb.append("<div class=\"invoice-id-label\">Invoice</div>");
        sb.append("<div class=\"invoice-id\">INV-").append(String.format("%06d", invoice.id())).append("</div>");
        sb.append("</div>");
        sb.append("</div>");

        sb.append("<div class=\"accent-bar\"></div>");

        sb.append("<div class=\"meta\">");
        sb.append("<div class=\"meta-left\">");
        sb.append("<div class=\"bill-to-label\">Bill To</div>");
        sb.append("<div class=\"bill-to-name\">").append(escapeHtml(companyName != null ? companyName : "Customer")).append("</div>");
        if (invoice.companyId() != null) {
            sb.append("<div class=\"bill-to-company-id\">Company #").append(invoice.companyId()).append("</div>");
        }
        sb.append("</div>");

        sb.append("<div class=\"meta-right\">");
        sb.append("<div class=\"meta-row\">");
        sb.append("<div class=\"meta-label\">Invoice Date</div>");
        sb.append("<div class=\"meta-value\">").append(formatDate(invoice.issuedAt())).append("</div>");
        sb.append("</div>");
        if (invoice.dueDate() != null) {
            sb.append("<div class=\"meta-row\">");
            sb.append("<div class=\"meta-label\">Due Date</div>");
            sb.append("<div class=\"meta-value\">").append(formatDate(invoice.dueDate())).append("</div>");
            sb.append("</div>");
        }
        sb.append("<div class=\"meta-row\">");
        sb.append("<div class=\"meta-label\">Status</div>");
        String statusClass = invoice.status() != null && invoice.status().equalsIgnoreCase("paid") ? "status-paid" : "status-pending";
        sb.append("<div class=\"meta-value\"><span class=\"status-badge ").append(statusClass).append("\">").append(capitalize(invoice.status())).append("</span></div>");
        sb.append("</div>");
        sb.append("</div></div>");

        String price = formatCurrency(invoice.amount(), invoice.currency());
        sb.append("<table><thead><tr><th>Description</th><th>Qty</th><th>Unit Price</th><th>Amount</th></tr></thead><tbody>");
        sb.append("<tr><td><div class=\"item-name\">").append(escapeHtml(invoice.description() != null ? invoice.description() : "Credit Purchase")).append("</div>");
        sb.append("<div class=\"item-desc\">TMAG Platform Credits</div></td>");
        sb.append("<td>1</td>");
        sb.append("<td>").append(price).append("</td>");
        sb.append("<td>").append(price).append("</td>");
        sb.append("</tr></tbody></table>");

        sb.append("<div class=\"totals\">");
        sb.append("<div class=\"totals-row\"><div class=\"totals-label\">Subtotal</div><div class=\"totals-value\">").append(price).append("</div></div>");
        sb.append("<div class=\"totals-row\"><div class=\"totals-label\">Tax (0%)</div><div class=\"totals-value\">").append(formatCurrency(BigDecimal.ZERO, invoice.currency())).append("</div></div>");
        sb.append("<div class=\"totals-total\"><div class=\"totals-label\">TOTAL</div><div class=\"totals-value\">").append(price).append("</div></div>");
        sb.append("</div>");

        sb.append("<div class=\"payment\">");
        sb.append("<div class=\"payment-left\">");
        sb.append("<div class=\"section-label\">Payment</div>");
        sb.append("<div class=\"section-value\">Method: ").append(invoice.paymentMethod() != null ? invoice.paymentMethod() : "Online Payment").append("</div>");
        sb.append("<div class=\"section-value\">Currency: ").append(invoice.currency() != null ? invoice.currency() : "USD").append("</div>");
        sb.append("</div>");
        sb.append("<div class=\"payment-right\">");
        sb.append("<div class=\"section-label\">Transaction</div>");
        sb.append("<div class=\"section-value\">Invoice #").append(invoice.id()).append("</div>");
        sb.append("<div class=\"section-value\">Created: ").append(formatDateTime(invoice.createdAt())).append("</div>");
        if (invoice.paidAt() != null) {
            sb.append("<div class=\"section-value\">Paid: ").append(formatDateTime(invoice.paidAt())).append("</div>");
        }
        sb.append("</div></div>");

        sb.append("<div class=\"footer-bar\"></div>");
        sb.append("<div class=\"footer\">");
        sb.append("<div class=\"footer-left\">");
        sb.append("<div class=\"footer-thanks\">Thank you for your business</div>");
        sb.append("<div class=\"footer-company\">Travel Medicine Advisory Global</div>");
        sb.append("<div class=\"footer-tagline\">Professional travel health solutions</div>");
        sb.append("</div>");
        sb.append("<div class=\"footer-right\">");
        sb.append("<div class=\"footer-contact-label\">Questions? Contact</div>");
        sb.append("<div class=\"footer-email\">support@tmag.com</div>");
        sb.append("<div class=\"footer-website\">www.tmag.com</div>");
        sb.append("</div></div>");

        sb.append("</body></html>");
        return sb.toString();
    }

    private String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) return "—";
        return dateTime.format(DateTimeFormatter.ofPattern("MMM d, yyyy"));
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "—";
        return dateTime.format(DateTimeFormatter.ofPattern("MMM d, yyyy • h:mm a"));
    }

    private String formatCurrency(BigDecimal amount, String currency) {
        if (amount == null) amount = BigDecimal.ZERO;
        String symbol = switch (currency) {
            case "NGN" -> "₦";
            case "EUR" -> "€";
            case "GBP" -> "£";
            default -> "$";
        };
        return symbol + String.format("%,.2f", amount);
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return "—";
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    private String escapeHtml(String str) {
        if (str == null) return "";
        return str.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;");
    }
}
