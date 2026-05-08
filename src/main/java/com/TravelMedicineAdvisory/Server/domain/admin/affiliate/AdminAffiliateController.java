package com.TravelMedicineAdvisory.Server.domain.admin.affiliate;

import com.TravelMedicineAdvisory.Server.core.types.SuccessResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

@Tag(name = "Admin · Affiliates")
@RestController
@RequestMapping("/api/v1/admin/affiliates")
@PreAuthorize("hasRole('SUPERADMIN')")
public class AdminAffiliateController {

    private final AdminAffiliateService service;

    public AdminAffiliateController(AdminAffiliateService service) {
        this.service = service;
    }

    // ---- Applications -------------------------------------------------------

    @GetMapping("/applications")
    public ResponseEntity<SuccessResponse> listApplications(
            @RequestParam(required = false) String status) {
        if (status != null && !status.isBlank()) {
            return ResponseEntity.ok(new SuccessResponse("Fetched successfully", service.listApplicationsByStatus(status)));
        }
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", service.listAllApplications()));
    }

    @GetMapping("/applications/{id}")
    public ResponseEntity<SuccessResponse> getApplication(@PathVariable Long id) {
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", service.getApplication(id)));
    }

    @PostMapping("/applications/{id}/approve")
    public ResponseEntity<SuccessResponse> approveApplication(@PathVariable Long id) {
        return ResponseEntity.ok(new SuccessResponse("Application approved successfully", service.approveApplication(id)));
    }

    @PostMapping("/applications/{id}/reject")
    public ResponseEntity<SuccessResponse> rejectApplication(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String reason = body.getOrDefault("reason", "");
        return ResponseEntity.ok(new SuccessResponse("Application rejected", service.rejectApplication(id, reason)));
    }

    @PostMapping("/applications/{id}/request-info")
    public ResponseEntity<SuccessResponse> requestInfo(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String notes = body.getOrDefault("notes", "");
        return ResponseEntity.ok(new SuccessResponse("Info requested", service.requestInfo(id, notes)));
    }

    // ---- Affiliate management -----------------------------------------------

    @GetMapping
    public ResponseEntity<SuccessResponse> listAffiliates(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        if (page != null) {
            int pageSize = size != null ? size : 20;
            return ResponseEntity.ok(new SuccessResponse("Fetched successfully", service.listAffiliatesPaged(page, pageSize)));
        }
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", service.listAffiliates()));
    }

    @GetMapping("/stats")
    public ResponseEntity<SuccessResponse> getStats() {
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", service.getStats()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse> getAffiliateDetail(@PathVariable Long id) {
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", service.getAffiliateDetail(id)));
    }

    @PostMapping("/{id}/suspend")
    public ResponseEntity<SuccessResponse> suspend(@PathVariable Long id) {
        return ResponseEntity.ok(new SuccessResponse("Affiliate suspended", service.suspendAffiliate(id)));
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<SuccessResponse> activate(@PathVariable Long id) {
        return ResponseEntity.ok(new SuccessResponse("Affiliate activated", service.activateAffiliate(id)));
    }

    @PutMapping("/{id}/commission-rate")
    public ResponseEntity<SuccessResponse> updateCommissionRate(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        BigDecimal rate = new BigDecimal(body.get("rate").toString());
        return ResponseEntity.ok(new SuccessResponse("Commission rate updated", service.updateCommissionRate(id, rate)));
    }
}
