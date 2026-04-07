package com.TravelMedicineAdvisory.Server.domain.admin.system;

import io.swagger.v3.oas.annotations.tags.Tag;

import com.TravelMedicineAdvisory.Server.core.currency.ExchangeRateService;
import com.TravelMedicineAdvisory.Server.core.types.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Admin · System")
@RestController
@RequestMapping("/api/v1/admin/system")
@PreAuthorize("hasAuthority('all')")
public class AdminSystemController {

    private final AdminSystemService service;
    private final ExchangeRateService exchangeRateService;

    public AdminSystemController(AdminSystemService service, ExchangeRateService exchangeRateService) {
        this.service = service;
        this.exchangeRateService = exchangeRateService;
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
        AdminSystemSettingsResponse response = service.updateSettings(updates);
        // Refresh exchange rates after settings update
        exchangeRateService.refreshAdminRates();
        return ResponseEntity.ok(new SuccessResponse("Updated successfully", response));
    }

    @PostMapping("/settings/toggle-maintenance")
    public ResponseEntity<SuccessResponse> toggleMaintenance() {
        service.toggleMaintenance();
        return ResponseEntity.ok(new SuccessResponse("Maintenance mode toggled", null));
    }

    @PostMapping("/settings/fetch-live-rates")
    public ResponseEntity<SuccessResponse> fetchLiveRates() {
        exchangeRateService.fetchRates();
        return ResponseEntity.ok(new SuccessResponse("Live rates fetched", Map.of(
                "rates", exchangeRateService.getRates(),
                "lastFetched", exchangeRateService.getLastFetched()
        )));
    }
}
