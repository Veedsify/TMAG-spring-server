package com.TravelMedicineAdvisory.Server.domain.report;

import io.swagger.v3.oas.annotations.tags.Tag;

import com.TravelMedicineAdvisory.Server.core.types.SuccessResponse;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Reports")
@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final ReportService reportService;
    private final UserRepository userRepository;

    public ReportController(ReportService reportService, UserRepository userRepository) {
        this.reportService = reportService;
        this.userRepository = userRepository;
    }

    @GetMapping("/dashboard/analytics")
    @PreAuthorize("@perm.has(authentication, 'report:read', 'system_log:read')")
    public ResponseEntity<SuccessResponse> getDashboardAnalytics(@RequestParam(required = false) Long companyId) {
        Long userId = null;
        if (companyId == null) {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            userId = userRepository.findByEmail(email).map(u -> u.getId()).orElse(null);
        }
        DashboardAnalyticsDto dto = reportService.getDashboardAnalytics(companyId, userId);
        return ResponseEntity.ok(new SuccessResponse("Dashboard analytics fetched successfully", dto));
    }

    @GetMapping("/usage")
    @PreAuthorize("@perm.has(authentication, 'report:read', 'plan_usage_ledger:read', 'employee:list')")
    public ResponseEntity<SuccessResponse> getUsageReport(@RequestParam(required = false) Long companyId) {
        UsageReportSummary summary = reportService.getUsageReport(companyId);
        return ResponseEntity.ok(new SuccessResponse("Usage report fetched successfully", summary));
    }

    @GetMapping("/usage/csv")
    @PreAuthorize("@perm.has(authentication, 'data_export:read', 'report:read', 'plan_usage_ledger:read')")
    public ResponseEntity<String> exportUsageReportCsv(@RequestParam(required = false) Long companyId) {
        UsageReportSummary summary = reportService.getUsageReport(companyId);
        String csv = generateUsageReportCsv(summary);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"usage-report.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @GetMapping("/plans")
    @PreAuthorize("@perm.has(authentication, 'report:read', 'travel_plan:list', 'travel_plan:read')")
    public ResponseEntity<SuccessResponse> getPlanHistory(@RequestParam(required = false) Long companyId) {
        List<PlanHistoryDto> plans = reportService.getPlanHistory(companyId);
        return ResponseEntity.ok(new SuccessResponse("Plan history fetched successfully", plans));
    }

    @GetMapping("/plans/csv")
    @PreAuthorize("@perm.has(authentication, 'data_export:read', 'report:read', 'travel_plan:list')")
    public ResponseEntity<String> exportPlanHistoryCsv(@RequestParam(required = false) Long companyId) {
        List<PlanHistoryDto> plans = reportService.getPlanHistory(companyId);
        String csv = generatePlanHistoryCsv(plans);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"plan-history.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @GetMapping("/compliance")
    @PreAuthorize("@perm.has(authentication, 'report:read', 'plan_usage_ledger:read', 'system_log:read')")
    public ResponseEntity<SuccessResponse> getComplianceReport(@RequestParam(required = false) Long companyId) {
        ComplianceReportDto report = reportService.getComplianceReport(companyId);
        return ResponseEntity.ok(new SuccessResponse("Compliance report fetched successfully", report));
    }

    @GetMapping("/compliance/csv")
    @PreAuthorize("@perm.has(authentication, 'data_export:read', 'report:read', 'plan_usage_ledger:read')")
    public ResponseEntity<String> exportComplianceReportCsv(@RequestParam(required = false) Long companyId) {
        ComplianceReportDto report = reportService.getComplianceReport(companyId);
        String csv = generateComplianceReportCsv(report);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"compliance-report.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    private String generateUsageReportCsv(UsageReportSummary summary) {
        StringBuilder sb = new StringBuilder();
        sb.append("Employee Name,Email,Department,Credits Allocated,Credits Used,Plans Generated,Status,Last Activity\n");
        for (UsageReportDto emp : summary.employees()) {
            sb.append(String.format("\"%s\",\"%s\",\"%s\",%d,%d,%d,\"%s\",\"%s\"\n",
                escapeCsv(emp.employeeName()),
                escapeCsv(emp.employeeEmail()),
                escapeCsv(emp.department()),
                emp.creditsAllocated() != null ? emp.creditsAllocated() : 0,
                emp.creditsUsed() != null ? emp.creditsUsed() : 0,
                emp.plansGenerated() != null ? emp.plansGenerated() : 0,
                emp.status() != null ? escapeCsv(emp.status()) : "",
                emp.lastActivityAt() != null ? emp.lastActivityAt().toString() : ""
            ));
        }
        sb.append("\nSummary\n");
        sb.append(String.format("Total Employees,%d\n", summary.totalEmployees()));
        sb.append(String.format("Total Plans Generated,%d\n", summary.totalPlansGenerated()));
        sb.append(String.format("Total Credits Used,%d\n", summary.totalCreditsUsed()));
        sb.append(String.format("Total Credits Allocated,%d\n", summary.totalCreditsAllocated()));
        return sb.toString();
    }

    private String generatePlanHistoryCsv(List<PlanHistoryDto> plans) {
        StringBuilder sb = new StringBuilder();
        sb.append("Plan ID,Destination,Country,Purpose,Duration,Risk Score,Status,Employee Name,Created At,Updated At\n");
        for (PlanHistoryDto plan : plans) {
            sb.append(String.format("%d,\"%s\",\"%s\",\"%s\",%d,%d,\"%s\",\"%s\",\"%s\",\"%s\"\n",
                plan.planId(),
                escapeCsv(plan.destination()),
                escapeCsv(plan.country()),
                escapeCsv(plan.purpose()),
                plan.duration() != null ? plan.duration() : 0,
                plan.riskScore() != null ? plan.riskScore() : 0,
                plan.status() != null ? escapeCsv(plan.status()) : "",
                escapeCsv(plan.employeeName()),
                plan.createdAt() != null ? plan.createdAt() : "",
                plan.updatedAt() != null ? plan.updatedAt() : ""
            ));
        }
        return sb.toString();
    }

    private String generateComplianceReportCsv(ComplianceReportDto report) {
        StringBuilder sb = new StringBuilder();
        sb.append("Ledger ID,Action,Employee Name,Plan Destination,IP Address,User Agent,Timestamp\n");
        for (ComplianceAuditDto audit : report.audits()) {
            sb.append(String.format("%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                audit.ledgerId(),
                escapeCsv(audit.action()),
                escapeCsv(audit.employeeName()),
                escapeCsv(audit.planDestination()),
                escapeCsv(audit.ipAddress()),
                escapeCsv(audit.userAgent()),
                audit.timestamp() != null ? audit.timestamp() : ""
            ));
        }
        sb.append(String.format("\nTotal Records,%d\n", report.totalRecords()));
        sb.append(String.format("Generated At,%s\n", report.generatedAt()));
        return sb.toString();
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }
}
