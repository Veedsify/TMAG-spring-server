package com.TravelMedicineAdvisory.Server.domain.plangenerationcontext;

import com.TravelMedicineAdvisory.Server.core.types.SuccessResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Admin-facing synthesis and curation of reference materials for {@link com.TravelMedicineAdvisory.Server.domain.plans.PlanGenerationService}.
 * End users create travel plans via the client; they do not use this API.
 */
@Tag(name = "Admin · Plan generation contexts")
@RestController
@RequestMapping("/api/v1/admin/plan-contexts")
@PreAuthorize("hasAuthority('all')")
public class AdminPlanGenerationContextController {

    private final PlanGenerationContextService service;

    public AdminPlanGenerationContextController(PlanGenerationContextService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<SuccessResponse> getAll() {
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", service.findAll()));
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<SuccessResponse> upload(
            @RequestParam("title") String title,
            @RequestParam("file") MultipartFile file
    ) {
        return ResponseEntity.ok(new SuccessResponse("Uploaded successfully", service.create(title, file)));
    }

    @PutMapping("/{id}/active")
    public ResponseEntity<SuccessResponse> setActive(@PathVariable Long id, @RequestParam("active") boolean active) {
        return ResponseEntity.ok(new SuccessResponse("Updated successfully", service.updateActive(id, active)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponse> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(new SuccessResponse("Deleted successfully", null));
    }
}
