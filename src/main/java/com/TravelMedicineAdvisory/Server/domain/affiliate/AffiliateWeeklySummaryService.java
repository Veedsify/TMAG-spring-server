package com.TravelMedicineAdvisory.Server.domain.affiliate;

import com.TravelMedicineAdvisory.Server.core.email.EmailService;
import com.TravelMedicineAdvisory.Server.core.email.EmailTemplates;
import com.TravelMedicineAdvisory.Server.domain.role.RoleRepository;
import com.TravelMedicineAdvisory.Server.domain.user.User;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Sends a weekly affiliate performance summary to super-admin(s) every Monday at 9 AM.
 */
@Service
public class AffiliateWeeklySummaryService {

    private static final Logger logger = LoggerFactory.getLogger(AffiliateWeeklySummaryService.class);

    private final AffiliateProfileRepository affiliateProfileRepository;
    private final AffiliateClickRepository affiliateClickRepository;
    private final AffiliateCommissionRepository affiliateCommissionRepository;
    private final AffiliatePayoutRepository affiliatePayoutRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmailService emailService;

    public AffiliateWeeklySummaryService(
            AffiliateProfileRepository affiliateProfileRepository,
            AffiliateClickRepository affiliateClickRepository,
            AffiliateCommissionRepository affiliateCommissionRepository,
            AffiliatePayoutRepository affiliatePayoutRepository,
            UserRepository userRepository,
            RoleRepository roleRepository,
            EmailService emailService) {
        this.affiliateProfileRepository = affiliateProfileRepository;
        this.affiliateClickRepository = affiliateClickRepository;
        this.affiliateCommissionRepository = affiliateCommissionRepository;
        this.affiliatePayoutRepository = affiliatePayoutRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.emailService = emailService;
    }

    /**
     * Runs every Monday at 9:00 AM. Sends weekly affiliate performance summary
     * to all super-admin users.
     */
    @Scheduled(cron = "0 0 9 * * MON")
    @Transactional(readOnly = true)
    public void sendWeeklySummary() {
        try {
            logger.info("Generating weekly affiliate performance summary...");

            LocalDate now = LocalDate.now();
            LocalDate weekAgo = now.minusDays(7);
            LocalDateTime since = weekAgo.atStartOfDay();

            long totalActive = affiliateProfileRepository.countByStatusAndDeletedAtIsNull("active");
            long weeklyClicks = affiliateClickRepository.countByCreatedAtAfterAndDeletedAtIsNull(since);
            BigDecimal weeklyCommissions = affiliateCommissionRepository.sumAmountByCreatedAtAfter(since);
            BigDecimal weeklyPayouts = affiliatePayoutRepository.sumAmountByCreatedAtAfter(since);

            String summaryHtml = buildSummaryHtml(totalActive, weeklyClicks, weeklyCommissions, weeklyPayouts, weekAgo, now);

            // Send to all super-admins
            List<User> superAdmins = userRepository.findByRoleName("SuperAdmin");
            for (User admin : superAdmins) {
                try {
                    emailService.sendHtmlEmail(
                            admin.getEmail(),
                            "Weekly Affiliate Performance Summary",
                            summaryHtml
                    );
                } catch (Exception e) {
                    logger.error("Failed to send weekly summary to {}: {}", admin.getEmail(), e.getMessage());
                }
            }

            logger.info("Weekly affiliate performance summary sent to {} super-admin(s)", superAdmins.size());
        } catch (Exception e) {
            logger.error("Failed to generate weekly affiliate summary: {}", e.getMessage());
        }
    }

    private String buildSummaryHtml(long totalActive, long weeklyClicks, BigDecimal weeklyCommissions,
                                     BigDecimal weeklyPayouts, LocalDate weekStart, LocalDate weekEnd) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        return """
                <div style="font-family: 'Hanken Grotesk', Helvetica, Arial, sans-serif; color: #3d2c1e;">
                    <h2 style="font-family: 'Fraunces', Georgia, serif; font-weight: 400; font-size: 24px; margin: 0 0 16px;">
                        Weekly Affiliate Performance
                    </h2>
                    <p style="color: #8a7968; font-size: 14px; margin: 0 0 24px;">
                        %s – %s
                    </p>
                    <table style="width: 100%%; border-collapse: collapse; margin-bottom: 24px;">
                        <tr>
                            <td style="padding: 12px 16px; background: #fcf6ef; border: 1px solid #e8ddd3; border-radius: 8px 0 0 8px;">
                                <p style="margin: 0 0 4px; font-size: 11px; text-transform: uppercase; letter-spacing: 1px; color: #b0a090;">Active Affiliates</p>
                                <p style="margin: 0; font-size: 28px; font-weight: 600; color: #2a7a6a;">%d</p>
                            </td>
                            <td style="padding: 12px 16px; background: #fcf6ef; border: 1px solid #e8ddd3; border-left: none;">
                                <p style="margin: 0 0 4px; font-size: 11px; text-transform: uppercase; letter-spacing: 1px; color: #b0a090;">Weekly Clicks</p>
                                <p style="margin: 0; font-size: 28px; font-weight: 600; color: #2a7a6a;">%d</p>
                            </td>
                        </tr>
                        <tr>
                            <td style="padding: 12px 16px; background: #fcf6ef; border: 1px solid #e8ddd3; border-top: none; border-radius: 0 0 0 8px;">
                                <p style="margin: 0 0 4px; font-size: 11px; text-transform: uppercase; letter-spacing: 1px; color: #b0a090;">Commissions Earned (7d)</p>
                                <p style="margin: 0; font-size: 28px; font-weight: 600; color: #2a7a6a;">$%s</p>
                            </td>
                            <td style="padding: 12px 16px; background: #fcf6ef; border: 1px solid #e8ddd3; border-top: none; border-left: none; border-radius: 0 0 8px 0;">
                                <p style="margin: 0 0 4px; font-size: 11px; text-transform: uppercase; letter-spacing: 1px; color: #b0a090;">Payouts Processed (7d)</p>
                                <p style="margin: 0; font-size: 28px; font-weight: 600; color: #2a7a6a;">$%s</p>
                            </td>
                        </tr>
                    </table>
                    <p style="color: #8a7968; font-size: 13px; margin: 0;">
                        Log in to the admin dashboard for detailed affiliate performance metrics and charts.
                    </p>
                </div>
                """.formatted(
                weekStart.format(fmt),
                weekEnd.format(fmt),
                totalActive,
                weeklyClicks,
                weeklyCommissions != null ? weeklyCommissions.setScale(2, RoundingMode.HALF_UP).toPlainString() : "0.00",
                weeklyPayouts != null ? weeklyPayouts.setScale(2, RoundingMode.HALF_UP).toPlainString() : "0.00"
        );
    }
}
