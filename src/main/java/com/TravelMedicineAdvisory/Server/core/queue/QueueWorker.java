package com.TravelMedicineAdvisory.Server.core.queue;

import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.TravelMedicineAdvisory.Server.core.email.EmailService;
import com.TravelMedicineAdvisory.Server.core.email.EmailTemplates;
import static com.TravelMedicineAdvisory.Server.core.queue.QueueService.KEY_DELAYED;
import static com.TravelMedicineAdvisory.Server.core.queue.QueueService.KEY_FAILED;
import static com.TravelMedicineAdvisory.Server.core.queue.QueueService.KEY_PENDING;
import com.TravelMedicineAdvisory.Server.domain.plans.PlanGenerationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class QueueWorker {

    private static final Logger logger = LoggerFactory.getLogger(QueueWorker.class);
    private static final int BATCH_SIZE = 10;

    private final StringRedisTemplate redis;
    private final EmailService emailService;
    private final EmailTemplates emailTemplates;
    private final ObjectMapper objectMapper;
    private final PlanGenerationService planGenerationService;

    public QueueWorker(StringRedisTemplate redis, EmailService emailService,
            EmailTemplates emailTemplates, ObjectMapper objectMapper,
            PlanGenerationService planGenerationService) {
        this.redis = redis;
        this.emailService = emailService;
        this.emailTemplates = emailTemplates;
        this.objectMapper = objectMapper;
        this.planGenerationService = planGenerationService;
    }

    @Scheduled(fixedDelayString = "${app.queue.worker.interval-ms:5000}")
    public void processJobs() {
        promoteDelayedJobs();

        for (int i = 0; i < BATCH_SIZE; i++) {
            String json = redis.opsForList().rightPop(KEY_PENDING);
            if (json == null) {
                break;
            }
            processMessage(json);
        }
    }

    /**
     * Moves jobs from the delayed sorted set into the pending list when their
     * scheduled time has arrived.
     */
    private void promoteDelayedJobs() {
        long now = System.currentTimeMillis();
        Set<String> ready = redis.opsForZSet().rangeByScore(KEY_DELAYED, 0, now);
        if (ready == null || ready.isEmpty()) {
            return;
        }

        for (String json : ready) {
            redis.opsForZSet().remove(KEY_DELAYED, json);
            redis.opsForList().leftPush(KEY_PENDING, json);
        }
        logger.debug("Promoted {} delayed job(s) to pending", ready.size());
    }

    private void processMessage(String json) {
        QueueMessage msg;
        try {
            msg = objectMapper.readValue(json, QueueMessage.class);
        } catch (JsonProcessingException e) {
            logger.error("Malformed queue message, discarding: {}", e.getMessage());
            return;
        }

        msg.setAttempts(msg.getAttempts() + 1);

        try {
            switch (msg.getType()) {
                case GENERATE_TRAVEL_PLAN -> {
                    Number travelPlanId = (Number) msg.getData().get("travelPlanId");
                    if (travelPlanId == null) {
                        throw new IllegalArgumentException("Missing travelPlanId in queue payload");
                    }
                    long planId = travelPlanId.longValue();
                    logger.info("GENERATE_TRAVEL_PLAN starting: queueJobId={} travelPlanId={}", msg.getId(), planId);
                    planGenerationService.processQueuedGeneration(planId);
                }
                case EMAIL_VERIFICATION ->
                    handleEmailJob(msg, "verification");
                case EMAIL_PASSWORD_RESET ->
                    handleEmailJob(msg, "password_reset");
                case EMAIL_PASSWORD_CHANGED ->
                    handleEmailJob(msg, "password_changed");
                case EMAIL_EMPLOYEE_INVITATION ->
                    handleEmailJob(msg, "employee_invitation");
                case EMAIL_GENERIC ->
                    handleEmailJob(msg, "generic");
                case EMAIL_CREDIT_PURCHASE ->
                    handleEmailJob(msg, "credit_purchase");
                case EMAIL_CREDIT_ALLOCATION ->
                    handleEmailJob(msg, "credit_allocation");
                case EMAIL_CREDIT_REQUEST_APPROVED ->
                    handleEmailJob(msg, "credit_request_approved");
                case EMAIL_CREDIT_REQUEST_REJECTED ->
                    handleEmailJob(msg, "credit_request_rejected");
                case EMAIL_EMPLOYEE_STATUS_CHANGED ->
                    handleEmailJob(msg, "employee_status_changed");
                case EMAIL_EMPLOYEE_REMOVED ->
                    handleEmailJob(msg, "employee_removed");
                case EMAIL_API_KEY_CREATED ->
                    handleEmailJob(msg, "api_key_created");
                case EMAIL_API_KEY_REVOKED ->
                    handleEmailJob(msg, "api_key_revoked");
                case EMAIL_TRAVEL_PLAN_CREATED ->
                    handleEmailJob(msg, "travel_plan_created");
                case EMAIL_LOGIN_ALERT ->
                    handleEmailJob(msg, "login_alert");
                case EMAIL_INVOICE_AVAILABLE ->
                    handleEmailJob(msg, "invoice_available");
                case EMAIL_ONBOARDING_REMINDER ->
                    handleEmailJob(msg, "onboarding_reminder");
                case EMAIL_TWO_FACTOR_ENABLED ->
                    handleEmailJob(msg, "two_factor_enabled");
                case EMAIL_TWO_FACTOR_DISABLED ->
                    handleEmailJob(msg, "two_factor_disabled");
                case EMAIL_BILLING_CURRENCY_CHANGED ->
                    handleEmailJob(msg, "billing_currency_changed");
                case EMAIL_DATA_EXPORT ->
                    handleEmailJob(msg, "data_export");
                case EMAIL_INVITATION_ACCEPTED ->
                    handleEmailJob(msg, "invitation_accepted");
                case EMAIL_CREDIT_REQUEST_SUBMITTED ->
                    handleEmailJob(msg, "credit_request_submitted");
                case EMAIL_COMPANY_ADMIN_ONBOARDING ->
                    handleEmailJob(msg, "company_admin_onboarding");
                case EMAIL_CONTACT_ACKNOWLEDGMENT ->
                    handleEmailJob(msg, "contact_acknowledgment");
                case EMAIL_CONTACT_SUBMISSION ->
                    handleEmailJob(msg, "contact_submission");
                case EMAIL_NEWSLETTER_WELCOME ->
                    handleEmailJob(msg, "newsletter_welcome");
            }
            if (msg.getType() == JobType.GENERATE_TRAVEL_PLAN) {
                logger.info("GENERATE_TRAVEL_PLAN finished successfully: queueJobId={}", msg.getId());
            } else {
                logger.info("Queue job [{}] id={} completed", msg.getType(), msg.getId());
            }

        } catch (Exception e) {
            if (msg.getType() == JobType.GENERATE_TRAVEL_PLAN) {
                logger.error("GENERATE_TRAVEL_PLAN failed: queueJobId={} attempt={}/{} — {}",
                        msg.getId(), msg.getAttempts(), msg.getMaxAttempts(), e.getMessage());
            } else {
                logger.error("Queue job [{}] id={} failed (attempt {}/{}): {}",
                        msg.getType(), msg.getId(), msg.getAttempts(), msg.getMaxAttempts(), e.getMessage());
            }

            if (msg.getAttempts() >= msg.getMaxAttempts()) {
                // Dead-letter: store for inspection / manual replay
                try {
                    redis.opsForList().leftPush(KEY_FAILED, objectMapper.writeValueAsString(msg));
                } catch (JsonProcessingException ex) {
                    logger.error("Failed to push job to dead-letter queue: {}", ex.getMessage());
                }
                logger.error("Queue job [{}] id={} permanently failed after {} attempts",
                        msg.getType(), msg.getId(), msg.getAttempts());
            } else {
                // Exponential backoff: 2, 4, 8 minutes
                long backoffMs = (long) Math.pow(2, msg.getAttempts()) * 60_000L;
                long processAt = System.currentTimeMillis() + backoffMs;
                try {
                    redis.opsForZSet().add(KEY_DELAYED, objectMapper.writeValueAsString(msg), processAt);
                    logger.info("Queue job [{}] id={} scheduled for retry in {}m",
                            msg.getType(), msg.getId(), backoffMs / 60_000L);
                } catch (JsonProcessingException ex) {
                    logger.error("Failed to schedule retry for job id={}: {}", msg.getId(), ex.getMessage());
                }
            }
        }
    }

    private void handleEmailJob(QueueMessage msg, String templateType) throws Exception {
        Map<String, Object> data = msg.getData();
        String to = (String) data.get("to");
        String subject = (String) data.get("subject");

        @SuppressWarnings("unchecked")
        Map<String, String> vars = data.containsKey("variables")
                ? (Map<String, String>) data.get("variables")
                : Map.of();

        String firstName = vars.getOrDefault("firstName", "there");
        String link = vars.getOrDefault("link", "#");
        String code = vars.getOrDefault("code", "");

        String companyName = vars.getOrDefault("companyName", "");
        String credits = vars.getOrDefault("credits", "0");
        String currencySymbol = vars.getOrDefault("currencySymbol", "$");
        String amount = vars.getOrDefault("amount", "0");
        String status = vars.getOrDefault("status", "active");
        String reason = vars.getOrDefault("reason", "");
        String keyName = vars.getOrDefault("keyName", "");
        String destination = vars.getOrDefault("destination", "");
        String location = vars.getOrDefault("location", "Unknown");
        String device = vars.getOrDefault("device", "Unknown");
        String timestamp = vars.getOrDefault("timestamp", "");
        String invoiceNumber = vars.getOrDefault("invoiceNumber", "");
        String oldCurrency = vars.getOrDefault("oldCurrency", "");
        String newCurrency = vars.getOrDefault("newCurrency", "");
        String exportType = vars.getOrDefault("exportType", "");
        String employeeName = vars.getOrDefault("employeeName", "");
        String name = vars.getOrDefault("name", "");
        String message = vars.getOrDefault("message", "");
        String inquiryType = vars.getOrDefault("inquiryType", "");
        String senderEmail = vars.getOrDefault("email", to);

        String html = switch (templateType) {
            case "verification" ->
                emailTemplates.verificationEmail(firstName, code);
            case "password_reset" ->
                emailTemplates.passwordResetEmail(firstName, link);
            case "password_changed" ->
                emailTemplates.passwordChangedEmail(firstName);
            case "employee_invitation" ->
                emailTemplates.employeeInvitationEmail(firstName, companyName, vars.getOrDefault("role", "Individual"), link);
            case "credit_purchase" ->
                emailTemplates.creditPurchaseConfirmationEmail(firstName, Integer.parseInt(credits), currencySymbol, amount);
            case "credit_allocation" ->
                emailTemplates.creditAllocationNotificationEmail(firstName, Integer.parseInt(credits), companyName);
            case "credit_request_approved" ->
                emailTemplates.creditRequestApprovedEmail(firstName, Integer.parseInt(credits), companyName);
            case "credit_request_rejected" ->
                emailTemplates.creditRequestRejectedEmail(firstName, Integer.parseInt(credits), reason, companyName);
            case "employee_status_changed" ->
                emailTemplates.employeeStatusChangedEmail(firstName, status, companyName);
            case "employee_removed" ->
                emailTemplates.employeeRemovedEmail(firstName, companyName);
            case "api_key_created" ->
                emailTemplates.apiKeyCreatedEmail(firstName, keyName, companyName);
            case "api_key_revoked" ->
                emailTemplates.apiKeyRevokedEmail(firstName, keyName, companyName);
            case "travel_plan_created" ->
                emailTemplates.travelPlanCreatedEmail(firstName, destination, companyName);
            case "login_alert" ->
                emailTemplates.loginAlertEmail(firstName, location, device, timestamp);
            case "invoice_available" ->
                emailTemplates.invoiceAvailableEmail(firstName, invoiceNumber, amount, currencySymbol, companyName);
            case "onboarding_reminder" ->
                emailTemplates.onboardingReminderEmail(firstName, companyName);
            case "two_factor_enabled" ->
                emailTemplates.twoFactorEnabledEmail(firstName);
            case "two_factor_disabled" ->
                emailTemplates.twoFactorDisabledEmail(firstName);
            case "billing_currency_changed" ->
                emailTemplates.billingCurrencyChangedEmail(firstName, oldCurrency, newCurrency, companyName);
            case "data_export" ->
                emailTemplates.dataExportEmail(firstName, exportType, companyName);
            case "invitation_accepted" ->
                emailTemplates.invitationAcceptedEmail(firstName, vars.getOrDefault("employeeName", "User"), companyName);
            case "credit_request_submitted" ->
                emailTemplates.creditRequestSubmittedEmail(firstName, employeeName, Integer.parseInt(credits), companyName);
            case "company_admin_onboarding" ->
                emailTemplates.companyAdminOnboardingEmail(firstName, companyName, vars.getOrDefault("temporaryPassword", ""));
            case "contact_acknowledgment" ->
                emailTemplates.contactAcknowledgmentEmail(firstName, subject);
            case "contact_submission" ->
                emailTemplates.contactSubmissionEmail(name, senderEmail, inquiryType, subject, message);
            case "newsletter_welcome" ->
                emailTemplates.newsletterWelcomeEmail(firstName);
            default ->
                emailTemplates.genericEmail(subject, vars.getOrDefault("content", ""));
        };

        emailService.sendHtmlEmail(to, subject, html);
    }
}
