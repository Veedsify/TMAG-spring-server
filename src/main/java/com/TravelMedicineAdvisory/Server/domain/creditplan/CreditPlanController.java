package com.TravelMedicineAdvisory.Server.domain.creditplan;

import com.TravelMedicineAdvisory.Server.core.types.SuccessResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    public ResponseEntity<SuccessResponse> list() {
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", service.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", service.findById(id)));
    }
}
