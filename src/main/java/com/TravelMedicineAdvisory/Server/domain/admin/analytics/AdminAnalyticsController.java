package com.TravelMedicineAdvisory.Server.domain.admin.analytics;

import io.swagger.v3.oas.annotations.tags.Tag;

import com.TravelMedicineAdvisory.Server.core.types.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Admin · Analytics")
@RestController
@RequestMapping("/api/v1/admin")
public class AdminAnalyticsController {

    private final AdminAnalyticsService service;

    public AdminAnalyticsController(AdminAnalyticsService service) {
        this.service = service;
    }

    @GetMapping("/dashboard/stats")
    @PreAuthorize("@perm.admin(authentication, 'system_log:read')")
    public ResponseEntity<SuccessResponse> getDashboardStats() {
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", service.getDashboardStats()));
    }

    @GetMapping("/analytics")
    @PreAuthorize("@perm.admin(authentication, 'system_log:read')")
    public ResponseEntity<SuccessResponse> getAnalytics() {
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", service.getAnalytics()));
    }

    @GetMapping("/ai-logs")
    @PreAuthorize("@perm.admin(authentication, 'ai_request_log:list')")
    public ResponseEntity<SuccessResponse> getAILogs(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", 
            service.getAILogs(userId, status)));
    }

    @GetMapping("/ai-logs/{id}")
    @PreAuthorize("@perm.admin(authentication, 'ai_request_log:read')")
    public ResponseEntity<SuccessResponse> getAILog(@PathVariable Long id) {
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", service.getAILog(id)));
    }

    @PostMapping("/ai-logs/{id}/flag")
    @PreAuthorize("@perm.admin(authentication, 'ai_request_log:update')")
    public ResponseEntity<SuccessResponse> flagAILog(@PathVariable Long id) {
        service.flagAILog(id);
        return ResponseEntity.ok(new SuccessResponse("AI log flagged", null));
    }

    @GetMapping("/plans")
    @PreAuthorize("@perm.admin(authentication, 'travel_plan:list')")
    public ResponseEntity<SuccessResponse> getPlans(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long companyId) {
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", 
            service.getPlans(userId, companyId)));
    }

    @GetMapping("/plans/{id}")
    @PreAuthorize("@perm.admin(authentication, 'travel_plan:read')")
    public ResponseEntity<SuccessResponse> getPlan(@PathVariable Long id) {
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", service.getPlan(id)));
    }

    @PostMapping("/plans/{id}/flag")
    @PreAuthorize("@perm.admin(authentication, 'travel_plan:update')")
    public ResponseEntity<SuccessResponse> flagPlan(@PathVariable Long id) {
        service.flagPlan(id);
        return ResponseEntity.ok(new SuccessResponse("Plan flagged", null));
    }

    @PostMapping("/plans/{id}/archive")
    @PreAuthorize("@perm.admin(authentication, 'travel_plan:update')")
    public ResponseEntity<SuccessResponse> archivePlan(@PathVariable Long id) {
        service.archivePlan(id);
        return ResponseEntity.ok(new SuccessResponse("Plan archived", null));
    }

    @DeleteMapping("/plans/{id}")
    @PreAuthorize("@perm.admin(authentication, 'travel_plan:delete')")
    public ResponseEntity<SuccessResponse> deletePlan(@PathVariable Long id) {
        service.deletePlan(id);
        return ResponseEntity.ok(new SuccessResponse("Plan deleted", null));
    }

    @GetMapping("/billing/invoices")
    @PreAuthorize("@perm.admin(authentication, 'invoice:list')")
    public ResponseEntity<SuccessResponse> getInvoices() {
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", service.getInvoices()));
    }

    @GetMapping("/billing/invoices/{id}")
    @PreAuthorize("@perm.admin(authentication, 'invoice:read')")
    public ResponseEntity<SuccessResponse> getInvoice(@PathVariable Long id) {
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", service.getInvoice(id)));
    }

    @PostMapping("/billing/invoices")
    @PreAuthorize("@perm.admin(authentication, 'invoice:create')")
    public ResponseEntity<SuccessResponse> createInvoice(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(new SuccessResponse("Created successfully", service.createInvoice(body)));
    }

    @PutMapping("/billing/invoices/{id}")
    @PreAuthorize("@perm.admin(authentication, 'invoice:update')")
    public ResponseEntity<SuccessResponse> updateInvoice(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        return ResponseEntity.ok(new SuccessResponse("Updated successfully", service.updateInvoice(id, updates)));
    }

    @PostMapping("/billing/invoices/{id}/paid")
    @PreAuthorize("@perm.admin(authentication, 'invoice:update')")
    public ResponseEntity<SuccessResponse> markInvoicePaid(@PathVariable Long id) {
        service.markInvoicePaid(id);
        return ResponseEntity.ok(new SuccessResponse("Invoice marked as paid", null));
    }
}
