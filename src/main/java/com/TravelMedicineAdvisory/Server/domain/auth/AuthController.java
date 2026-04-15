package com.TravelMedicineAdvisory.Server.domain.auth;

import io.swagger.v3.oas.annotations.tags.Tag;

import com.TravelMedicineAdvisory.Server.core.types.SuccessResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Auth")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody RegisterRequest request) {
        AuthResponse data = authService.register(request);
        return ResponseEntity.ok(Map.of("success", true, "data", data));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request) {
        AuthResponse data = authService.login(request);
        return ResponseEntity.ok(Map.of("success", true, "data", data));
    }

    @PostMapping("/logout")
    public ResponseEntity<SuccessResponse> logout() {
        return ResponseEntity.ok(new SuccessResponse("Logout successful", true, null));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<SuccessResponse> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(new SuccessResponse("Password reset email sent", true, null));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<SuccessResponse> resetPassword(@RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.getEmail(), request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(new SuccessResponse("Password reset successful", true, null));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<SuccessResponse> resendVerification(@RequestBody ResendVerificationRequest request) {
        authService.resendVerification(request.getEmail());
        return ResponseEntity.ok(new SuccessResponse("Verification email sent", true, null));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<SuccessResponse> verifyEmail(@RequestBody VerifyEmailRequest request) {
        authService.verifyEmail(request.getEmail(), request.getCode());
        return ResponseEntity.ok(new SuccessResponse("Email verified successfully", true, null));
    }

    @PostMapping("/accept-invitation")
    public ResponseEntity<Map<String, Object>> acceptInvitation(@RequestBody AcceptInvitationRequest request) {
        AuthResponse data = authService.acceptInvitation(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(Map.of("success", true, "data", data));
    }

    @GetMapping("/google/url")
    public ResponseEntity<Map<String, Object>> googleAuthUrl() {
        String url = authService.googleAuthUrl();
        return ResponseEntity.ok(Map.of("success", true, "data", Map.of("url", url)));
    }

    @PostMapping("/google/callback")
    public ResponseEntity<Map<String, Object>> googleCallback(@RequestBody GoogleCallbackRequest request) {
        AuthResponse data = authService.googleCallback(request.getCode(), request.getPlanCode());
        return ResponseEntity.ok(Map.of("success", true, "data", data));
    }
}
