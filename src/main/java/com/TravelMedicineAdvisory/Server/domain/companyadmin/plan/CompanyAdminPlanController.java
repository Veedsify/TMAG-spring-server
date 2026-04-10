package com.TravelMedicineAdvisory.Server.domain.companyadmin.plan;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.TravelMedicineAdvisory.Server.core.types.SuccessResponse;
import com.TravelMedicineAdvisory.Server.domain.companyplan.PlanPdfGenerator;
import com.TravelMedicineAdvisory.Server.domain.companyplan.PlanRequest;
import com.TravelMedicineAdvisory.Server.domain.companyplan.PlanService;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Company admin · Plans")
@RestController
@RequestMapping("/api/v1/company-admin/plans")
public class CompanyAdminPlanController {

    private final PlanService service;
    private final PlanPdfGenerator pdfGenerator;

    public CompanyAdminPlanController(PlanService service, PlanPdfGenerator pdfGenerator) {
        this.service = service;
        this.pdfGenerator = pdfGenerator;
    }

    @GetMapping
    public ResponseEntity<SuccessResponse> list() {
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", service.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", service.findById(id)));
    }

    @PostMapping
    public ResponseEntity<SuccessResponse> create(@RequestBody PlanRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SuccessResponse("Created successfully", service.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SuccessResponse> update(@PathVariable Long id, @RequestBody PlanRequest request) {
        return ResponseEntity.ok(new SuccessResponse("Updated successfully", service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponse> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(new SuccessResponse("Deleted successfully", null));
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
        var plan = service.findById(id);
        byte[] pdf = pdfGenerator.generate(plan);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + plan.code().toLowerCase() + "-plan.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
