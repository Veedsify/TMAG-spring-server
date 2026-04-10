package com.TravelMedicineAdvisory.Server.domain.companyplan;

import java.io.ByteArrayOutputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

@Component
public class PlanPdfGenerator {

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    public byte[] generate(PlanResponse plan) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(buildHtml(plan), frontendUrl);
            builder.toStream(out);
            builder.run();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate company plan PDF", e);
        }
    }

    private String buildHtml(PlanResponse plan) {
        return """
                <!doctype html>
                <html>
                <head>
                  <meta charset="utf-8" />
                  <style>
                    body { font-family: Arial, sans-serif; color: #222; padding: 24px; }
                    .card { border: 1px solid #ddd; border-radius: 12px; padding: 20px; }
                    h1 { margin: 0 0 12px; }
                    .muted { color: #666; margin-bottom: 16px; }
                    table { width: 100%%; border-collapse: collapse; }
                    td { padding: 8px 0; border-bottom: 1px solid #eee; }
                    td:first-child { color: #666; width: 42%%; }
                  </style>
                </head>
                <body>
                  <div class="card">
                    <h1>%s Plan</h1>
                    <p class="muted">TMAG company plan details</p>
                    <table>
                      <tr><td>Plan code</td><td>%s</td></tr>
                      <tr><td>Signup credits</td><td>%d</td></tr>
                      <tr><td>Max employees</td><td>%d</td></tr>
                      <tr><td>Custom support</td><td>%s</td></tr>
                      <tr><td>API access</td><td>%s</td></tr>
                      <tr><td>Multiple admin accounts</td><td>%s</td></tr>
                      <tr><td>10,000+ employees access</td><td>%s</td></tr>
                    </table>
                  </div>
                </body>
                </html>
                """.formatted(
                escape(plan.displayName()),
                escape(plan.code()),
                plan.signupCredits() != null ? plan.signupCredits() : 0,
                plan.maxEmployees() != null ? plan.maxEmployees() : 0,
                yesNo(plan.customSupportEnabled()),
                yesNo(plan.apiAccessEnabled()),
                yesNo(plan.multipleAdminAccountsEnabled()),
                yesNo(plan.highEmployeeLimitEnabled()));
    }

    private String yesNo(Boolean value) {
        return Boolean.TRUE.equals(value) ? "Yes" : "No";
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
