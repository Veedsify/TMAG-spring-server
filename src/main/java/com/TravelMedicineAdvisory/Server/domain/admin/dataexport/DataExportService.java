package com.TravelMedicineAdvisory.Server.domain.admin.dataexport;

import com.TravelMedicineAdvisory.Server.core.notifications.AdminNotificationService;
import com.TravelMedicineAdvisory.Server.core.queue.JobType;
import com.TravelMedicineAdvisory.Server.core.queue.QueueService;
import com.TravelMedicineAdvisory.Server.domain.company.Company;
import com.TravelMedicineAdvisory.Server.domain.company.CompanyRepository;
import com.TravelMedicineAdvisory.Server.domain.companyuser.CompanyUser;
import com.TravelMedicineAdvisory.Server.domain.companyuser.CompanyUserRepository;
import com.TravelMedicineAdvisory.Server.domain.creditrequest.CreditRequest;
import com.TravelMedicineAdvisory.Server.domain.creditrequest.CreditRequestRepository;
import com.TravelMedicineAdvisory.Server.domain.employee.Employee;
import com.TravelMedicineAdvisory.Server.domain.employee.EmployeeRepository;
import com.TravelMedicineAdvisory.Server.domain.invoice.Invoice;
import com.TravelMedicineAdvisory.Server.domain.invoice.InvoiceRepository;
import com.TravelMedicineAdvisory.Server.domain.travelplan.TravelPlan;
import com.TravelMedicineAdvisory.Server.domain.travelplan.TravelPlanRepository;
import com.TravelMedicineAdvisory.Server.domain.user.User;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DataExportService {

    private static final Logger logger = LoggerFactory.getLogger(DataExportService.class);

    private final EmployeeRepository employeeRepository;
    private final TravelPlanRepository travelPlanRepository;
    private final CreditRequestRepository creditRequestRepository;
    private final InvoiceRepository invoiceRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final CompanyUserRepository companyUserRepository;
    private final QueueService queueService;
    private final AdminNotificationService adminNotificationService;

    public DataExportService(EmployeeRepository employeeRepository,
            TravelPlanRepository travelPlanRepository,
            CreditRequestRepository creditRequestRepository,
            InvoiceRepository invoiceRepository,
            CompanyRepository companyRepository,
            UserRepository userRepository,
            CompanyUserRepository companyUserRepository,
            QueueService queueService,
            AdminNotificationService adminNotificationService) {
        this.employeeRepository = employeeRepository;
        this.travelPlanRepository = travelPlanRepository;
        this.creditRequestRepository = creditRequestRepository;
        this.invoiceRepository = invoiceRepository;
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.companyUserRepository = companyUserRepository;
        this.queueService = queueService;
        this.adminNotificationService = adminNotificationService;
    }

    public List<Map<String, Object>> exportEmployees(Long companyId) {
        List<Employee> employees = employeeRepository.findAllByCompanyId(companyId);
        return employees.stream().map(this::mapEmployeeToExport).collect(Collectors.toList());
    }

    public String exportEmployeesCsv(Long companyId) {
        List<Employee> employees = employeeRepository.findAllByCompanyId(companyId);
        StringBuilder sb = new StringBuilder();
        sb.append("ID,Name,Email,Department,Status,Credits Allocated,Credits Used,Plans Generated,Created\n");
        for (Employee emp : employees) {
            sb.append(String.format("%d,\"%s\",\"%s\",\"%s\",\"%s\",%d,%d,%d,\"%s\"\n",
                    emp.getId(),
                    escapeCsv(emp.getName()),
                    escapeCsv(emp.getEmail()),
                    escapeCsv(emp.getDepartment()),
                    escapeCsv(emp.getStatus()),
                    emp.getCreditsAllocated() != null ? emp.getCreditsAllocated() : 0,
                    emp.getCreditsUsed() != null ? emp.getCreditsUsed() : 0,
                    emp.getPlansGenerated() != null ? emp.getPlansGenerated() : 0,
                    emp.getCreatedAt() != null ? emp.getCreatedAt().toString() : ""));
        }
        return sb.toString();
    }

    public List<Map<String, Object>> exportPlans(Long companyId) {
        List<TravelPlan> plans = travelPlanRepository.findAllActiveByCompanyId(companyId);
        return plans.stream().map(this::mapPlanToExport).collect(Collectors.toList());
    }

    public String exportPlansCsv(Long companyId) {
        List<TravelPlan> plans = travelPlanRepository.findAllActiveByCompanyId(companyId);
        StringBuilder sb = new StringBuilder();
        sb.append("ID,Destination,Country,Duration,Purpose,Risk Score,Status,Created\n");
        for (TravelPlan plan : plans) {
            sb.append(String.format("%d,\"%s\",\"%s\",%d,\"%s\",%d,\"%s\",\"%s\"\n",
                    plan.getId(),
                    escapeCsv(plan.getDestination()),
                    escapeCsv(plan.getCountry()),
                    plan.getDuration() != null ? plan.getDuration() : 0,
                    escapeCsv(plan.getPurpose()),
                    plan.getRiskScore() != null ? plan.getRiskScore() : 0,
                    escapeCsv(plan.getStatus()),
                    plan.getCreatedAt() != null ? plan.getCreatedAt().toString() : ""));
        }
        return sb.toString();
    }

    public List<Map<String, Object>> exportRequests(Long companyId) {
        Page<CreditRequest> page = creditRequestRepository.findAllByCompanyId(companyId, PageRequest.of(0, 1000));
        return page.getContent().stream().map(this::mapRequestToExport).collect(Collectors.toList());
    }

    public String exportRequestsCsv(Long companyId) {
        Page<CreditRequest> page = creditRequestRepository.findAllByCompanyId(companyId, PageRequest.of(0, 1000));
        List<CreditRequest> requests = page.getContent();
        StringBuilder sb = new StringBuilder();
        sb.append("ID,Credits Requested,Reason,Status,Submitted At,Created\n");
        for (CreditRequest req : requests) {
            sb.append(String.format("%d,%d,\"%s\",\"%s\",\"%s\",\"%s\"\n",
                    req.getId(),
                    req.getCreditsRequested() != null ? req.getCreditsRequested() : 0,
                    escapeCsv(req.getReason()),
                    escapeCsv(req.getStatus()),
                    req.getSubmittedAt() != null ? req.getSubmittedAt().toString() : "",
                    req.getCreatedAt() != null ? req.getCreatedAt().toString() : ""));
        }
        return sb.toString();
    }

    public List<Map<String, Object>> exportBilling(Long companyId) {
        List<Invoice> invoices = invoiceRepository.findByCompanyId(companyId);
        return invoices.stream().map(this::mapInvoiceToExport).collect(Collectors.toList());
    }

    public String exportBillingCsv(Long companyId) {
        List<Invoice> invoices = invoiceRepository.findByCompanyId(companyId);
        StringBuilder sb = new StringBuilder();
        sb.append("ID,Amount,Currency,Status,Description,Issued,Due,Paid,Payment Method\n");
        for (Invoice inv : invoices) {
            sb.append(String.format("%d,%s,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                    inv.getId(),
                    inv.getAmount() != null ? inv.getAmount().toString() : "0",
                    escapeCsv(inv.getCurrency()),
                    escapeCsv(inv.getStatus()),
                    escapeCsv(inv.getDescription()),
                    inv.getIssuedAt() != null ? inv.getIssuedAt().toString() : "",
                    inv.getDueDate() != null ? inv.getDueDate().toString() : "",
                    inv.getPaidAt() != null ? inv.getPaidAt().toString() : "",
                    escapeCsv(inv.getPaymentMethod())));
        }
        return sb.toString();
    }

    public void sendExportNotification(Long companyId, List<String> dataTypes) {
        Company company = companyRepository.findById(companyId).orElse(null);
        if (company == null) return;

        String exportTypes = String.join(", ", dataTypes);
        
        adminNotificationService.notifyCompanyAdmins(
                companyId,
                "Your data export is ready for " + company.getName(),
                JobType.EMAIL_DATA_EXPORT,
                Map.of(
                        "exportType", exportTypes,
                        "companyName", company.getName()));
        
        logger.info("Data export notification sent to company {} for types: {}", companyId, exportTypes);
    }

    private Map<String, Object> mapEmployeeToExport(Employee emp) {
        return Map.of(
                "id", emp.getId(),
                "name", emp.getName() != null ? emp.getName() : "",
                "email", emp.getEmail() != null ? emp.getEmail() : "",
                "department", emp.getDepartment() != null ? emp.getDepartment() : "",
                "status", emp.getStatus() != null ? emp.getStatus() : "",
                "creditsAllocated", emp.getCreditsAllocated() != null ? emp.getCreditsAllocated() : 0,
                "creditsUsed", emp.getCreditsUsed() != null ? emp.getCreditsUsed() : 0,
                "plansGenerated", emp.getPlansGenerated() != null ? emp.getPlansGenerated() : 0,
                "createdAt", emp.getCreatedAt() != null ? emp.getCreatedAt().toString() : "");
    }

    private Map<String, Object> mapPlanToExport(TravelPlan plan) {
        return Map.of(
                "id", plan.getId(),
                "destination", plan.getDestination() != null ? plan.getDestination() : "",
                "country", plan.getCountry() != null ? plan.getCountry() : "",
                "duration", plan.getDuration() != null ? plan.getDuration() : 0,
                "purpose", plan.getPurpose() != null ? plan.getPurpose() : "",
                "riskScore", plan.getRiskScore() != null ? plan.getRiskScore() : 0,
                "status", plan.getStatus() != null ? plan.getStatus() : "",
                "createdAt", plan.getCreatedAt() != null ? plan.getCreatedAt().toString() : "");
    }

    private Map<String, Object> mapRequestToExport(CreditRequest req) {
        return Map.of(
                "id", req.getId(),
                "creditsRequested", req.getCreditsRequested() != null ? req.getCreditsRequested() : 0,
                "reason", req.getReason() != null ? req.getReason() : "",
                "status", req.getStatus() != null ? req.getStatus() : "",
                "submittedAt", req.getSubmittedAt() != null ? req.getSubmittedAt().toString() : "",
                "createdAt", req.getCreatedAt() != null ? req.getCreatedAt().toString() : "");
    }

    private Map<String, Object> mapInvoiceToExport(Invoice inv) {
        return Map.of(
                "id", inv.getId(),
                "amount", inv.getAmount() != null ? inv.getAmount().toString() : "",
                "currency", inv.getCurrency() != null ? inv.getCurrency() : "",
                "status", inv.getStatus() != null ? inv.getStatus() : "",
                "description", inv.getDescription() != null ? inv.getDescription() : "",
                "issuedAt", inv.getIssuedAt() != null ? inv.getIssuedAt().toString() : "",
                "dueDate", inv.getDueDate() != null ? inv.getDueDate().toString() : "",
                "paidAt", inv.getPaidAt() != null ? inv.getPaidAt().toString() : "",
                "paymentMethod", inv.getPaymentMethod() != null ? inv.getPaymentMethod() : "");
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }
}