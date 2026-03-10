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

    @Value("${app.email.from-address}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
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
}
