package com.TravelMedicineAdvisory.Server.domain.creditplan;

import com.TravelMedicineAdvisory.Server.core.types.SuccessResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Public · User Credit Plans")
@RestController
@RequestMapping("/api/v1/user-credit-plans")
public class CreditPlanController {

    private final CreditPlanService service;

    public CreditPlanController(CreditPlanService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<SuccessResponse> list(@RequestParam(required = false) Long companyId) {
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", service.findPublicAndCompanyPlans(companyId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", service.findById(id)));
    }

    @PostMapping("/custom")
    @PreAuthorize("@perm.admin(authentication, 'company:update', 'all')")
    public ResponseEntity<SuccessResponse> createCustom(@RequestBody CustomCreditPlanRequest request) {
        return ResponseEntity.ok(new SuccessResponse("Custom plan created successfully", service.createCustomPlan(request)));
    }
}
