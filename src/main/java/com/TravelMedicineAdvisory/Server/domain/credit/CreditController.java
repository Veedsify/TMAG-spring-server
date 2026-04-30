package com.TravelMedicineAdvisory.Server.domain.credit;

import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.TravelMedicineAdvisory.Server.core.types.PaginatedResponse;
import com.TravelMedicineAdvisory.Server.core.types.Pagination;
import com.TravelMedicineAdvisory.Server.core.types.SuccessResponse;
import com.TravelMedicineAdvisory.Server.domain.user.User;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;

@Tag(name = "Credits")
@RestController
@RequestMapping("/api/v1/credits")
public class CreditController {

    private final CreditService service;
    private final UserRepository userRepository;

    public CreditController(CreditService service, UserRepository userRepository) {
        this.service = service;
        this.userRepository = userRepository;
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping
    @PreAuthorize("@perm.has(authentication, 'credit:list', 'credit:read')")
    public ResponseEntity<SuccessResponse> getAll(Integer companyId, Pageable pageable) {
        User currentUser = getCurrentUser();
        Page<CreditResponse> page = service.findAllByUser(currentUser.getId(), companyId != null ? companyId.longValue() : null, pageable);
        Pagination pagination = new Pagination(
                (int) page.getTotalElements(),
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalPages()
        );
        PaginatedResponse<java.util.List<CreditResponse>> paginatedResponse = new PaginatedResponse<>(page.getContent(), pagination);
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", paginatedResponse));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@perm.has(authentication, 'credit:read')")
    public ResponseEntity<SuccessResponse> getById(@PathVariable Long id) {
        CreditResponse credit = service.findById(id);
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", credit));
    }
}
