package com.TravelMedicineAdvisory.Server.domain.admin.credits;

import com.TravelMedicineAdvisory.Server.core.types.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/ledger")
@PreAuthorize("hasAuthority('all')")
public class AdminCreditController {

    private final AdminCreditService service;

    public AdminCreditController(AdminCreditService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<SuccessResponse> getLedger(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long companyId) {
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", 
            service.getLedger(userId, companyId)));
    }

    @PostMapping("/adjust")
    public ResponseEntity<SuccessResponse> adjustCredits(@RequestBody Map<String, Object> body) {
        service.adjustCredits(body);
        return ResponseEntity.ok(new SuccessResponse("Credits adjusted", null));
    }
}
