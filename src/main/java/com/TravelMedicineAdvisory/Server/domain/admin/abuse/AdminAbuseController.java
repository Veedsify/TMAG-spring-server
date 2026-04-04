package com.TravelMedicineAdvisory.Server.domain.admin.abuse;

import io.swagger.v3.oas.annotations.tags.Tag;

import com.TravelMedicineAdvisory.Server.core.types.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin · Abuse")
@RestController
@RequestMapping("/api/v1/admin/abuse")
@PreAuthorize("hasAuthority('all')")
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
