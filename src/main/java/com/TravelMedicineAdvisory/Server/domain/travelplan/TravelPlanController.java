package com.TravelMedicineAdvisory.Server.domain.travelplan;

import java.nio.charset.StandardCharsets;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.TravelMedicineAdvisory.Server.core.types.PaginatedResponse;
import com.TravelMedicineAdvisory.Server.core.types.Pagination;
import com.TravelMedicineAdvisory.Server.core.types.SuccessResponse;
import com.TravelMedicineAdvisory.Server.security.AppUserDetails;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Travel plans")
@RestController
@RequestMapping("/api/v1/travel-plans")
public class TravelPlanController {

    private final TravelPlanService service;

    public TravelPlanController(TravelPlanService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<SuccessResponse> getAll(@RequestParam(required = false) Long companyId, Pageable pageable,
            @AuthenticationPrincipal AppUserDetails user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Page<TravelPlanResponse> page = service.findAll(companyId, user.getUserId(), pageable);
        Pagination pagination = new Pagination(
                (int) page.getTotalElements(),
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalPages());
        PaginatedResponse<TravelPlanResponse> paginatedResponse = new PaginatedResponse(page.getContent(), pagination);
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", paginatedResponse));
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id, @AuthenticationPrincipal AppUserDetails user) {

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        TravelPlanPdfExport exp = service.exportPdfForUser(id, user.getUserId());
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(exp.filenameBase() + "-travel-health.pdf", StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(MediaType.APPLICATION_PDF)
                .body(exp.pdfBytes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse> getById(@PathVariable Long id,
            @AuthenticationPrincipal AppUserDetails user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", service.findById(id, user.getUserId())));
    }

    @PostMapping
    public ResponseEntity<SuccessResponse> create(@RequestBody TravelPlanRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SuccessResponse("Created successfully", service.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SuccessResponse> update(@PathVariable Long id, @RequestBody TravelPlanRequest request) {
        return ResponseEntity.ok(new SuccessResponse("Updated successfully", service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponse> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(new SuccessResponse("Deleted successfully", null));
    }
}
