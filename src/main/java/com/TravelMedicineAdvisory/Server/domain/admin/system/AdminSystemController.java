package com.TravelMedicineAdvisory.Server.domain.admin.system;

import com.TravelMedicineAdvisory.Server.core.types.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/system")
@PreAuthorize("hasAuthority('all')")
public class AdminSystemController {

    private final AdminSystemService service;

    public AdminSystemController(AdminSystemService service) {
        this.service = service;
    }

    @GetMapping("/status")
    public ResponseEntity<SuccessResponse> getSystemStatus() {
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", service.getSystemStatus()));
    }

    @GetMapping("/logs")
    public ResponseEntity<SuccessResponse> getSystemLogs(
            @RequestParam(required = false) String level,
            @RequestParam(required = false) Integer limit) {
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", 
            service.getSystemLogs(level, limit)));
    }

    @GetMapping("/settings")
    public ResponseEntity<SuccessResponse> getSettings() {
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", service.getSettings()));
    }

    @PutMapping("/settings")
    public ResponseEntity<SuccessResponse> updateSettings(@RequestBody Map<String, Object> updates) {
        return ResponseEntity.ok(new SuccessResponse("Updated successfully", service.updateSettings(updates)));
    }

    @PostMapping("/settings/toggle-maintenance")
    public ResponseEntity<SuccessResponse> toggleMaintenance() {
        service.toggleMaintenance();
        return ResponseEntity.ok(new SuccessResponse("Maintenance mode toggled", null));
    }
}
