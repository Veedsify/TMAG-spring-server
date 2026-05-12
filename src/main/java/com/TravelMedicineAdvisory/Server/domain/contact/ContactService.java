package com.TravelMedicineAdvisory.Server.domain.contact;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.TravelMedicineAdvisory.Server.core.queue.JobType;
import com.TravelMedicineAdvisory.Server.core.queue.QueueService;
import com.TravelMedicineAdvisory.Server.domain.contact.ContactDto.ContactRequest;
import com.TravelMedicineAdvisory.Server.domain.contact.ContactDto.ContactResponse;
import com.TravelMedicineAdvisory.Server.domain.role.Roles;
import com.TravelMedicineAdvisory.Server.domain.user.User;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;

@Service
public class ContactService {

    Logger logger = org.slf4j.LoggerFactory.getLogger(ContactService.class);

    private final ContactRepository repository;
    private final QueueService queueService;

    @Value("${app.admin.email:hello@tmag.health}")
    private String adminEmail;

    public ContactService(ContactRepository repository, QueueService queueService, UserRepository userRepository) {
        this.repository = repository;
        this.queueService = queueService;
        List<User> AdminUser = userRepository.findByRoleName(Roles.SuperAdmin.name());
        if (AdminUser.isEmpty()) {
            logger.warn("No SuperAdmin user found. Admin email will default to hardcoded value: " + adminEmail);
        } else {
            this.adminEmail = AdminUser.get(0).getEmail();
        }
    }

    @Transactional
    public ContactResponse submit(ContactRequest request, String ipAddress, String userAgent) {
        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (request.email() == null || request.email().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (request.subject() == null || request.subject().isBlank()) {
            throw new IllegalArgumentException("Subject is required");
        }
        if (request.message() == null || request.message().isBlank()) {
            throw new IllegalArgumentException("Message is required");
        }
        if (request.inquiryType() == null) {
            throw new IllegalArgumentException("Inquiry type is required");
        }

        Contact contact = new Contact();
        contact.setName(request.name());
        contact.setEmail(request.email());
        contact.setSubject(request.subject());
        contact.setMessage(request.message());
        contact.setInquiryType(request.inquiryType());
        contact.setStatus(ContactStatus.NEW);
        contact.setIpAddress(ipAddress);
        contact.setUserAgent(userAgent);

        Contact saved = repository.save(contact);

        String firstName = request.name().split("\\s+")[0];

        queueService.dispatch(JobType.EMAIL_CONTACT_ACKNOWLEDGMENT, Map.of(
                "to", request.email(),
                "subject", "We received your message — TMAG",
                "variables", Map.of(
                        "firstName", firstName,
                        "subject", request.subject())));

        queueService.dispatch(JobType.EMAIL_CONTACT_SUBMISSION, Map.of(
                "to", adminEmail,
                "subject", "New contact submission: " + request.subject(),
                "variables", Map.of(
                        "firstName", "Team",
                        "name", request.name(),
                        "email", request.email(),
                        "inquiryType", request.inquiryType().name(),
                        "subject", request.subject(),
                        "message", request.message())));

        return toResponse(saved);
    }

    private ContactResponse toResponse(Contact c) {
        return new ContactResponse(
                c.getId(),
                c.getName(),
                c.getEmail(),
                c.getSubject(),
                c.getMessage(),
                c.getInquiryType(),
                c.getStatus(),
                c.getCreatedAt());
    }
}
