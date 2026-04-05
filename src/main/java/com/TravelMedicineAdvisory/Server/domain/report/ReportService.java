package com.TravelMedicineAdvisory.Server.domain.report;

import com.TravelMedicineAdvisory.Server.domain.creditrequest.CreditRequest;
import com.TravelMedicineAdvisory.Server.domain.creditrequest.CreditRequestRepository;
import com.TravelMedicineAdvisory.Server.domain.employee.Employee;
import com.TravelMedicineAdvisory.Server.domain.employee.EmployeeRepository;
import com.TravelMedicineAdvisory.Server.domain.planusageledger.PlanUsageLedger;
import com.TravelMedicineAdvisory.Server.domain.planusageledger.PlanUsageLedgerRepository;
import com.TravelMedicineAdvisory.Server.domain.travelplan.TravelPlan;
import com.TravelMedicineAdvisory.Server.domain.travelplan.TravelPlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReportService {

    private final EmployeeRepository employeeRepository;
    private final TravelPlanRepository travelPlanRepository;
    private final PlanUsageLedgerRepository planUsageLedgerRepository;
    private final CreditRequestRepository creditRequestRepository;

    public ReportService(EmployeeRepository employeeRepository,
                         TravelPlanRepository travelPlanRepository,
                         PlanUsageLedgerRepository planUsageLedgerRepository,
                         CreditRequestRepository creditRequestRepository) {
        this.employeeRepository = employeeRepository;
        this.travelPlanRepository = travelPlanRepository;
        this.planUsageLedgerRepository = planUsageLedgerRepository;
        this.creditRequestRepository = creditRequestRepository;
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
                    emp.getUpdatedAt()));
        }

        return new UsageReportSummary(
                employees.size(),
                totalPlans,
                totalCreditsUsed,
                totalCreditsAllocated,
                employeeDtos);
    }

    public List<PlanHistoryDto> getPlanHistory(Long companyId) {
        List<TravelPlan> plans;
        if (companyId != null) {
            plans = travelPlanRepository.findAllActiveByCompanyId(companyId);
        } else {
            plans = travelPlanRepository.findAllActive();
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return plans.stream()
                .map(plan -> new PlanHistoryDto(
                        plan.getId(),
                        plan.getDestination(),
                        plan.getCountry(),
                        plan.getPurpose(),
                        plan.getDuration(),
                        plan.getRiskScore(),
                        plan.getStatus(),
                        plan.getEmployee() != null
                                ? plan.getEmployee().getName()
                                : (plan.getUser() != null ? plan.getUser().getName() : null),
                        plan.getCreatedAt() != null ? plan.getCreatedAt().format(formatter) : null,
                        plan.getUpdatedAt() != null ? plan.getUpdatedAt().format(formatter) : null))
                .toList();
    }

    public ComplianceReportDto getComplianceReport(Long companyId) {
        List<PlanUsageLedger> ledgers;

        if (companyId != null) {
            ledgers = planUsageLedgerRepository.findByCompanyIdOrderByCreatedAtDesc(companyId);
        } else {
            ledgers = planUsageLedgerRepository.findAll();
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        List<ComplianceAuditDto> auditDtos = ledgers.stream()
                .map(ledger -> {
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
                            ledger.getCreatedAt() != null ? ledger.getCreatedAt().format(formatter) : null);
                })
                .toList();

        return new ComplianceReportDto(
                auditDtos,
                auditDtos.size(),
                LocalDateTime.now().format(formatter));
    }

    /**
     * Chart/table data for dashboards. Company scope when {@code companyId} is set; otherwise user scope.
     */
    public DashboardAnalyticsDto getDashboardAnalytics(Long companyId, Long userId) {
        List<TravelPlan> plans;
        if (companyId != null) {
            plans = travelPlanRepository.findAllActiveByCompanyId(companyId);
        } else if (userId != null) {
            plans = travelPlanRepository.findAllActiveByUserId(userId);
        } else {
            plans = List.of();
        }

        List<NamedCountDto> plansByStatus = buildPlansByStatus(plans);
        List<MonthCountDto> byMonth = buildPlansByMonth(plans);
        List<NamedCountDto> topDest = buildTopDestinations(plans);

        List<TopEmployeePlansDto> topEmps = List.of();
        List<NamedCountDto> creditByStatus = List.of();
        if (companyId != null) {
            topEmps = buildTopEmployees(companyId);
            creditByStatus = buildCreditRequestStatusCounts(companyId);
        }

        return new DashboardAnalyticsDto(plansByStatus, byMonth, topDest, topEmps, creditByStatus);
    }

    private List<NamedCountDto> buildPlansByStatus(List<TravelPlan> plans) {
        Map<String, Long> raw = plans.stream()
                .collect(Collectors.groupingBy(p -> normalizePlanStatus(p.getStatus()), Collectors.counting()));

        List<String> preferredOrder = List.of("Completed", "Processing", "Pending", "Failed");
        List<NamedCountDto> ordered = new ArrayList<>();
        for (String label : preferredOrder) {
            Long c = raw.remove(label);
            if (c != null && c > 0) {
                ordered.add(new NamedCountDto(label, c));
            }
        }
        raw.entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .forEach(e -> ordered.add(new NamedCountDto(e.getKey(), e.getValue())));
        return ordered;
    }

    private static String normalizePlanStatus(String s) {
        if (s == null || s.isBlank()) {
            return "Other";
        }
        return switch (s.trim().toUpperCase()) {
            case "COMPLETED" -> "Completed";
            case "PROCESSING" -> "Processing";
            case "PENDING" -> "Pending";
            case "FAILED", "ERROR" -> "Failed";
            default -> {
                String t = s.trim();
                yield t.substring(0, 1).toUpperCase() + t.substring(1).toLowerCase();
            }
        };
    }

    private List<MonthCountDto> buildPlansByMonth(List<TravelPlan> plans) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM yyyy");
        YearMonth end = YearMonth.now();
        List<MonthCountDto> rows = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            YearMonth ym = end.minusMonths(i);
            long cnt = plans.stream()
                    .filter(p -> p.getCreatedAt() != null)
                    .filter(p -> YearMonth.from(p.getCreatedAt()).equals(ym))
                    .count();
            rows.add(new MonthCountDto(ym.format(fmt), cnt));
        }
        return rows;
    }

    private List<NamedCountDto> buildTopDestinations(List<TravelPlan> plans) {
        Map<String, Long> map = plans.stream()
                .map(TravelPlan::getDestination)
                .filter(d -> d != null && !d.isBlank())
                .collect(Collectors.groupingBy(String::trim, Collectors.counting()));
        return map.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(8)
                .map(e -> new NamedCountDto(e.getKey(), e.getValue()))
                .toList();
    }

    private List<TopEmployeePlansDto> buildTopEmployees(Long companyId) {
        return getUsageReport(companyId).employees().stream()
                .sorted((a, b) -> Integer.compare(
                        b.plansGenerated() != null ? b.plansGenerated() : 0,
                        a.plansGenerated() != null ? a.plansGenerated() : 0))
                .limit(8)
                .map(e -> new TopEmployeePlansDto(
                        e.employeeName() != null ? e.employeeName() : "—",
                        e.plansGenerated() != null ? e.plansGenerated() : 0,
                        e.creditsUsed() != null ? e.creditsUsed() : 0))
                .toList();
    }

    private List<NamedCountDto> buildCreditRequestStatusCounts(Long companyId) {
        List<CreditRequest> list = creditRequestRepository.findAllActiveByCompanyId(companyId);
        Map<String, Long> map = new LinkedHashMap<>();
        for (CreditRequest cr : list) {
            String st = cr.getStatus() != null ? cr.getStatus() : "UNKNOWN";
            map.merge(st, 1L, Long::sum);
        }
        return map.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(e -> new NamedCountDto(e.getKey(), e.getValue()))
                .toList();
    }
}
