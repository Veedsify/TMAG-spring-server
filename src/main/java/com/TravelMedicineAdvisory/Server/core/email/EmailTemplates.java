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
              <!-- Google Fonts: matches the client app -->
              <link rel="preconnect" href="https://fonts.googleapis.com">
              <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
              <link href="https://fonts.googleapis.com/css2?family=Fraunces:ital,opsz,wght@0,9..144,400;0,9..144,600;0,9..144,700;0,9..144,800;1,9..144,400&family=Hanken+Grotesk:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
              <style>
                /* Fallback @import for clients that block <link> but allow <style> */
                @import url('https://fonts.googleapis.com/css2?family=Fraunces:ital,opsz,wght@0,9..144,400;0,9..144,600;0,9..144,700;0,9..144,800;1,9..144,400&family=Hanken+Grotesk:wght@300;400;500;600;700;800&display=swap');
              </style>
            </head>
            <body style="margin:0;padding:0;background-color:#f6f0e9;font-family:'Hanken Grotesk',Helvetica,Arial,sans-serif;-webkit-text-size-adjust:100%;-ms-text-size-adjust:100%;">
              <!-- Preheader text (hidden) -->
              <span style="display:none;max-height:0;overflow:hidden;font-size:1px;color:#f6f0e9;line-height:1px;">{preheader}&#8203;&nbsp;&#847;&nbsp;</span>
              <table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0" style="background-color:#f6f0e9;">
                <tr>
                  <td align="center" style="padding:48px 16px;">
                    <table role="presentation" width="600" cellspacing="0" cellpadding="0" border="0" style="max-width:600px;width:100%;">

                      <!-- ====== HEADER ====== -->
                      <tr>
                        <td style="background-color:#1a1008;border-radius:16px 16px 0 0;padding:0;text-align:center;">
                          <!-- Gradient accent bar: teal → deep teal → gold -->
                          <table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0">
                            <tr>
                              <td style="height:4px;background:linear-gradient(90deg,#2a7a6a 0%,#1a6a7a 55%,#c4953a 100%);border-radius:16px 16px 0 0;font-size:0;line-height:0;">&nbsp;</td>
                            </tr>
                            <tr>
                              <td style="padding:32px 40px 36px;text-align:center;">
                                <a href="{frontendUrl}" style="text-decoration:none;">
                                  <span style="display:block;font-family:'Hanken Grotesk',Helvetica,Arial,sans-serif;font-size:10px;font-weight:600;letter-spacing:5px;color:#8a7968;text-transform:uppercase;margin-bottom:10px;">Travel Medicine</span>
                                  <span style="display:block;font-family:'Fraunces',Georgia,serif;font-size:36px;font-weight:800;color:#f6f0e9;letter-spacing:4px;">TMAG</span>
                                  <span style="display:block;font-family:'Hanken Grotesk',Helvetica,Arial,sans-serif;font-size:10px;font-weight:500;color:#c4953a;letter-spacing:4px;text-transform:uppercase;margin-top:10px;">Global Advisory</span>
                                </a>
                              </td>
                            </tr>
                          </table>
                        </td>
                      </tr>

                      <!-- ====== CONTENT CARD ====== -->
                      <tr>
                        <td style="background-color:#ffffff;padding:52px 48px;border-left:1px solid #e8ddd3;border-right:1px solid #e8ddd3;">
                          {content}
                        </td>
                      </tr>

                      <!-- ====== FOOTER ====== -->
                      <tr>
                        <td style="background-color:#1a1008;border-radius:0 0 16px 16px;padding:32px 48px;text-align:center;">
                          <p style="margin:0 0 16px;font-family:'Hanken Grotesk',Helvetica,Arial,sans-serif;font-size:12px;font-weight:400;color:#8a7968;line-height:1.7;letter-spacing:0.3px;">
                            Trusted health guidance for every journey
                          </p>
                          <table role="presentation" cellspacing="0" cellpadding="0" border="0" align="center" style="margin:0 auto 20px;">
                            <tr>
                              <td style="padding:0 10px;">
                                <a href="{frontendUrl}/privacy" style="font-family:'Hanken Grotesk',Helvetica,Arial,sans-serif;font-size:12px;font-weight:500;color:#2a7a6a;text-decoration:none;">Privacy</a>
                              </td>
                              <td style="color:#4a3e32;font-size:12px;">&bull;</td>
                              <td style="padding:0 10px;">
                                <a href="{frontendUrl}/terms" style="font-family:'Hanken Grotesk',Helvetica,Arial,sans-serif;font-size:12px;font-weight:500;color:#2a7a6a;text-decoration:none;">Terms</a>
                              </td>
                              <td style="color:#4a3e32;font-size:12px;">&bull;</td>
                              <td style="padding:0 10px;">
                                <a href="{frontendUrl}/contact" style="font-family:'Hanken Grotesk',Helvetica,Arial,sans-serif;font-size:12px;font-weight:500;color:#2a7a6a;text-decoration:none;">Contact</a>
                              </td>
                            </tr>
                          </table>
                          <p style="margin:0;font-family:'Hanken Grotesk',Helvetica,Arial,sans-serif;font-size:11px;font-weight:300;color:#4a3e32;letter-spacing:0.2px;">&copy; {year} Travel Medicine Advisory Global. All rights reserved.</p>
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

    private static final String P_STYLE = "margin:0 0 16px;font-family:'Hanken Grotesk',Helvetica,Arial,sans-serif;font-size:16px;font-weight:400;color:#8a7968;line-height:1.75;";
    private static final String STRONG_STYLE = "color:#3d2c1e;font-weight:600;";

    /** Email verification — teal gradient CTA button */
    public String verificationEmail(String firstName, String verifyLink) {
        String content =
                badge("Account Activation") +
                heading("Verify your email address") +
                "<p style=\"" + P_STYLE + "\">Hi <strong style=\"" + STRONG_STYLE + "\">" + esc(firstName) + "</strong>,</p>" +
                "<p style=\"" + P_STYLE + "margin-bottom:36px;\">Welcome to TMAG! Please verify your email address to activate your account and start getting personalized travel health plans.</p>" +
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
                "<p style=\"" + P_STYLE + "\">Hi <strong style=\"" + STRONG_STYLE + "\">" + esc(firstName) + "</strong>,</p>" +
                "<p style=\"" + P_STYLE + "margin-bottom:36px;\">We received a request to reset your password. Click below to set a new password. If you didn't request this, you can safely ignore this email.</p>" +
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
                "<p style=\"" + P_STYLE + "\">Hi <strong style=\"" + STRONG_STYLE + "\">" + esc(firstName) + "</strong>,</p>" +
                "<p style=\"" + P_STYLE + "margin-bottom:36px;\">Your TMAG account password has been successfully changed. You can now log in with your new password.</p>" +
                infoBox("If you did not make this change, please reset your password immediately and contact our support team.") +
                divider() +
                fine("For security, this notification was sent to the email address associated with your account.");

        return wrap("Your TMAG account password has been changed.", content);
    }

    /** Employee invitation — teal CTA button with company branding */
    public String employeeInvitationEmail(String firstName, String companyName, String inviteLink) {
        String content =
                badge("You're Invited") +
                heading("Join " + esc(companyName) + " on TMAG") +
                "<p style=\"" + P_STYLE + "\">Hi <strong style=\"" + STRONG_STYLE + "\">" + esc(firstName) + "</strong>,</p>" +
                "<p style=\"" + P_STYLE + "\">Your company <strong style=\"" + STRONG_STYLE + "\">" + esc(companyName) + "</strong> has invited you to join TMAG — the AI-powered travel health advisory platform.</p>" +
                "<p style=\"" + P_STYLE + "margin-bottom:36px;\">Click below to set your password and get started with personalised travel health recommendations.</p>" +
                tealButton("Accept Invitation", inviteLink) +
                copyLink(inviteLink) +
                divider() +
                fine("This invitation expires in <strong>7 days</strong>. If you weren't expecting this, you can safely ignore this email.");

        return wrap("You've been invited to join " + companyName + " on TMAG.", content);
    }

    /** Generic email — for ad-hoc messages */
    public String genericEmail(String subject, String htmlContent) {
        String content =
                heading(esc(subject)) +
                "<div style=\"font-family:'Hanken Grotesk',Helvetica,Arial,sans-serif;font-size:16px;font-weight:400;color:#8a7968;line-height:1.75;\">" + htmlContent + "</div>" +
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
        return "<h2 style=\"margin:0 0 20px;font-family:'Fraunces',Georgia,serif;font-size:28px;font-weight:400;color:#3d2c1e;line-height:1.25;\">" + text + "</h2>";
    }

    private String badge(String label) {
        return "<p style=\"margin:0 0 10px;font-family:'Hanken Grotesk',Helvetica,Arial,sans-serif;font-size:10px;font-weight:700;letter-spacing:3px;color:#c4953a;text-transform:uppercase;\">" + label + "</p>";
    }

    private String tealButton(String label, String url) {
        return "<table role=\"presentation\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" align=\"center\" style=\"margin:0 auto 36px;\">" +
               "<tr><td style=\"border-radius:12px;background:linear-gradient(135deg,#2a7a6a 0%,#1a6a7a 50%,#246858 100%);\">" +
               "<a href=\"" + url + "\" style=\"display:inline-block;padding:15px 48px;" +
               "font-family:'Hanken Grotesk',Helvetica,Arial,sans-serif;" +
               "color:#ffffff;text-decoration:none;border-radius:12px;" +
               "font-weight:700;font-size:15px;letter-spacing:0.5px;\">" + esc(label) + "</a>" +
               "</td></tr></table>";
    }

    private String darkButton(String label, String url) {
        return "<table role=\"presentation\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" align=\"center\" style=\"margin:0 auto 36px;\">" +
               "<tr><td style=\"border-radius:12px;background-color:#2a1e14;\">" +
               "<a href=\"" + url + "\" style=\"display:inline-block;padding:15px 48px;" +
               "font-family:'Hanken Grotesk',Helvetica,Arial,sans-serif;" +
               "color:#f6f0e9;text-decoration:none;border-radius:12px;" +
               "font-weight:700;font-size:15px;letter-spacing:0.5px;\">" + esc(label) + "</a>" +
               "</td></tr></table>";
    }

    private String copyLink(String url) {
        return "<p style=\"margin:0 0 8px;font-family:'Hanken Grotesk',Helvetica,Arial,sans-serif;font-size:13px;font-weight:400;color:#8a7968;\">Or copy this link into your browser:</p>" +
               "<p style=\"margin:0 0 28px;word-break:break-all;font-family:'Hanken Grotesk',Helvetica,Arial,sans-serif;font-size:12px;color:#2a7a6a;" +
               "background:#fcf6ef;padding:14px 16px;border-radius:8px;border:1px solid #d4c4b4;line-height:1.6;\">" + url + "</p>";
    }

    private String infoBox(String message) {
        return "<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" style=\"margin:0 0 36px;\">" +
               "<tr><td style=\"background:#fcf6ef;border-left:4px solid #c4953a;padding:16px 20px;border-radius:0 8px 8px 0;\">" +
               "<p style=\"margin:0;font-family:'Hanken Grotesk',Helvetica,Arial,sans-serif;font-size:14px;font-weight:400;color:#3d2c1e;line-height:1.7;\">" + message + "</p>" +
               "</td></tr></table>";
    }

    private String divider() {
        return "<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" style=\"margin:12px 0 28px;\">" +
               "<tr><td style=\"height:1px;background-color:#e8ddd3;font-size:0;line-height:0;\">&nbsp;</td></tr>" +
               "</table>";
    }

    private String fine(String text) {
        return "<p style=\"margin:0;font-family:'Hanken Grotesk',Helvetica,Arial,sans-serif;font-size:13px;font-weight:400;color:#b0a090;line-height:1.7;\">" + text + "</p>";
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
