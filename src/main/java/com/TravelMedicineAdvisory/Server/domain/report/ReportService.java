package com.TravelMedicineAdvisory.Server.domain.report;

import com.TravelMedicineAdvisory.Server.domain.companyuser.CompanyUserRepository;
import com.TravelMedicineAdvisory.Server.domain.employee.Employee;
import com.TravelMedicineAdvisory.Server.domain.employee.EmployeeRepository;
import com.TravelMedicineAdvisory.Server.domain.planusageledger.PlanUsageLedger;
import com.TravelMedicineAdvisory.Server.domain.planusageledger.PlanUsageLedgerRepository;
import com.TravelMedicineAdvisory.Server.domain.travelplan.TravelPlan;
import com.TravelMedicineAdvisory.Server.domain.travelplan.TravelPlanRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReportService {

    private final EmployeeRepository employeeRepository;
    private final TravelPlanRepository travelPlanRepository;
    private final PlanUsageLedgerRepository planUsageLedgerRepository;
    private final CompanyUserRepository companyUserRepository;

    public ReportService(EmployeeRepository employeeRepository,
                         TravelPlanRepository travelPlanRepository,
                         PlanUsageLedgerRepository planUsageLedgerRepository,
                         CompanyUserRepository companyUserRepository) {
        this.employeeRepository = employeeRepository;
        this.travelPlanRepository = travelPlanRepository;
        this.planUsageLedgerRepository = planUsageLedgerRepository;
        this.companyUserRepository = companyUserRepository;
    }

    public UsageReportSummary getUsageReport(Long companyId) {
        List<Employee> employees;
        if (companyId != null) {
            employees = employeeRepository.findAllByCompanyId(companyId);
        } else {
            employees = employeeRepository.findAll();
        }

        List<UsageReportDto> employeeDtos = new ArrayList<>();
        int totalPlans = 0;
        int totalCreditsUsed = 0;
        int totalCreditsAllocated = 0;

        for (Employee emp : employees) {
            int creditsUsed = emp.getCreditsUsed() != null ? emp.getCreditsUsed() : 0;
            int creditsAllocated = emp.getCreditsAllocated() != null ? emp.getCreditsAllocated() : 0;
            int plansGen = emp.getPlansGenerated() != null ? emp.getPlansGenerated() : 0;

            totalPlans += plansGen;
            totalCreditsUsed += creditsUsed;
            totalCreditsAllocated += creditsAllocated;

            employeeDtos.add(new UsageReportDto(
                emp.getName(),
                emp.getEmail(),
                emp.getDepartment(),
                creditsAllocated,
                creditsUsed,
                plansGen,
                emp.getStatus(),
                emp.getUpdatedAt()
            ));
        }

        return new UsageReportSummary(
            employees.size(),
            totalPlans,
            totalCreditsUsed,
            totalCreditsAllocated,
            employeeDtos
        );
    }

    public List<PlanHistoryDto> getPlanHistory(Long companyId) {
        List<TravelPlan> plans;
        if (companyId != null) {
            plans = travelPlanRepository.findAllActiveByCompanyId(companyId);
        } else {
            plans = travelPlanRepository.findAllActive();
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return plans.stream().map(plan -> new PlanHistoryDto(
            plan.getId(),
            plan.getDestination(),
            plan.getCountry(),
            plan.getPurpose(),
            plan.getDuration(),
            plan.getRiskScore(),
            plan.getStatus(),
            plan.getEmployee() != null ? plan.getEmployee().getName() : (plan.getUser() != null ? plan.getUser().getName() : null),
            plan.getCreatedAt() != null ? plan.getCreatedAt().format(formatter) : null,
            plan.getUpdatedAt() != null ? plan.getUpdatedAt().format(formatter) : null
        )).toList();
    }

    public ComplianceReportDto getComplianceReport(Long companyId) {
        List<PlanUsageLedger> ledgers;

        if (companyId != null) {
            ledgers = planUsageLedgerRepository.findByCompanyIdOrderByCreatedAtDesc(companyId);
        } else {
            ledgers = planUsageLedgerRepository.findAll();
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        List<ComplianceAuditDto> auditDtos = ledgers.stream().map(ledger -> {
            String employeeName = null;
            if (ledger.getTravelPlan() != null && ledger.getTravelPlan().getEmployee() != null) {
                employeeName = ledger.getTravelPlan().getEmployee().getName();
            } else if (ledger.getUser() != null) {
                employeeName = ledger.getUser().getName();
            }

            return new ComplianceAuditDto(
                ledger.getId(),
                ledger.getAction(),
                employeeName,
                ledger.getTravelPlan() != null ? ledger.getTravelPlan().getDestination() : null,
                ledger.getIpAddress(),
                ledger.getUserAgent(),
                ledger.getCreatedAt() != null ? ledger.getCreatedAt().format(formatter) : null
            );
        }).toList();

        return new ComplianceReportDto(
            auditDtos,
            auditDtos.size(),
            LocalDateTime.now().format(formatter)
        );
    }
}
