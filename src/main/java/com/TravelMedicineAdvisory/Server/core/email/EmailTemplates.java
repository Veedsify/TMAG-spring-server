package com.TravelMedicineAdvisory.Server.core.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Year;

/**
 * TMAG branded email templates.
 *
 * Color palette matches the client app:
 *   Background   #f6f0e9  warm cream
 *   Dark         #2a1e14  dark brown (primary text/buttons)
 *   Darkest      #1a1008  deepest brown (header/footer bg)
 *   Teal         #2a7a6a  accent / primary CTA gradient
 *   Gold         #c4953a  accent highlights
 *   Heading      #3d2c1e  section headings
 *   Body text    #8a7968  paragraph copy
 *   Muted        #b0a090  fine print
 *   Border       #d4c4b4  dividers
 *   Card bg      #ffffff  content card
 *   Secondary bg #fcf6ef  code / link boxes
 */
@Component
public class EmailTemplates {

    // -------------------------------------------------------------------------
    // Wrapper template — uses {placeholder} replaced via String.replace()
    // so content can safely contain % characters.
    // -------------------------------------------------------------------------
    private static final String WRAPPER = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
              <meta charset="UTF-8">
              <meta name="viewport" content="width=device-width, initial-scale=1.0">
              <meta http-equiv="X-UA-Compatible" content="IE=edge">
              <title>TMAG</title>
            </head>
            <body style="margin:0;padding:0;background-color:#f6f0e9;font-family:'Segoe UI',Helvetica,Arial,sans-serif;-webkit-text-size-adjust:100%;-ms-text-size-adjust:100%;">
              <!-- Preheader text (hidden) -->
              <span style="display:none;max-height:0;overflow:hidden;font-size:1px;color:#f6f0e9;line-height:1px;">{preheader}&#8203;&nbsp;&#847;&nbsp;</span>
              <table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0" style="background-color:#f6f0e9;">
                <tr>
                  <td align="center" style="padding:40px 16px;">
                    <table role="presentation" width="600" cellspacing="0" cellpadding="0" border="0" style="max-width:600px;width:100%;">

                      <!-- ====== HEADER ====== -->
                      <tr>
                        <td style="background-color:#1a1008;border-radius:16px 16px 0 0;padding:32px 40px;text-align:center;">
                          <!-- Gradient accent bar: teal → deep teal → gold -->
                          <div style="height:3px;background:linear-gradient(90deg,#2a7a6a 0%,#1a6a7a 55%,#c4953a 100%);border-radius:2px;margin-bottom:24px;"></div>
                          <a href="{frontendUrl}" style="text-decoration:none;">
                            <span style="display:block;font-size:10px;font-weight:700;letter-spacing:4px;color:#8a7968;text-transform:uppercase;margin-bottom:6px;">Travel Medicine</span>
                            <span style="display:block;font-size:30px;font-weight:800;color:#f6f0e9;letter-spacing:3px;">TMAG</span>
                            <span style="display:block;font-size:10px;color:#c4953a;letter-spacing:3px;text-transform:uppercase;margin-top:6px;">Global Advisory</span>
                          </a>
                        </td>
                      </tr>

                      <!-- ====== CONTENT CARD ====== -->
                      <tr>
                        <td style="background-color:#ffffff;padding:48px 40px;border-left:1px solid #e8ddd3;border-right:1px solid #e8ddd3;">
                          {content}
                        </td>
                      </tr>

                      <!-- ====== FOOTER ====== -->
                      <tr>
                        <td style="background-color:#1a1008;border-radius:0 0 16px 16px;padding:28px 40px;text-align:center;">
                          <p style="margin:0 0 12px;font-size:12px;color:#8a7968;line-height:1.6;">
                            Trusted health guidance for every journey
                          </p>
                          <p style="margin:0 0 16px;font-size:12px;line-height:1.6;">
                            <a href="{frontendUrl}/privacy" style="color:#2a7a6a;text-decoration:none;margin:0 8px;">Privacy</a>
                            <span style="color:#4a3e32;">&bull;</span>
                            <a href="{frontendUrl}/terms" style="color:#2a7a6a;text-decoration:none;margin:0 8px;">Terms</a>
                            <span style="color:#4a3e32;">&bull;</span>
                            <a href="{frontendUrl}/contact" style="color:#2a7a6a;text-decoration:none;margin:0 8px;">Contact</a>
                          </p>
                          <p style="margin:0;font-size:11px;color:#4a3e32;">&copy; {year} Travel Medicine Advisory Global. All rights reserved.</p>
                        </td>
                      </tr>

                    </table>
                  </td>
                </tr>
              </table>
            </body>
            </html>
            """;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    // -------------------------------------------------------------------------
    // Public template methods
    // -------------------------------------------------------------------------

    /** Email verification — teal gradient CTA button */
    public String verificationEmail(String firstName, String verifyLink) {
        String content =
                badge("Account Activation") +
                heading("Verify your email address") +
                "<p style=\"margin:0 0 16px;font-size:16px;color:#8a7968;line-height:1.7;\">Hi <strong style=\"color:#3d2c1e;\">" + esc(firstName) + "</strong>,</p>" +
                "<p style=\"margin:0 0 32px;font-size:16px;color:#8a7968;line-height:1.7;\">Welcome to TMAG! Please verify your email address to activate your account and start getting personalized travel health plans.</p>" +
                tealButton("Verify Email Address", verifyLink) +
                copyLink(verifyLink) +
                divider() +
                fine("This link expires in <strong>24 hours</strong>. If you didn't create an account, you can safely ignore this email.");

        return wrap("Verify your email to activate your TMAG account.", content);
    }

    /** Password reset — dark button (security action) */
    public String passwordResetEmail(String firstName, String resetLink) {
        String content =
                badge("Security") +
                heading("Reset your password") +
                "<p style=\"margin:0 0 16px;font-size:16px;color:#8a7968;line-height:1.7;\">Hi <strong style=\"color:#3d2c1e;\">" + esc(firstName) + "</strong>,</p>" +
                "<p style=\"margin:0 0 32px;font-size:16px;color:#8a7968;line-height:1.7;\">We received a request to reset your password. Click below to set a new password. If you didn't request this, you can safely ignore this email.</p>" +
                darkButton("Reset Password", resetLink) +
                copyLink(resetLink) +
                divider() +
                fine("This link expires in <strong>15 minutes</strong>. If you didn't request a password reset, please contact support immediately.");

        return wrap("Password reset request for your TMAG account.", content);
    }

    /** Password changed confirmation — teal confirmation style */
    public String passwordChangedEmail(String firstName) {
        String content =
                badge("Security Notice") +
                heading("Password changed successfully") +
                "<p style=\"margin:0 0 16px;font-size:16px;color:#8a7968;line-height:1.7;\">Hi <strong style=\"color:#3d2c1e;\">" + esc(firstName) + "</strong>,</p>" +
                "<p style=\"margin:0 0 32px;font-size:16px;color:#8a7968;line-height:1.7;\">Your TMAG account password has been successfully changed. You can now log in with your new password.</p>" +
                infoBox("If you did not make this change, please reset your password immediately and contact our support team.") +
                divider() +
                fine("For security, this notification was sent to the email address associated with your account.");

        return wrap("Your TMAG account password has been changed.", content);
    }

    /** Generic email — for ad-hoc messages */
    public String genericEmail(String subject, String htmlContent) {
        String content =
                heading(esc(subject)) +
                "<div style=\"font-size:16px;color:#8a7968;line-height:1.7;\">" + htmlContent + "</div>" +
                divider() +
                fine("You received this email because you have an account with Travel Medicine Advisory Global.");

        return wrap(subject, content);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private String wrap(String preheader, String content) {
        return WRAPPER
                .replace("{preheader}", esc(preheader))
                .replace("{frontendUrl}", frontendUrl)
                .replace("{content}", content)
                .replace("{year}", String.valueOf(Year.now().getValue()));
    }

    private String heading(String text) {
        return "<h2 style=\"margin:0 0 24px;font-size:24px;font-weight:700;color:#3d2c1e;line-height:1.3;\">" + text + "</h2>";
    }

    private String badge(String label) {
        return "<p style=\"margin:0 0 12px;font-size:11px;font-weight:700;letter-spacing:2px;color:#c4953a;text-transform:uppercase;\">" + label + "</p>";
    }

    private String tealButton(String label, String url) {
        return "<div style=\"text-align:center;margin:0 0 32px;\">" +
               "<a href=\"" + url + "\" style=\"display:inline-block;padding:14px 44px;" +
               "background:linear-gradient(135deg,#2a7a6a 0%,#1a6a7a 50%,#246858 100%);" +
               "color:#ffffff;text-decoration:none;border-radius:12px;" +
               "font-weight:700;font-size:16px;letter-spacing:0.5px;" +
               "box-shadow:0 4px 12px rgba(42,122,106,0.3);\">" + esc(label) + "</a>" +
               "</div>";
    }

    private String darkButton(String label, String url) {
        return "<div style=\"text-align:center;margin:0 0 32px;\">" +
               "<a href=\"" + url + "\" style=\"display:inline-block;padding:14px 44px;" +
               "background-color:#2a1e14;" +
               "color:#f6f0e9;text-decoration:none;border-radius:12px;" +
               "font-weight:700;font-size:16px;letter-spacing:0.5px;" +
               "box-shadow:0 4px 12px rgba(42,30,20,0.25);\">" + esc(label) + "</a>" +
               "</div>";
    }

    private String copyLink(String url) {
        return "<p style=\"margin:0 0 8px;font-size:13px;color:#8a7968;\">Or copy this link into your browser:</p>" +
               "<p style=\"margin:0 0 24px;word-break:break-all;font-size:12px;color:#2a7a6a;" +
               "background:#fcf6ef;padding:12px 16px;border-radius:8px;border:1px solid #d4c4b4;\">" + url + "</p>";
    }

    private String infoBox(String message) {
        return "<div style=\"background:#fcf6ef;border-left:4px solid #c4953a;padding:16px 20px;" +
               "border-radius:0 8px 8px 0;margin:0 0 32px;\">" +
               "<p style=\"margin:0;font-size:14px;color:#3d2c1e;line-height:1.6;\">" + message + "</p>" +
               "</div>";
    }

    private String divider() {
        return "<div style=\"height:1px;background:#e8ddd3;margin:8px 0 24px;\"></div>";
    }

    private String fine(String text) {
        return "<p style=\"margin:0;font-size:13px;color:#b0a090;line-height:1.6;\">" + text + "</p>";
    }

    /** Minimal HTML escaping for user-provided values inserted into HTML attributes/text. */
    private String esc(String value) {
        if (value == null) return "";
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
