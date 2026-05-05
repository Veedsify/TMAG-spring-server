package com.TravelMedicineAdvisory.Server.domain.usersetting;

import com.TravelMedicineAdvisory.Server.core.types.SuccessResponse;
import com.TravelMedicineAdvisory.Server.security.AppUserDetails;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/settings")
public class UserSettingController {

    private final UserSettingService userSettingService;

    public UserSettingController(UserSettingService userSettingService) {
        this.userSettingService = userSettingService;
    }

    @GetMapping
    @PreAuthorize("@perm.has(authentication, 'profile:read')")
    public ResponseEntity<SuccessResponse> getSettings(@AuthenticationPrincipal AppUserDetails user) {
        UserSetting settings = userSettingService.getOrCreateByUserId(user.getUserId());
        return ResponseEntity.ok(new SuccessResponse("Settings fetched", UserSettingResponse.from(settings)));
    }

    @PostMapping("/questionnaire-consent")
    @PreAuthorize("@perm.has(authentication, 'profile:update')")
    public ResponseEntity<SuccessResponse> acceptConsent(
            @AuthenticationPrincipal AppUserDetails user,
            HttpServletRequest request) {
        String ip = getClientIp(request);
        UserSetting settings = userSettingService.recordConsent(user.getUserId(), ip);
        return ResponseEntity.ok(new SuccessResponse("Consent recorded", UserSettingResponse.from(settings)));
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
