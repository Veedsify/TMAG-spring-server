package com.TravelMedicineAdvisory.Server.domain.travelplan;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.TravelMedicineAdvisory.Server.core.types.SuccessResponse;

@RestController
@RequestMapping("/api/v1/test/travel-plans")
public class TravelPlanTestController {

    private final TravelPlanService service;

    public TravelPlanTestController(TravelPlanService service) {
        this.service = service;
    }

    @PostMapping("/{id}/duplicate")
    public ResponseEntity<SuccessResponse> duplicate(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SuccessResponse("Duplicated successfully", service.duplicateForTest(id)));
    }
}
