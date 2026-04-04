package com.TravelMedicineAdvisory.Server.domain.companyadmin.setting;

import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.TravelMedicineAdvisory.Server.core.types.SuccessResponse;
import com.TravelMedicineAdvisory.Server.domain.company.BillingCurrency;
import com.TravelMedicineAdvisory.Server.domain.companysetting.CompanySettingRequest;
import com.TravelMedicineAdvisory.Server.domain.companysetting.CompanySettingResponse;
import com.TravelMedicineAdvisory.Server.domain.companysetting.CompanySettingService;

@Tag(name = "Company admin · Settings")
@RestController
@RequestMapping("/api/v1/company-admin/settings")
public class CompanyAdminSettingController {

    private final CompanySettingService service;

    public CompanyAdminSettingController(CompanySettingService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<SuccessResponse> get(@RequestParam Long companyId) {
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", service.getByCompany(companyId)));
    }

    @PutMapping
    public ResponseEntity<SuccessResponse> update(@RequestBody CompanySettingRequest request) {
        return ResponseEntity.ok(new SuccessResponse("Updated successfully", service.upsert(request.companyId(), request)));
    }

    @PutMapping("/billing-currency")
    public ResponseEntity<SuccessResponse> updateBillingCurrency(
            @RequestParam Long companyId,
            @RequestParam BillingCurrency currency) {
        service.updateBillingCurrency(companyId, currency);
        return ResponseEntity.ok(new SuccessResponse("Billing currency updated", null));
    }
}
