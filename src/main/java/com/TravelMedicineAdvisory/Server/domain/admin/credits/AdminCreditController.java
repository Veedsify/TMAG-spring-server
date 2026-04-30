package com.TravelMedicineAdvisory.Server.domain.admin.credits;

import io.swagger.v3.oas.annotations.tags.Tag;

import com.TravelMedicineAdvisory.Server.core.types.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Admin · Credit ledger")
@RestController
@RequestMapping("/api/v1/admin/ledger")
public class AdminCreditController {

    private final AdminCreditService service;

    public AdminCreditController(AdminCreditService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("@perm.admin(authentication, 'credit:list', 'credit:read')")
    public ResponseEntity<SuccessResponse> getLedger(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long companyId) {
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", 
            service.getLedger(userId, companyId)));
    }

    @PostMapping("/adjust")
    @PreAuthorize("@perm.admin(authentication, 'credit:update')")
    public ResponseEntity<SuccessResponse> adjustCredits(@RequestBody Map<String, Object> body) {
        service.adjustCredits(body);
        return ResponseEntity.ok(new SuccessResponse("Credits adjusted", null));
    }
}
