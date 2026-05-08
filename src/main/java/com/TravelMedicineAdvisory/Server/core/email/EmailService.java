package com.TravelMedicineAdvisory.Server.core.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;
    private final EmailTemplates emailTemplates;

    @Value("${app.email.from-address}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender, EmailTemplates emailTemplates) {
        this.mailSender = mailSender;
        this.emailTemplates = emailTemplates;
    }

    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            message.setFrom(fromEmail);
            mailSender.send(message);
        } catch (Exception e) {
            logger.error("Failed to send plain text email to {}: {}", to, e.getMessage());
        }
    }

    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            if (to == null || to.isEmpty()) {
                logger.warn("Attempted to send email with empty recipient. Subject: {}", subject);
                return;
            }

            if (htmlBody == null || htmlBody.isEmpty()) {
                logger.warn("Attempted to send email with empty body to {}. Subject: {}", to, subject);
                return;
            }

            if (subject == null) {
                logger.warn("Attempted to send email with null subject to {}. Body length: {}", to, htmlBody.length());
            }

            if (fromEmail == null || fromEmail.isBlank()) {
                logger.warn("Attempted to send email with empty sender address. Recipient: {}, Subject: {}", to, subject);
                return;
            }

            helper.setTo(to);
            helper.setSubject(subject != null ? subject : "");
            helper.setText(htmlBody, true);
            helper.setFrom(fromEmail != null ? fromEmail : "");
            
            mailSender.send(message);
        } catch (MessagingException e) {
            logger.error("Failed to send HTML email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }

    public void sendAffiliateWelcomeEmail(String to, String name, String email, String tempPassword, String loginUrl) {
        try {
            sendHtmlEmail(to, "Welcome to TMAG Affiliate Program",
                    emailTemplates.affiliateWelcome(name, email, tempPassword, loginUrl));
        } catch (Exception e) {
            logger.error("Failed to send affiliate welcome email to {}: {}", to, e.getMessage());
        }
    }

    public void sendAffiliateRejectionEmail(String to, String name, String reason) {
        try {
            sendHtmlEmail(to, "Your TMAG Affiliate Application",
                    emailTemplates.affiliateRejection(name, reason));
        } catch (Exception e) {
            logger.error("Failed to send affiliate rejection email to {}: {}", to, e.getMessage());
        }
    }

    public void sendEmailWithAttachment(String to, String subject, String htmlBody,
                                         byte[] attachment, String attachmentName, String attachmentContentType) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            if (to == null || to.isEmpty()) {
                logger.warn("Attempted to send email with empty recipient. Subject: {}", subject);
                return;
            }

            if (fromEmail == null || fromEmail.isBlank()) {
                logger.warn("Attempted to send email with empty sender address. Recipient: {}, Subject: {}", to, subject);
                return;
            }

            helper.setTo(to);
            helper.setSubject(subject != null ? subject : "");
            helper.setText(htmlBody, true);
            helper.setFrom(fromEmail);
            helper.addAttachment(attachmentName, new org.springframework.core.io.ByteArrayResource(attachment), attachmentContentType);

            mailSender.send(message);
            logger.info("Email with attachment sent to {}", to);
        } catch (MessagingException e) {
            logger.error("Failed to send email with attachment to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send email with attachment", e);
        }
    }

    public void sendAffiliateApplicationConfirmation(String to, String firstName) {
        try {
            sendHtmlEmail(to, "Application Received — TMAG Affiliate Program",
                    emailTemplates.affiliateApplicationConfirmation(firstName));
        } catch (Exception e) {
            logger.error("Failed to send affiliate application confirmation to {}: {}", to, e.getMessage());
        }
    }

    public void sendAffiliateCommissionEarned(String to, String firstName, String amount, String customerEmail, String campaign) {
        try {
            sendHtmlEmail(to, "You earned a commission on TMAG!",
                    emailTemplates.affiliateCommissionEarned(firstName, amount, customerEmail, campaign));
        } catch (Exception e) {
            logger.error("Failed to send affiliate commission earned email to {}: {}", to, e.getMessage());
        }
    }

    public void sendAffiliatePayoutProcessed(String to, String firstName, String amount, String paymentMethod) {
        try {
            sendHtmlEmail(to, "Your TMAG affiliate payout has been processed",
                    emailTemplates.affiliatePayoutProcessed(firstName, amount, paymentMethod));
        } catch (Exception e) {
            logger.error("Failed to send affiliate payout processed email to {}: {}", to, e.getMessage());
        }
    }
}
