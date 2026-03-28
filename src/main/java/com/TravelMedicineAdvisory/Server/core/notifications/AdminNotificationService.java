package com.TravelMedicineAdvisory.Server.core.notifications;

import com.TravelMedicineAdvisory.Server.core.queue.JobType;
import com.TravelMedicineAdvisory.Server.core.queue.QueueService;
import com.TravelMedicineAdvisory.Server.domain.companyuser.CompanyUser;
import com.TravelMedicineAdvisory.Server.domain.companyuser.CompanyUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(AdminNotificationService.class);

    private final CompanyUserRepository companyUserRepository;
    private final QueueService queueService;

    public AdminNotificationService(CompanyUserRepository companyUserRepository, QueueService queueService) {
        this.companyUserRepository = companyUserRepository;
        this.queueService = queueService;
    }

    public void notifyCompanyAdmins(Long companyId, String subject, JobType jobType, Map<String, String> variables) {
        if (companyId == null) {
            logger.warn("Cannot notify admins: companyId is null");
            return;
        }

        List<CompanyUser> admins = companyUserRepository.findAdminsByCompanyId(companyId);
        
        if (admins.isEmpty()) {
            logger.warn("No admins found for company {}", companyId);
            return;
        }

        for (CompanyUser admin : admins) {
            if (admin.getUser() == null) continue;
            
            String adminEmail = admin.getUser().getEmail();
            String firstName = admin.getUser().getFirstName() != null 
                    ? admin.getUser().getFirstName() 
                    : "there";

            Map<String, String> varsWithName = new java.util.HashMap<>(variables);
            varsWithName.putIfAbsent("firstName", firstName);
            varsWithName.putIfAbsent("companyName", admin.getCompany() != null 
                    ? admin.getCompany().getName() 
                    : "your company");

            queueService.dispatch(jobType, Map.of(
                    "to", adminEmail,
                    "subject", subject,
                    "variables", varsWithName));

            logger.info("Sent notification to admin {} (role: {}) for company {}", 
                    adminEmail, admin.getRole(), companyId);
        }
    }

    public void notifyCompanyAdminsByEmail(Long companyId, String subject, String htmlContent) {
        if (companyId == null) {
            logger.warn("Cannot notify admins: companyId is null");
            return;
        }

        List<CompanyUser> admins = companyUserRepository.findAdminsByCompanyId(companyId);
        List<String> emails = admins.stream()
                .filter(cu -> cu.getUser() != null)
                .map(cu -> cu.getUser().getEmail())
                .distinct()
                .collect(Collectors.toList());

        if (emails.isEmpty()) {
            logger.warn("No admin emails found for company {}", companyId);
            return;
        }

        for (String email : emails) {
            queueService.dispatch(JobType.EMAIL_GENERIC, Map.of(
                    "to", email,
                    "subject", subject,
                    "variables", Map.of("content", htmlContent)));
        }
    }
}