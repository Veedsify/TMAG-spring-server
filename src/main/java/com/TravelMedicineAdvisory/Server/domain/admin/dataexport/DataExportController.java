package com.TravelMedicineAdvisory.Server.domain.admin.dataexport;

import com.TravelMedicineAdvisory.Server.core.types.SuccessResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/company-admin/data-export")
public class DataExportController {

    private final DataExportService service;

    public DataExportController(DataExportService service) {
        this.service = service;
    }

    @GetMapping("/employees")
    public ResponseEntity<SuccessResponse> exportEmployees(@RequestParam Long companyId) {
        List<Map<String, Object>> data = service.exportEmployees(companyId);
        return ResponseEntity.ok(new SuccessResponse("Employee data fetched successfully", data));
    }

    @GetMapping("/employees/csv")
    public ResponseEntity<String> exportEmployeesCsv(@RequestParam Long companyId) {
        String csv = service.exportEmployeesCsv(companyId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"employees.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @GetMapping("/plans")
    public ResponseEntity<SuccessResponse> exportPlans(@RequestParam Long companyId) {
        List<Map<String, Object>> data = service.exportPlans(companyId);
        return ResponseEntity.ok(new SuccessResponse("Travel plans data fetched successfully", data));
    }

    @GetMapping("/plans/csv")
    public ResponseEntity<String> exportPlansCsv(@RequestParam Long companyId) {
        String csv = service.exportPlansCsv(companyId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"travel-plans.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @GetMapping("/requests")
    public ResponseEntity<SuccessResponse> exportRequests(@RequestParam Long companyId) {
        List<Map<String, Object>> data = service.exportRequests(companyId);
        return ResponseEntity.ok(new SuccessResponse("Credit requests data fetched successfully", data));
    }

    @GetMapping("/requests/csv")
    public ResponseEntity<String> exportRequestsCsv(@RequestParam Long companyId) {
        String csv = service.exportRequestsCsv(companyId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"credit-requests.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @GetMapping("/billing")
    public ResponseEntity<SuccessResponse> exportBilling(@RequestParam Long companyId) {
        List<Map<String, Object>> data = service.exportBilling(companyId);
        return ResponseEntity.ok(new SuccessResponse("Billing data fetched successfully", data));
    }

    @GetMapping("/billing/csv")
    public ResponseEntity<String> exportBillingCsv(@RequestParam Long companyId) {
        String csv = service.exportBillingCsv(companyId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"billing.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @PostMapping("/notify")
    public ResponseEntity<SuccessResponse> notifyExport(@RequestParam Long companyId, @RequestBody List<String> dataTypes) {
        service.sendExportNotification(companyId, dataTypes);
        return ResponseEntity.ok(new SuccessResponse("Export notification sent successfully", null));
    }
}