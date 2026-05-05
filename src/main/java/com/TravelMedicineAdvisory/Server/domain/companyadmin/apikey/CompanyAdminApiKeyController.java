package com.TravelMedicineAdvisory.Server.domain.companyadmin.apikey;

import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.TravelMedicineAdvisory.Server.core.types.SuccessResponse;
import com.TravelMedicineAdvisory.Server.domain.companyapikey.CompanyApiKeyRequest;
import com.TravelMedicineAdvisory.Server.domain.companyapikey.CompanyApiKeyResponse;
import com.TravelMedicineAdvisory.Server.domain.companyapikey.CompanyApiKeyService;
import com.TravelMedicineAdvisory.Server.domain.companyapikey.CompanyApiKeyService.CreateResult;

@Tag(name = "Company admin · API keys")
@RestController
@RequestMapping("/api/v1/company-admin/api-keys")
public class CompanyAdminApiKeyController {

    private final CompanyApiKeyService service;

    public CompanyAdminApiKeyController(CompanyApiKeyService service) {
        this.service = service;
    }

    public record CreateApiKeyResponse(String fullKey, CompanyApiKeyResponse key) {}

    @GetMapping
    @PreAuthorize("@perm.company(authentication, #companyId, 'api_key:list', 'authorization:read')")
    public ResponseEntity<SuccessResponse> list(@RequestParam Long companyId) {
        List<CompanyApiKeyResponse> keys = service.listByCompany(companyId);
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", keys));
    }

    @PostMapping
    @PreAuthorize("@perm.company(authentication, #request.companyId(), 'api_key:create', 'authorization:create')")
    public ResponseEntity<SuccessResponse> create(@RequestBody CompanyApiKeyRequest request) {
        CreateResult result = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SuccessResponse("API key created successfully",
                        new CreateApiKeyResponse(result.fullKey(), result.response())));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@perm.company(authentication, #companyId, 'api_key:delete', 'authorization:delete')")
    public ResponseEntity<SuccessResponse> revoke(@PathVariable Long id, @RequestParam Long companyId) {
        service.revoke(id, companyId);
        return ResponseEntity.ok(new SuccessResponse("API key revoked", null));
    }
}
