package com.TravelMedicineAdvisory.Server.domain.ebook;

import com.TravelMedicineAdvisory.Server.core.storage.StorageService;
import com.TravelMedicineAdvisory.Server.core.types.SuccessResponse;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;
import com.TravelMedicineAdvisory.Server.security.AppUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/v1")
public class EbookController {

    private final EbookService ebookService;
    private final UserRepository userRepository;
    private final StorageService storageService;

    public EbookController(EbookService ebookService, UserRepository userRepository, StorageService storageService) {
        this.ebookService = ebookService;
        this.userRepository = userRepository;
        this.storageService = storageService;
    }

    // ─── Public: Shop ───────────────────────────────────────────────────────

    @GetMapping("/ebooks")
    public ResponseEntity<SuccessResponse> listEbooks() {
        return ResponseEntity.ok(new SuccessResponse("Ebooks retrieved", ebookService.listActiveEbooks()));
    }

    @GetMapping("/ebooks/{slug}")
    public ResponseEntity<SuccessResponse> getEbook(@PathVariable String slug) {
        try {
            return ResponseEntity.ok(new SuccessResponse("Ebook retrieved", ebookService.getEbookBySlug(slug)));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/ebooks/checkout")
    public ResponseEntity<SuccessResponse> checkout(
            @RequestBody EbookDto.CheckoutRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = resolveUserId(userDetails);
        try {
            EbookDto.CheckoutResponse response = ebookService.initiateCheckout(userId, request);
            return ResponseEntity.ok(new SuccessResponse("Checkout initiated", response));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(new SuccessResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/ebooks/orders/{txRef}")
    public ResponseEntity<SuccessResponse> getOrderStatus(@PathVariable String txRef) {
        try {
            return ResponseEntity.ok(new SuccessResponse("Order retrieved", ebookService.getOrderByTxRef(txRef)));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/ebooks/orders/verify")
    public ResponseEntity<SuccessResponse> verifyOrder(@RequestBody EbookDto.VerifyOrderRequest request) {
        try {
            EbookDto.EbookOrderResponse response = ebookService.verifyOrder(request.txRef(), request.transactionId());
            return ResponseEntity.ok(new SuccessResponse("Order verified", response));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ─── Authenticated: My ebooks ────────────────────────────────────────────

    @GetMapping("/ebooks/my-orders")
    public ResponseEntity<SuccessResponse> myOrders(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = requireUserId(userDetails);
        return ResponseEntity.ok(new SuccessResponse("Your ebooks", ebookService.getMyOrders(userId)));
    }

    // ─── Admin ────────────────────────────────────────────────────────────────

    @GetMapping("/admin/ebooks")
    public ResponseEntity<SuccessResponse> adminListEbooks() {
        return ResponseEntity.ok(new SuccessResponse("Ebooks", ebookService.listAllForAdmin()));
    }

    @PostMapping("/admin/ebooks")
    public ResponseEntity<SuccessResponse> adminCreateEbook(@RequestBody EbookDto.CreateEbookRequest request) {
        try {
            return ResponseEntity.ok(new SuccessResponse("Ebook created", ebookService.createEbook(request)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new SuccessResponse(e.getMessage(), null));
        }
    }

    @PutMapping("/admin/ebooks/{id}")
    public ResponseEntity<SuccessResponse> adminUpdateEbook(
            @PathVariable Long id, @RequestBody EbookDto.UpdateEbookRequest request) {
        try {
            return ResponseEntity.ok(new SuccessResponse("Ebook updated", ebookService.updateEbook(id, request)));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/admin/ebooks/{id}")
    public ResponseEntity<SuccessResponse> adminDeleteEbook(@PathVariable Long id) {
        try {
            ebookService.deleteEbook(id);
            return ResponseEntity.ok(new SuccessResponse("Ebook deleted", null));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/admin/ebooks/{id}/versions")
    public ResponseEntity<SuccessResponse> adminAddVersion(
            @PathVariable Long id, @RequestBody EbookDto.CreateVersionRequest request) {
        try {
            return ResponseEntity.ok(new SuccessResponse("Version added", ebookService.addVersion(id, request)));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/admin/ebooks/{id}/versions/{versionId}")
    public ResponseEntity<SuccessResponse> adminUpdateVersion(
            @PathVariable Long id, @PathVariable Long versionId,
            @RequestBody EbookDto.UpdateVersionRequest request) {
        try {
            return ResponseEntity.ok(new SuccessResponse("Version updated",
                    ebookService.updateVersion(id, versionId, request)));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/admin/ebooks/{id}/versions/{versionId}")
    public ResponseEntity<SuccessResponse> adminDeleteVersion(
            @PathVariable Long id, @PathVariable Long versionId) {
        try {
            ebookService.deleteVersion(id, versionId);
            return ResponseEntity.ok(new SuccessResponse("Version deleted", null));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/admin/ebooks/orders")
    public ResponseEntity<SuccessResponse> adminListOrders() {
        return ResponseEntity.ok(new SuccessResponse("Orders", ebookService.listAllOrders()));
    }

    @GetMapping("/admin/ebooks/{id}/orders")
    public ResponseEntity<SuccessResponse> adminListOrdersByEbook(@PathVariable Long id) {
        return ResponseEntity.ok(new SuccessResponse("Orders", ebookService.listOrdersByEbook(id)));
    }

    @GetMapping("/admin/ebooks/stats")
    public ResponseEntity<SuccessResponse> adminStats() {
        return ResponseEntity.ok(new SuccessResponse("Stats", ebookService.getStats()));
    }

    @PostMapping(value = "/admin/ebooks/upload-pdf", consumes = "multipart/form-data")
    public ResponseEntity<SuccessResponse> uploadPdf(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(new SuccessResponse("File is required", null));
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("application/pdf")) {
            return ResponseEntity.badRequest().body(new SuccessResponse("Only PDF files are accepted", null));
        }
        var attachment = storageService.store(file, "ebooks", null, "EbookVersion");
        String fileUrl = storageService.getUrl(attachment.getStoragePath());
        String fileKey = attachment.getStoragePath();
        BigDecimal fileSizeMb = BigDecimal.valueOf(file.getSize())
                .divide(BigDecimal.valueOf(1024 * 1024), 1, RoundingMode.HALF_UP);
        return ResponseEntity.ok(new SuccessResponse("File uploaded", Map.of(
                "fileUrl", fileUrl,
                "fileKey", fileKey,
                "fileSizeMb", fileSizeMb,
                "fileName", file.getOriginalFilename() != null ? file.getOriginalFilename() : "ebook.pdf"
        )));
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private Long resolveUserId(UserDetails userDetails) {
        if (userDetails == null) return null;
        if (userDetails instanceof AppUserDetails appUserDetails) {
            return appUserDetails.getUserId();
        }
        return userRepository.findByEmail(userDetails.getUsername())
                .map(u -> u.getId()).orElse(null);
    }

    private Long requireUserId(UserDetails userDetails) {
        Long id = resolveUserId(userDetails);
        if (id == null) throw new IllegalStateException("Authentication required");
        return id;
    }
}
