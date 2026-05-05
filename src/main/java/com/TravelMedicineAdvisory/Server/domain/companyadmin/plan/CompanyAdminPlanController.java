package com.TravelMedicineAdvisory.Server.domain.companyadmin.plan;

import com.TravelMedicineAdvisory.Server.core.types.SuccessResponse;
import com.TravelMedicineAdvisory.Server.domain.creditplan.CreditPlanService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Company Admin · Plans")
@RestController
@RequestMapping("/api/v1/company-admin/plans")
public class CompanyAdminPlanController {

    private final CreditPlanService service;

    public CompanyAdminPlanController(CreditPlanService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("@perm.has(authentication, 'pricing_plan:read')")
    public ResponseEntity<SuccessResponse> list() {
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", service.findAll()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@perm.has(authentication, 'pricing_plan:read')")
    public ResponseEntity<SuccessResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", service.findById(id)));
    }
}
