package com.TravelMedicineAdvisory.Server.domain.admin.abuse;

import com.TravelMedicineAdvisory.Server.core.types.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/abuse")
public class AdminAbuseController {

    private final AdminAbuseService service;

    public AdminAbuseController(AdminAbuseService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<SuccessResponse> getAll(@RequestParam(required = false) Boolean resolved) {
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", service.findAll(resolved)));
    }

    @PostMapping("/{id}/resolve")
    public ResponseEntity<SuccessResponse> resolve(@PathVariable Long id) {
        service.resolve(id);
        return ResponseEntity.ok(new SuccessResponse("Abuse flag resolved", null));
    }
}
