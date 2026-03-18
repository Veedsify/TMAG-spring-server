package com.TravelMedicineAdvisory.Server.domain.admin.analytics;

import com.TravelMedicineAdvisory.Server.core.types.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasAuthority('all')")
public class AdminAnalyticsController {

    private final AdminAnalyticsService service;

    public AdminAnalyticsController(AdminAnalyticsService service) {
        this.service = service;
    }

    @GetMapping("/dashboard/stats")
    public ResponseEntity<SuccessResponse> getDashboardStats() {
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", service.getDashboardStats()));
    }

    @GetMapping("/analytics")
    public ResponseEntity<SuccessResponse> getAnalytics() {
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", service.getAnalytics()));
    }

    @GetMapping("/ai-logs")
    public ResponseEntity<SuccessResponse> getAILogs(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", 
            service.getAILogs(userId, status)));
    }

    @GetMapping("/ai-logs/{id}")
    public ResponseEntity<SuccessResponse> getAILog(@PathVariable Long id) {
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", service.getAILog(id)));
    }

    @PostMapping("/ai-logs/{id}/flag")
    public ResponseEntity<SuccessResponse> flagAILog(@PathVariable Long id) {
        service.flagAILog(id);
        return ResponseEntity.ok(new SuccessResponse("AI log flagged", null));
    }

    @GetMapping("/plans")
    public ResponseEntity<SuccessResponse> getPlans(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long companyId) {
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", 
            service.getPlans(userId, companyId)));
    }

    @GetMapping("/plans/{id}")
    public ResponseEntity<SuccessResponse> getPlan(@PathVariable Long id) {
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", service.getPlan(id)));
    }

    @PostMapping("/plans/{id}/flag")
    public ResponseEntity<SuccessResponse> flagPlan(@PathVariable Long id) {
        service.flagPlan(id);
        return ResponseEntity.ok(new SuccessResponse("Plan flagged", null));
    }

    @PostMapping("/plans/{id}/archive")
    public ResponseEntity<SuccessResponse> archivePlan(@PathVariable Long id) {
        service.archivePlan(id);
        return ResponseEntity.ok(new SuccessResponse("Plan archived", null));
    }

    @DeleteMapping("/plans/{id}")
    public ResponseEntity<SuccessResponse> deletePlan(@PathVariable Long id) {
        service.deletePlan(id);
        return ResponseEntity.ok(new SuccessResponse("Plan deleted", null));
    }

    @GetMapping("/billing/invoices")
    public ResponseEntity<SuccessResponse> getInvoices() {
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", service.getInvoices()));
    }

    @GetMapping("/billing/invoices/{id}")
    public ResponseEntity<SuccessResponse> getInvoice(@PathVariable Long id) {
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", service.getInvoice(id)));
    }

    @PostMapping("/billing/invoices/{id}/paid")
    public ResponseEntity<SuccessResponse> markInvoicePaid(@PathVariable Long id) {
        service.markInvoicePaid(id);
        return ResponseEntity.ok(new SuccessResponse("Invoice marked as paid", null));
    }
}
