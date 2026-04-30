package com.TravelMedicineAdvisory.Server.domain.draftplan;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.TravelMedicineAdvisory.Server.core.types.SuccessResponse;
import com.TravelMedicineAdvisory.Server.security.AppUserDetails;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Draft plans")
@RestController
@RequestMapping("/api/v1/draft-plans")
public class DraftPlanController {

    private final DraftPlanService service;

    public DraftPlanController(DraftPlanService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("@perm.has(authentication, 'travel_plan:list', 'travel_plan:read')")
    public ResponseEntity<SuccessResponse> getAll(@AuthenticationPrincipal AppUserDetails user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<DraftPlanResponse> drafts = service.findByUserId(user.getUserId());
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", drafts));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@perm.has(authentication, 'travel_plan:read')")
    public ResponseEntity<SuccessResponse> getById(@PathVariable Long id,
            @AuthenticationPrincipal AppUserDetails user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", service.findById(id, user.getUserId())));
    }

    @PostMapping
    @PreAuthorize("@perm.has(authentication, 'travel_plan:create')")
    public ResponseEntity<SuccessResponse> create(@RequestBody SaveDraftPlanRequest request,
            @AuthenticationPrincipal AppUserDetails user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SuccessResponse("Draft saved", service.create(user.getUserId(), request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@perm.has(authentication, 'travel_plan:update')")
    public ResponseEntity<SuccessResponse> update(@PathVariable Long id,
            @RequestBody SaveDraftPlanRequest request,
            @AuthenticationPrincipal AppUserDetails user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(new SuccessResponse("Draft updated", service.update(id, user.getUserId(), request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@perm.has(authentication, 'travel_plan:delete')")
    public ResponseEntity<SuccessResponse> delete(@PathVariable Long id,
            @AuthenticationPrincipal AppUserDetails user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        service.delete(id, user.getUserId());
        return ResponseEntity.ok(new SuccessResponse("Draft deleted", null));
    }
}
