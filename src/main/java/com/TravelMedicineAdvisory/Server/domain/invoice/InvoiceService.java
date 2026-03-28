package com.TravelMedicineAdvisory.Server.domain.invoice;

import com.TravelMedicineAdvisory.Server.core.queue.JobType;
import com.TravelMedicineAdvisory.Server.core.queue.QueueService;
import com.TravelMedicineAdvisory.Server.domain.company.Company;
import com.TravelMedicineAdvisory.Server.domain.company.CompanyRepository;
import com.TravelMedicineAdvisory.Server.domain.companyuser.CompanyUser;
import com.TravelMedicineAdvisory.Server.domain.companyuser.CompanyUserRepository;
import com.TravelMedicineAdvisory.Server.domain.user.User;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class InvoiceService {

    private final InvoiceRepository repository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final CompanyUserRepository companyUserRepository;
    private final QueueService queueService;

    public InvoiceService(InvoiceRepository repository, CompanyRepository companyRepository, 
            UserRepository userRepository, CompanyUserRepository companyUserRepository, QueueService queueService) {
        this.repository = repository;
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.companyUserRepository = companyUserRepository;
        this.queueService = queueService;
    }

    public Page<InvoiceResponse> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(this::toResponse);
    }

    public InvoiceResponse findById(Long id) {
        Invoice entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Invoice not found"));
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
        String email;
        String firstName;
        String companyName;

        if (invoice.getUser() != null) {
            email = invoice.getUser().getEmail();
            firstName = invoice.getUser().getFirstName() != null ? invoice.getUser().getFirstName() : "there";
            companyName = invoice.getCompany() != null ? invoice.getCompany().getName() : "TMAG";
        } else if (invoice.getCompany() != null) {
            companyName = invoice.getCompany().getName();
            var companyUserList = companyUserRepository.findAll();
            var companyUser = companyUserList.stream()
                    .filter(cu -> cu.getCompany() != null && cu.getCompany().getId().equals(invoice.getCompany().getId()))
                    .findFirst().orElse(null);
            if (companyUser != null && companyUser.getUser() != null) {
                email = companyUser.getUser().getEmail();
                firstName = companyUser.getUser().getFirstName() != null ? companyUser.getUser().getFirstName() : "there";
            } else {
                email = companyName + "@company.com";
                firstName = "there";
            }
        } else {
            return;
        }

        String invoiceNumber = "INV-" + String.format("%06d", invoice.getId());
        String currencySymbol = getCurrencySymbol(invoice.getCurrency());

        queueService.dispatch(JobType.EMAIL_INVOICE_AVAILABLE, Map.of(
                "to", email,
                "subject", "New invoice #" + invoiceNumber + " from TMAG",
                "variables", Map.of(
                        "firstName", firstName,
                        "invoiceNumber", invoiceNumber,
                        "amount", invoice.getAmount() != null ? invoice.getAmount().toString() : "0.00",
                        "currencySymbol", currencySymbol,
                        "companyName", companyName)));
    }

    private String getCurrencySymbol(String currency) {
        if (currency == null) return "$";
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
            entity.getUpdatedAt()
        );
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
