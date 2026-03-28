package com.TravelMedicineAdvisory.Server.domain.companyadmin.apikey;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.TravelMedicineAdvisory.Server.core.types.SuccessResponse;
import com.TravelMedicineAdvisory.Server.domain.companyapikey.CompanyApiKeyRequest;
import com.TravelMedicineAdvisory.Server.domain.companyapikey.CompanyApiKeyResponse;
import com.TravelMedicineAdvisory.Server.domain.companyapikey.CompanyApiKeyService;
import com.TravelMedicineAdvisory.Server.domain.companyapikey.CompanyApiKeyService.CreateResult;

@RestController
@RequestMapping("/api/v1/company-admin/api-keys")
public class CompanyAdminApiKeyController {

    private final CompanyApiKeyService service;

    public CompanyAdminApiKeyController(CompanyApiKeyService service) {
        this.service = service;
    }

    public record CreateApiKeyResponse(String fullKey, CompanyApiKeyResponse key) {}

    @GetMapping
    public ResponseEntity<SuccessResponse> list(@RequestParam Long companyId) {
        List<CompanyApiKeyResponse> keys = service.listByCompany(companyId);
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", keys));
    }

    @PostMapping
    public ResponseEntity<SuccessResponse> create(@RequestBody CompanyApiKeyRequest request) {
        CreateResult result = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SuccessResponse("API key created successfully",
                        new CreateApiKeyResponse(result.fullKey(), result.response())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponse> revoke(@PathVariable Long id, @RequestParam Long companyId) {
        service.revoke(id, companyId);
        return ResponseEntity.ok(new SuccessResponse("API key revoked", null));
    }
}
