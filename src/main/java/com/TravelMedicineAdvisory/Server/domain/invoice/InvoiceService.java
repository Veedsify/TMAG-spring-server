package com.TravelMedicineAdvisory.Server.domain.invoice;

import com.TravelMedicineAdvisory.Server.core.notifications.AdminNotificationService;
import com.TravelMedicineAdvisory.Server.core.queue.JobType;
import com.TravelMedicineAdvisory.Server.domain.company.Company;
import com.TravelMedicineAdvisory.Server.domain.company.CompanyRepository;
import com.TravelMedicineAdvisory.Server.domain.companyuser.CompanyUser;
import com.TravelMedicineAdvisory.Server.domain.companyuser.CompanyUserRepository;
import com.TravelMedicineAdvisory.Server.domain.user.User;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class InvoiceService {

    private static final Logger logger = LoggerFactory.getLogger(InvoiceService.class);

    private final InvoiceRepository repository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final CompanyUserRepository companyUserRepository;
    private final AdminNotificationService adminNotificationService;

    public InvoiceService(InvoiceRepository repository, CompanyRepository companyRepository,
            UserRepository userRepository, CompanyUserRepository companyUserRepository,
            AdminNotificationService adminNotificationService) {
        this.repository = repository;
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.companyUserRepository = companyUserRepository;
        this.adminNotificationService = adminNotificationService;
    }

    public Page<InvoiceResponse> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(this::toResponse);
    }

    public Page<InvoiceResponse> findAllByUser(User currentUser, Pageable pageable) {
        Long companyId = getCompanyIdForUser(currentUser);
        Page<Invoice> page;
        if (companyId != null) {
            page = repository.findAllActiveByCompanyId(companyId, pageable);
        } else {
            page = repository.findAllActiveByUserId(currentUser.getId(), pageable);
        }
        return page.map(this::toResponse);
    }

    private Long getCompanyIdForUser(User user) {
        List<CompanyUser> companyUsers = companyUserRepository.findAllByUser(user);
        if (!companyUsers.isEmpty() && companyUsers.get(0).getCompany() != null) {
            return companyUsers.get(0).getCompany().getId();
        }
        return null;
    }

    private boolean matchesUserScope(Invoice invoice, Long companyId, Long userId) {
        if (companyId != null && invoice.getCompany() != null && invoice.getCompany().getId().equals(companyId)) {
            return true;
        }
        if (invoice.getUser() != null && invoice.getUser().getId().equals(userId)) {
            return true;
        }
        return false;
    }

    public InvoiceResponse findById(Long id, User currentUser) {
        Invoice entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Invoice not found"));
        Long companyId = getCompanyIdForUser(currentUser);
        if (!matchesUserScope(entity, companyId, currentUser.getId())) {
            throw new NoSuchElementException("Invoice not found");
        }
        return toResponse(entity);
    }

    public InvoiceResponse create(InvoiceRequest request) {
        Invoice entity = new Invoice();
        mapRequestToEntity(request, entity);
        Invoice saved = repository.save(entity);

        sendInvoiceEmail(saved);

        return toResponse(saved);
    }

    private void sendInvoiceEmail(Invoice invoice) {
        Long companyId = null;
        String invoiceNumber = "INV-" + String.format("%06d", invoice.getId());
        String currencySymbol = getCurrencySymbol(invoice.getCurrency());

        if (invoice.getCompany() != null) {
            companyId = invoice.getCompany().getId();
        } else if (invoice.getUser() != null) {
            var userCompanies = companyUserRepository.findAllByUser(invoice.getUser());
            if (!userCompanies.isEmpty() && userCompanies.get(0).getCompany() != null) {
                companyId = userCompanies.get(0).getCompany().getId();
            }
        }

        if (companyId == null) {
            logger.warn("Cannot send invoice email: no company found for invoice {}", invoice.getId());
            return;
        }

        adminNotificationService.notifyCompanyAdmins(
                companyId,
                "New invoice #" + invoiceNumber + " from TMAG",
                JobType.EMAIL_INVOICE_AVAILABLE,
                Map.of(
                        "invoiceNumber", invoiceNumber,
                        "amount", invoice.getAmount() != null ? invoice.getAmount().toString() : "0.00",
                        "currencySymbol", currencySymbol));
    }

    private String getCurrencySymbol(String currency) {
        if (currency == null)
            return "$";
        return switch (currency.toUpperCase()) {
            case "USD" -> "$";
            case "EUR" -> "€";
            case "GBP" -> "£";
            case "NGN" -> "₦";
            case "JPY" -> "¥";
            default -> "$";
        };
    }

    public InvoiceResponse update(Long id, InvoiceRequest request) {
        Invoice entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Invoice not found"));
        mapRequestToEntity(request, entity);
        Invoice saved = repository.save(entity);
        return toResponse(saved);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new NoSuchElementException("Invoice not found");
        }
        repository.deleteById(id);
    }

    private InvoiceResponse toResponse(Invoice entity) {
        return new InvoiceResponse(
                entity.getId(),
                entity.getAmount(),
                entity.getCurrency(),
                entity.getStatus(),
                entity.getDescription(),
                entity.getIssuedAt(),
                entity.getDueDate(),
                entity.getPaidAt(),
                entity.getPaymentMethod(),
                entity.getCompany() != null ? entity.getCompany().getId() : null,
                entity.getUser() != null ? entity.getUser().getId() : null,
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    private void mapRequestToEntity(InvoiceRequest request, Invoice entity) {
        entity.setAmount(request.amount());
        entity.setCurrency(request.currency());
        entity.setStatus(request.status());
        entity.setDescription(request.description());
        entity.setIssuedAt(request.issuedAt());
        entity.setDueDate(request.dueDate());
        entity.setPaidAt(request.paidAt());
        entity.setPaymentMethod(request.paymentMethod());
        if (request.companyId() != null) {
            Company company = companyRepository.findById(request.companyId())
                    .orElseThrow(() -> new NoSuchElementException("Company not found"));
            entity.setCompany(company);
        }
        if (request.userId() != null) {
            User user = userRepository.findById(request.userId())
                    .orElseThrow(() -> new NoSuchElementException("User not found"));
            entity.setUser(user);
        }
    }
}
