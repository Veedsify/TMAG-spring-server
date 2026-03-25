package com.TravelMedicineAdvisory.Server.domain.admin.auth;

import com.TravelMedicineAdvisory.Server.core.types.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/auth")
public class AdminAuthController {

    private final AdminAuthService service;

    public AdminAuthController(AdminAuthService service) {
        this.service = service;
    }

    @PostMapping("/login")
    public ResponseEntity<SuccessResponse> login(@RequestBody Map<String, String> credentials) {
        return ResponseEntity.ok(new SuccessResponse("Login successful",
                service.login(credentials.get("email"), credentials.get("password"))));
    }

    @PostMapping("/logout")
    public ResponseEntity<SuccessResponse> logout() {
        service.logout();
        return ResponseEntity.ok(new SuccessResponse("Logout successful", null));
    }

    @GetMapping("/me")
    public ResponseEntity<SuccessResponse> getCurrentUser() {
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", service.getCurrentUser()));
    }
}
