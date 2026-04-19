package com.TravelMedicineAdvisory.Server.core.email;

import java.time.Year;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
                              <td style="padding:32px 40px 36px;text-align:center;">
                                <a href="{frontendUrl}" style="text-decoration:none;">
                                  <span style="display:block;font-family:'Hanken Grotesk',Helvetica,Arial,sans-serif;font-size:10px;font-weight:600;letter-spacing:5px;color:#8a7968;text-transform:uppercase;margin-bottom:10px;">Travel Medicine</span>
                                  <span style="display:block;font-family:'Fraunces',Georgia,serif;font-size:36px;font-weight:400;color:#f6f0e9;letter-spacing:4px;">TMAG</span>
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

    /** Email verification — 6-digit code */
    public String verificationEmail(String firstName, String code) {
        String content =
                badge("Account Activation") +
                heading("Verify your email address") +
                "<p style=\"" + P_STYLE + "\">Hi <strong style=\"" + STRONG_STYLE + "\">" + esc(firstName) + "</strong>,</p>" +
                "<p style=\"" + P_STYLE + "margin-bottom:36px;\">Welcome to TMAG! Enter the code below to verify your email and activate your account.</p>" +
                verificationCode(code) +
                divider() +
                fine("This code expires in <strong>15 minutes</strong>. If you didn't create an account, you can safely ignore this email.");

        return wrap("Your TMAG verification code is " + code, content);
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
    public String employeeInvitationEmail(String firstName, String companyName, String role, String inviteLink) {
        String content =
                badge("You're Invited") +
                heading("Join " + esc(companyName) + " on TMAG") +
                "<p style=\"" + P_STYLE + "\">Hi <strong style=\"" + STRONG_STYLE + "\">" + esc(firstName) + "</strong>,</p>" +
                "<p style=\"" + P_STYLE + "\">Your company <strong style=\"" + STRONG_STYLE + "\">" + esc(companyName) + "</strong> has invited you to join TMAG — the AI-powered travel health advisory platform.</p>" +
                roleBadge(role) +
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

    /** Credit purchase confirmation */
    public String creditPurchaseConfirmationEmail(String firstName, Integer credits, String currencySymbol, String amount) {
        String content =
                badge("Payment Confirmed") +
                heading("Credit purchase successful") +
                "<p style=\"" + P_STYLE + "\">Hi <strong style=\"" + STRONG_STYLE + "\">" + esc(firstName) + "</strong>,</p>" +
                "<p style=\"" + P_STYLE + "\">Your credit purchase has been completed successfully. Your account has been credited.</p>" +
                infoBox("Credits purchased: <strong>" + credits + "</strong><br/>Amount paid: " + currencySymbol + " " + amount) +
                divider() +
                fine("Thank you for your purchase. You can now use these credits to generate travel health plans.");

        return wrap("Your TMAG credit purchase is complete", content);
    }

    /** Credit allocation notification */
    public String creditAllocationNotificationEmail(String firstName, Integer credits, String companyName) {
        String content =
                badge("Credits Allocated") +
                heading("You've received credits") +
                "<p style=\"" + P_STYLE + "\">Hi <strong style=\"" + STRONG_STYLE + "\">" + esc(firstName) + "</strong>,</p>" +
                "<p style=\"" + P_STYLE + "\">" + esc(companyName) + " has allocated credits to your account.</p>" +
                infoBox("Credits added: <strong>" + credits + "</strong>") +
                divider() +
                fine("Log in to your account to use these credits for travel health plans.");

        return wrap("Credits allocated to your TMAG account", content);
    }

    /** Credit request approved */
    public String creditRequestApprovedEmail(String firstName, Integer credits, String companyName) {
        String content =
                badge("Request Approved") +
                heading("Credit request approved") +
                "<p style=\"" + P_STYLE + "\">Hi <strong style=\"" + STRONG_STYLE + "\">" + esc(firstName) + "</strong>,</p>" +
                "<p style=\"" + P_STYLE + "\">Great news! " + esc(companyName) + " has approved your credit request.</p>" +
                infoBox("Credits approved: <strong>" + credits + "</strong>") +
                divider() +
                fine("Your credits are now available. Start generating your travel health plans!");

        return wrap("Your credit request has been approved", content);
    }

    /** Credit request rejected */
    public String creditRequestRejectedEmail(String firstName, Integer credits, String reason, String companyName) {
        String content =
                badge("Request Update") +
                heading("Credit request update") +
                "<p style=\"" + P_STYLE + "\">Hi <strong style=\"" + STRONG_STYLE + "\">" + esc(firstName) + "</strong>,</p>" +
                "<p style=\"" + P_STYLE + "\">Your credit request for <strong>" + credits + "</strong> credits with " + esc(companyName) + " has been reviewed.</p>" +
                infoBox("Status: <strong>Declined</strong><br/>Reason: " + esc(reason)) +
                divider() +
                fine("Please contact your HR or administrator for more information.");

        return wrap("Update on your credit request", content);
    }

    /** Employee status changed */
    public String employeeStatusChangedEmail(String firstName, String status, String companyName) {
        String content =
                badge("Account Status") +
                heading("Your account status has changed") +
                "<p style=\"" + P_STYLE + "\">Hi <strong style=\"" + STRONG_STYLE + "\">" + esc(firstName) + "</strong>,</p>" +
                "<p style=\"" + P_STYLE + "\">Your account status with <strong>" + esc(companyName) + "</strong> has been updated.</p>" +
                infoBox("New status: <strong>" + esc(status) + "</strong>") +
                divider() +
                fine("If you have questions, please contact your administrator.");

        return wrap("Your TMAG account status has been updated", content);
    }

    /** Employee removed */
    public String employeeRemovedEmail(String firstName, String companyName) {
        String content =
                badge("Account Update") +
                heading("You have been removed from " + esc(companyName)) +
                "<p style=\"" + P_STYLE + "\">Hi <strong style=\"" + STRONG_STYLE + "\">" + esc(firstName) + "</strong>,</p>" +
                "<p style=\"" + P_STYLE + "\">Your access to <strong>" + esc(companyName) + "</strong> on TMAG has been removed.</p>" +
                infoBox("Your account has been deactivated for this organization.") +
                divider() +
                fine("If you believe this is an error, please contact your HR or administrator.");

        return wrap("Removed from " + companyName + " on TMAG", content);
    }

    /** API key created */
    public String apiKeyCreatedEmail(String firstName, String keyName, String companyName) {
        String content =
                badge("API Access") +
                heading("New API key created") +
                "<p style=\"" + P_STYLE + "\">Hi <strong style=\"" + STRONG_STYLE + "\">" + esc(firstName) + "</strong>,</p>" +
                "<p style=\"" + P_STYLE + "\">A new API key has been created for <strong>" + esc(companyName) + "</strong>.</p>" +
                infoBox("Key name: <strong>" + esc(keyName) + "</strong>") +
                divider() +
                fine("Store this key securely. You won't be able to view it again. If you didn't create this key, revoke it immediately.");

        return wrap("New API key created for " + companyName, content);
    }

    /** API key revoked */
    public String apiKeyRevokedEmail(String firstName, String keyName, String companyName) {
        String content =
                badge("API Access") +
                heading("API key revoked") +
                "<p style=\"" + P_STYLE + "\">Hi <strong style=\"" + STRONG_STYLE + "\">" + esc(firstName) + "</strong>,</p>" +
                "<p style=\"" + P_STYLE + "\">An API key for <strong>" + esc(companyName) + "</strong> has been revoked.</p>" +
                infoBox("Revoked key: <strong>" + esc(keyName) + "</strong>") +
                divider() +
                fine("If you didn't revoke this key, please contact your administrator immediately.");

        return wrap("API key revoked for " + companyName, content);
    }

    /** Travel plan created */
    public String travelPlanCreatedEmail(String firstName, String destination, String companyName) {
        String content =
                badge("Travel Plan Ready") +
                heading("Your travel health plan is ready") +
                "<p style=\"" + P_STYLE + "\">Hi <strong style=\"" + STRONG_STYLE + "\">" + esc(firstName) + "</strong>,</p>" +
                "<p style=\"" + P_STYLE + "\">Your personalized travel health plan for <strong>" + esc(destination) + "</strong> has been generated.</p>" +
                infoBox("Destination: <strong>" + esc(destination) + "</strong><br/>Generated by: " + esc(companyName)) +
                tealButton("View Your Plan", "{frontendUrl}/plans") +
                divider() +
                fine("Log in to view complete health advisories, vaccinations, and safety recommendations.");

        return wrap("Your travel health plan for " + destination + " is ready", content);
    }

    /** Login alert */
    public String loginAlertEmail(String firstName, String location, String device, String timestamp) {
        String content =
                badge("Security Alert") +
                heading("New login detected") +
                "<p style=\"" + P_STYLE + "\">Hi <strong style=\"" + STRONG_STYLE + "\">" + esc(firstName) + "</strong>,</p>" +
                "<p style=\"" + P_STYLE + "\">We detected a new sign-in to your TMAG account.</p>" +
                infoBox("Device: <strong>" + esc(device) + "</strong><br/>Location: <strong>" + esc(location) + "</strong><br/>Time: <strong>" + esc(timestamp) + "</strong>") +
                divider() +
                fine("If this wasn't you, please reset your password immediately and contact support.");

        return wrap("New login to your TMAG account", content);
    }

    /** Invoice available */
    public String invoiceAvailableEmail(String firstName, String invoiceNumber, String amount, String currencySymbol, String companyName) {
        String content =
                badge("Invoice") +
                heading("New invoice available") +
                "<p style=\"" + P_STYLE + "\">Hi <strong style=\"" + STRONG_STYLE + "\">" + esc(firstName) + "</strong>,</p>" +
                "<p style=\"" + P_STYLE + "\">A new invoice from <strong>" + esc(companyName) + "</strong> is now available.</p>" +
                infoBox("Invoice #: <strong>" + esc(invoiceNumber) + "</strong><br/>Amount: " + currencySymbol + " " + esc(amount)) +
                tealButton("View Invoice", "{frontendUrl}/credits/invoices") +
                divider() +
                fine("You can view and download your invoices from the billing section.");

        return wrap("New invoice #" + invoiceNumber + " from TMAG", content);
    }

    /** Onboarding reminder */
    public String onboardingReminderEmail(String firstName, String companyName) {
        String content =
                badge("Action Required") +
                heading("Complete your TMAG onboarding") +
                "<p style=\"" + P_STYLE + "\">Hi <strong style=\"" + STRONG_STYLE + "\">" + esc(firstName) + "</strong>,</p>" +
                "<p style=\"" + P_STYLE + "\">" + esc(companyName) + " has invited you to TMAG, but you haven't completed your onboarding yet.</p>" +
                "<p style=\"" + P_STYLE + "\">Complete your profile to start receiving personalized travel health recommendations.</p>" +
                tealButton("Complete Onboarding", "{frontendUrl}/onboarding") +
                divider() +
                fine("If you already completed onboarding, you can safely ignore this email.");

        return wrap("Reminder: Complete your TMAG onboarding", content);
    }

    /** Two-Factor Authentication enabled */
    public String twoFactorEnabledEmail(String firstName) {
        String content =
                badge("Security") +
                heading("Two-factor authentication enabled") +
                "<p style=\"" + P_STYLE + "\">Hi <strong style=\"" + STRONG_STYLE + "\">" + esc(firstName) + "</strong>,</p>" +
                "<p style=\"" + P_STYLE + "\">Two-factor authentication has been enabled on your TMAG account.</p>" +
                infoBox("Your account is now more secure. You will be prompted for a verification code when logging in.") +
                divider() +
                fine("If you didn't enable 2FA, please contact support immediately.");

        return wrap("Two-factor authentication enabled on your TMAG account", content);
    }

    /** Two-Factor Authentication disabled */
    public String twoFactorDisabledEmail(String firstName) {
        String content =
                badge("Security") +
                heading("Two-factor authentication disabled") +
                "<p style=\"" + P_STYLE + "\">Hi <strong style=\"" + STRONG_STYLE + "\">" + esc(firstName) + "</strong>,</p>" +
                "<p style=\"" + P_STYLE + "\">Two-factor authentication has been disabled on your TMAG account.</p>" +
                infoBox("Your account is now less secure. We recommend keeping 2FA enabled.") +
                divider() +
                fine("If you didn't disable 2FA, please re-enable it immediately and contact support.");

        return wrap("Two-factor authentication disabled on your TMAG account", content);
    }

    /** Billing currency changed */
    public String billingCurrencyChangedEmail(String firstName, String oldCurrency, String newCurrency, String companyName) {
        String content =
                badge("Billing Update") +
                heading("Billing currency updated") +
                "<p style=\"" + P_STYLE + "\">Hi <strong style=\"" + STRONG_STYLE + "\">" + esc(firstName) + "</strong>,</p>" +
                "<p style=\"" + P_STYLE + "\">The billing currency for <strong>" + esc(companyName) + "</strong> has been updated.</p>" +
                infoBox("Previous currency: <strong>" + esc(oldCurrency) + "</strong><br/>New currency: <strong>" + esc(newCurrency) + "</strong>") +
                divider() +
                fine("All future invoices will be generated in " + esc(newCurrency) + ".");

        return wrap("Billing currency updated for " + companyName, content);
    }

    /** Data export confirmation */
    public String dataExportEmail(String firstName, String exportType, String companyName) {
        String content =
                badge("Data Export") +
                heading("Your data export is ready") +
                "<p style=\"" + P_STYLE + "\">Hi <strong style=\"" + STRONG_STYLE + "\">" + esc(firstName) + "</strong>,</p>" +
                "<p style=\"" + P_STYLE + "\">A data export request for <strong>" + esc(companyName) + "</strong> has been processed.</p>" +
                infoBox("Data type exported: <strong>" + esc(exportType) + "</strong>") +
                divider() +
                fine("You can download your exported data from the admin dashboard. This export complies with GDPR requirements.");

        return wrap("Your data export is ready for " + companyName, content);
    }

    /** Invitation accepted notification to admin */
    public String invitationAcceptedEmail(String adminName, String employeeName, String companyName) {
        String content =
                badge("Team Update") +
                heading(employeeName + " has accepted the invitation") +
                "<p style=\"" + P_STYLE + "\">Hi <strong style=\"" + STRONG_STYLE + "\">" + esc(adminName) + "</strong>,</p>" +
                "<p style=\"" + P_STYLE + "\"><strong>" + esc(employeeName) + "</strong> has accepted the invitation to join <strong>" + esc(companyName) + "</strong> on TMAG.</p>" +
                infoBox("The employee has completed their registration and can now access the platform.") +
                tealButton("View Team", "{frontendUrl}/team/members") +
                divider() +
                fine("Log in to the admin dashboard to manage your team.");

        return wrap(employeeName + " has joined " + companyName + " on TMAG", content);
    }

    /** New credit request submitted - notify HR admin */
    public String creditRequestSubmittedEmail(String adminName, String employeeName, Integer credits, String companyName) {
        String content =
                badge("New Credit Request") +
                heading("New credit request pending approval") +
                "<p style=\"" + P_STYLE + "\">Hi <strong style=\"" + STRONG_STYLE + "\">" + esc(adminName) + "</strong>,</p>" +
                "<p style=\"" + P_STYLE + "\"><strong>" + esc(employeeName) + "</strong> has submitted a credit request that requires your approval.</p>" +
                infoBox("Credits requested: <strong>" + credits + "</strong>") +
                tealButton("Review Request", "{frontendUrl}/hr/credit-requests") +
                divider() +
                fine("Log in to the HR dashboard to approve or reject this request.");

        return wrap("New credit request from " + employeeName, content);
    }

    /**
     * Company admin onboarding welcome email
     */
    public String companyAdminOnboardingEmail(String firstName, String companyName, String temporaryPassword) {
        String content
                = badge("Welcome to TMAG")
                + heading("Your TMAG admin account is ready")
                + "<p style=\"" + P_STYLE + "\">Hi <strong style=\"" + STRONG_STYLE + "\">" + esc(firstName) + "</strong>,</p>"
                + "<p style=\"" + P_STYLE + "\">Welcome to <strong>Travel Medicine Advisory Global</strong>! Your administrator account for <strong>" + esc(companyName) + "</strong> has been created.</p>"
                + "<p style=\"" + P_STYLE + "\">Below is your temporary login credential:</p>"
                + infoBox("Temporary Password: <strong>" + esc(temporaryPassword) + "</strong><br/><em>You will be required to change this password on first login.</em>")
                + heading("Getting Started")
                + "<p style=\"" + P_STYLE + "\">Here's a quick guide to help you navigate the admin dashboard:</p>"
                + "<div style=\"font-family:'Hanken Grotesk',Helvetica,Arial,sans-serif;font-size:14px;color:#8a7968;line-height:1.8;padding-left:20px;\">"
                + "<p><strong style=\"color:#1c3a2d;\">1. Team Management</strong><br/>"
                + "Navigate to <strong>Team & Members</strong> to invite employees, allocate credits, and manage your organization's users. Use CSV bulk upload for quick onboarding.</p>"
                + "<p><strong style=\"color:#1c3a2d;\">2. Credit Management</strong><br/>"
                + "Purchase credits under <strong>Credits</strong> and allocate them to employees. Monitor usage and track credit distribution.</p>"
                + "<p><strong style=\"color:#1c3a2d;\">3. Credit Requests</strong><br/>"
                + "Review and approve/reject credit requests from employees in the <strong>Requests</strong> section.</p>"
                + "<p><strong style=\"color:#1c3a2d;\">4. Travel Plans</strong><br/>"
                + "Generate travel health plans for your employees. Each plan includes vaccinations, health alerts, and safety advisories.</p>"
                + "<p><strong style=\"color:#1c3a2d;\">5. Reports & Analytics</strong><br/>"
                + "Export usage reports, team reports, and compliance data from the <strong>Reports</strong> section.</p>"
                + "<p><strong style=\"color:#1c3a2d;\">6. API Keys</strong><br/>"
                + "Generate API keys under <strong>Settings</strong> to integrate TMAG with your existing systems.</p>"
                + "<p><strong style=\"color:#1c3a2d;\">7. Settings</strong><br/>"
                + "Configure billing currency, notification preferences, and security settings including 2FA.</p>"
                + "</div>"
                + tealButton("Go to Admin Dashboard", "{frontendUrl}/admin")
                + divider()
                + fine("If you have questions, contact our support team. We're here to help you keep your travelers safe!");

        return wrap("Welcome to TMAG - Your admin account is ready", content);
    }

    public String contactAcknowledgmentEmail(String firstName, String subject) {
        String content = badge("Message Received")
                + heading("We've got your message")
                + "<p style=\"margin:0 0 24px;font-family:'Hanken Grotesk',Helvetica,Arial,sans-serif;font-size:15px;font-weight:400;color:#5a4a3a;line-height:1.7;\">Hi " + esc(firstName) + ",</p>"
                + "<p style=\"margin:0 0 24px;font-family:'Hanken Grotesk',Helvetica,Arial,sans-serif;font-size:15px;font-weight:400;color:#5a4a3a;line-height:1.7;\">Thank you for reaching out to TMAG. We've received your message regarding <strong style=\"color:#3d2c1e;\">&ldquo;" + esc(subject) + "&rdquo;</strong> and our team will get back to you within 24&ndash;48 hours.</p>"
                + infoBox("We take every inquiry seriously. A member of our team will review your message and respond as soon as possible.")
                + tealButton("Visit TMAG", "{frontendUrl}/contact")
                + divider()
                + fine("If you didn't send this message, you can safely ignore this email.");

        return wrap("We received your message — TMAG", content);
    }

    public String contactSubmissionEmail(String name, String email, String inquiryType, String subject, String message) {
        String typeLabel = switch (inquiryType) {
            case "SUPPORT" -> "Support Request";
            case "DEMO"    -> "Demo Request";
            case "SALES"   -> "Sales Inquiry";
            default        -> "General Inquiry";
        };

        String content = badge("New Inquiry")
                + heading("New contact submission")
                + "<p style=\"margin:0 0 24px;font-family:'Hanken Grotesk',Helvetica,Arial,sans-serif;font-size:15px;font-weight:400;color:#5a4a3a;line-height:1.7;\">A new contact form submission has been received. Details below:</p>"
                + infoBox("<strong style=\"color:#3d2c1e;\">From:</strong> " + esc(name) + " &lt;" + esc(email) + "&gt;<br/>"
                        + "<strong style=\"color:#3d2c1e;\">Type:</strong> " + esc(typeLabel) + "<br/>"
                        + "<strong style=\"color:#3d2c1e;\">Subject:</strong> " + esc(subject) + "<br/><br/>"
                        + "<strong style=\"color:#3d2c1e;\">Message:</strong><br/>" + esc(message).replace("\n", "<br/>"))
                + darkButton("Open Admin Dashboard", "{frontendUrl}/admin")
                + divider()
                + fine("This is an internal notification sent to the TMAG admin team.");

        return wrap("New contact submission: " + subject, content);
    }

    public String newsletterWelcomeEmail(String firstName) {
        String content = badge("Newsletter")
                + heading("You're on the list!")
                + "<p style=\"margin:0 0 24px;font-family:'Hanken Grotesk',Helvetica,Arial,sans-serif;font-size:15px;font-weight:400;color:#5a4a3a;line-height:1.7;\">Hi " + esc(firstName) + ",</p>"
                + "<p style=\"margin:0 0 24px;font-family:'Hanken Grotesk',Helvetica,Arial,sans-serif;font-size:15px;font-weight:400;color:#5a4a3a;line-height:1.7;\">Welcome to TMAG updates! You'll be the first to hear about new features, travel health insights, and tips to keep your journeys safe and healthy.</p>"
                + infoBox("Expect updates on destination health alerts, vaccination guidance, and platform improvements — straight to your inbox.")
                + tealButton("Explore TMAG", "{frontendUrl}/how-it-works")
                + divider()
                + fine("You're receiving this because you subscribed to TMAG updates. To unsubscribe, reply to this email.");

        return wrap("Welcome to TMAG updates", content);
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

    private String verificationCode(String code) {
        StringBuilder digits = new StringBuilder();
        for (char c : code.toCharArray()) {
            digits.append("<td style=\"width:48px;height:56px;background:#fcf6ef;border:2px solid #d4c4b4;border-radius:10px;text-align:center;vertical-align:middle;")
                  .append("font-family:'Fraunces',Georgia,serif;font-size:28px;font-weight:700;color:#3d2c1e;letter-spacing:0;\">")
                  .append(c)
                  .append("</td><td style=\"width:8px;\"></td>");
        }
        return "<table role=\"presentation\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" align=\"center\" style=\"margin:0 auto 36px;\">" +
               "<tr>" + digits + "</tr></table>";
    }

    private String copyLink(String url) {
        return "<p style=\"margin:0 0 8px;font-family:'Hanken Grotesk',Helvetica,Arial,sans-serif;font-size:13px;font-weight:400;color:#8a7968;\">Or copy this link into your browser:</p>" +
               "<p style=\"margin:0 0 28px;word-break:break-all;font-family:'Hanken Grotesk',Helvetica,Arial,sans-serif;font-size:12px;color:#2a7a6a;" +
               "background:#fcf6ef;padding:14px 16px;border-radius:8px;border:1px solid #d4c4b4;line-height:1.6;\">" + url + "</p>";
    }

    private String roleBadge(String role) {
        return "<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" style=\"margin:0 0 28px;\">" +
               "<tr><td style=\"background:#f0faf7;border:1px solid #b8e0d6;border-radius:10px;padding:14px 20px;text-align:center;\">" +
               "<p style=\"margin:0 0 2px;font-family:'Hanken Grotesk',Helvetica,Arial,sans-serif;font-size:11px;font-weight:700;letter-spacing:2px;color:#8a7968;text-transform:uppercase;\">Your Role</p>" +
               "<p style=\"margin:0;font-family:'Fraunces',Georgia,serif;font-size:20px;font-weight:600;color:#2a7a6a;\">" + esc(role) + "</p>" +
               "</td></tr></table>";
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

    // ─── Ebook delivery email ─────────────────────────────────────────────────

    public String ebookDeliveryEmail(String buyerName, String ebookTitle, String versionLabel,
                                      String downloadUrl, String currencySymbol, String amount, String txRef) {
        String content =
                badge("Download Ready") +
                heading("Your ebook is ready") +
                "<p style=\"" + P_STYLE + "\">Hi <strong style=\"" + STRONG_STYLE + "\">" + esc(buyerName) + "</strong>,</p>" +
                "<p style=\"" + P_STYLE + "\">Thank you for your purchase! Your copy of <strong style=\"" + STRONG_STYLE + "\">" + esc(ebookTitle) + "</strong> (" + esc(versionLabel) + " edition) is ready to download.</p>" +
                infoBox("Ebook: <strong>" + esc(ebookTitle) + "</strong><br/>Edition: " + esc(versionLabel) + "<br/>Amount Paid: " + esc(currencySymbol) + " " + esc(amount) + "<br/>Order Ref: " + esc(txRef)) +
                tealButton("Download Your Ebook", downloadUrl) +
                divider() +
                fine("Keep this email safe &mdash; it contains your personal download link. If you have any issues, reply to this email and we&rsquo;ll assist you promptly.");
        return wrap("Your TMAG Ebook is Ready", content);
    }

    public String ebookOrderConfirmationEmail(String buyerName, String ebookTitle,
                                               String currencySymbol, String amount, String txRef) {
        String content =
                badge("Order Confirmed") +
                heading("Payment received") +
                "<p style=\"" + P_STYLE + "\">Hi <strong style=\"" + STRONG_STYLE + "\">" + esc(buyerName) + "</strong>,</p>" +
                "<p style=\"" + P_STYLE + "\">We&rsquo;ve received your payment for <strong style=\"" + STRONG_STYLE + "\">" + esc(ebookTitle) + "</strong>.</p>" +
                infoBox("Ebook: <strong>" + esc(ebookTitle) + "</strong><br/>Amount: " + esc(currencySymbol) + " " + esc(amount) + "<br/>Order Ref: " + esc(txRef)) +
                divider() +
                "<p style=\"" + P_STYLE + "\">You will receive a separate email shortly with your download link. If you don&rsquo;t receive it within 5 minutes, please check your spam folder.</p>" +
                fine("Questions? Contact us at <a href=\"mailto:support@travelmedicineadvisory.com\" style=\"color:#2a7a6a;\">support@travelmedicineadvisory.com</a>.");
        return wrap("Order Confirmed — TMAG Ebook", content);
    }
}
