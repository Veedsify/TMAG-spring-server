package com.TravelMedicineAdvisory.Server.domain.companyadmin.report;

import io.swagger.v3.oas.annotations.tags.Tag;

import com.TravelMedicineAdvisory.Server.core.types.SuccessResponse;
import com.TravelMedicineAdvisory.Server.domain.report.ComplianceAuditDto;
import com.TravelMedicineAdvisory.Server.domain.report.ComplianceReportDto;
import com.TravelMedicineAdvisory.Server.domain.report.DashboardAnalyticsDto;
import com.TravelMedicineAdvisory.Server.domain.report.PlanHistoryDto;
import com.TravelMedicineAdvisory.Server.domain.report.ReportService;
import com.TravelMedicineAdvisory.Server.domain.report.UsageReportDto;
import com.TravelMedicineAdvisory.Server.domain.report.UsageReportSummary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Company admin · Reports")
@RestController
@RequestMapping("/api/v1/company-admin/reports")
public class AdminReportController {

    private final ReportService reportService;

    public AdminReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/dashboard/analytics")
    @PreAuthorize("@perm.company(authentication, #companyId, 'report:read', 'company:read')")
    public ResponseEntity<SuccessResponse> getDashboardAnalytics(@RequestParam(required = false) Long companyId) {
        DashboardAnalyticsDto dto = reportService.getDashboardAnalytics(companyId, null);
        return ResponseEntity.ok(new SuccessResponse("Dashboard analytics fetched successfully", dto));
    }

    @GetMapping("/usage")
    @PreAuthorize("@perm.company(authentication, #companyId, 'report:read', 'plan_usage_ledger:read', 'employee:list')")
    public ResponseEntity<SuccessResponse> getUsageReport(@RequestParam(required = false) Long companyId) {
        UsageReportSummary summary = reportService.getUsageReport(companyId);
        return ResponseEntity.ok(new SuccessResponse("Usage report fetched successfully", summary));
    }

    @GetMapping("/usage/csv")
    @PreAuthorize("@perm.company(authentication, #companyId, 'report:read', 'data_export:read', 'plan_usage_ledger:read', 'employee:list')")
    public ResponseEntity<String> exportUsageReportCsv(@RequestParam(required = false) Long companyId) {
        UsageReportSummary summary = reportService.getUsageReport(companyId);
        String csv = generateUsageReportCsv(summary);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"usage-report.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @GetMapping("/plans")
    @PreAuthorize("@perm.company(authentication, #companyId, 'report:read', 'travel_plan:read', 'travel_plan:list')")
    public ResponseEntity<SuccessResponse> getPlanHistory(@RequestParam(required = false) Long companyId) {
        List<PlanHistoryDto> plans = reportService.getPlanHistory(companyId);
        return ResponseEntity.ok(new SuccessResponse("Plan history fetched successfully", plans));
    }

    @GetMapping("/plans/csv")
    @PreAuthorize("@perm.company(authentication, #companyId, 'report:read', 'data_export:read', 'travel_plan:read', 'travel_plan:list')")
    public ResponseEntity<String> exportPlanHistoryCsv(@RequestParam(required = false) Long companyId) {
        List<PlanHistoryDto> plans = reportService.getPlanHistory(companyId);
        String csv = generatePlanHistoryCsv(plans);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"plan-history.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @GetMapping("/compliance")
    @PreAuthorize("@perm.company(authentication, #companyId, 'report:read', 'health_profile:read', 'travel_plan:read')")
    public ResponseEntity<SuccessResponse> getComplianceReport(@RequestParam(required = false) Long companyId) {
        ComplianceReportDto report = reportService.getComplianceReport(companyId);
        return ResponseEntity.ok(new SuccessResponse("Compliance report fetched successfully", report));
    }

    @GetMapping("/compliance/csv")
    @PreAuthorize("@perm.company(authentication, #companyId, 'report:read', 'data_export:read', 'health_profile:read', 'travel_plan:read')")
    public ResponseEntity<String> exportComplianceReportCsv(@RequestParam(required = false) Long companyId) {
        ComplianceReportDto report = reportService.getComplianceReport(companyId);
        String csv = generateComplianceReportCsv(report);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"compliance-report.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @GetMapping("/team")
    @PreAuthorize("@perm.company(authentication, #companyId, 'report:read', 'employee:list')")
    public ResponseEntity<SuccessResponse> getTeamReport(@RequestParam(required = false) Long companyId) {
        UsageReportSummary summary = reportService.getUsageReport(companyId);
        return ResponseEntity.ok(new SuccessResponse("Team report fetched successfully", summary));
    }

    @GetMapping("/team/csv")
    @PreAuthorize("@perm.company(authentication, #companyId, 'report:read', 'data_export:read', 'employee:list')")
    public ResponseEntity<String> exportTeamReportCsv(@RequestParam(required = false) Long companyId) {
        UsageReportSummary summary = reportService.getUsageReport(companyId);
        String csv = generateTeamReportCsv(summary);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"team-report.csv\"")
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
        sb.append("Plan ID,Destination,Country,Purpose,Trip Type,Trip Details,Duration,Risk Score,Status,Generated Plan Status,Employee Name,Medical Considerations,Vaccinations,Health Alerts,Safety Advisories,Medications,Water and Food,Emergency Contacts,Generated Plan JSON,Signed PDF URL,Summary PDF URL,Created At,Updated At\n");
        for (PlanHistoryDto plan : plans) {
            sb.append(String.format("%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%d,%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                plan.planId(),
                escapeCsv(plan.destination()),
                escapeCsv(plan.country()),
                escapeCsv(plan.purpose()),
                escapeCsv(plan.tripType()),
                escapeCsv(plan.tripDetailsJson()),
                plan.duration() != null ? plan.duration() : 0,
                plan.riskScore() != null ? plan.riskScore() : 0,
                plan.status() != null ? escapeCsv(plan.status()) : "",
                plan.generatedPlanStatus() != null ? escapeCsv(plan.generatedPlanStatus()) : "",
                escapeCsv(plan.employeeName()),
                escapeCsv(plan.medicalConsiderations()),
                escapeCsv(plan.vaccinations()),
                escapeCsv(plan.healthAlerts()),
                escapeCsv(plan.safetyAdvisories()),
                escapeCsv(plan.medications()),
                escapeCsv(plan.waterFood()),
                escapeCsv(plan.emergencyContacts()),
                escapeCsv(plan.generatedPlanJson()),
                escapeCsv(plan.signedPdfUrl()),
                escapeCsv(plan.summaryPdfUrl()),
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

    private String generateTeamReportCsv(UsageReportSummary summary) {
        StringBuilder sb = new StringBuilder();
        sb.append("Name,Email,Department,Status,Credits Allocated,Credits Used,Plans Generated\n");
        for (UsageReportDto emp : summary.employees()) {
            sb.append(String.format("\"%s\",\"%s\",\"%s\",\"%s\",%d,%d,%d\n",
                escapeCsv(emp.employeeName()),
                escapeCsv(emp.employeeEmail()),
                escapeCsv(emp.department()),
                emp.status() != null ? escapeCsv(emp.status()) : "",
                emp.creditsAllocated() != null ? emp.creditsAllocated() : 0,
                emp.creditsUsed() != null ? emp.creditsUsed() : 0,
                emp.plansGenerated() != null ? emp.plansGenerated() : 0
            ));
        }
        sb.append("\nSummary\n");
        sb.append(String.format("Total Employees,%d\n", summary.totalEmployees()));
        sb.append(String.format("Total Plans Generated,%d\n", summary.totalPlansGenerated()));
        sb.append(String.format("Total Credits Used,%d\n", summary.totalCreditsUsed()));
        sb.append(String.format("Total Credits Allocated,%d\n", summary.totalCreditsAllocated()));
        return sb.toString();
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }
}
