package com.TravelMedicineAdvisory.Server.domain.familytrip;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.TravelMedicineAdvisory.Server.core.types.SuccessResponse;
import com.TravelMedicineAdvisory.Server.domain.familytrip.dto.FamilyTripRequest;
import com.TravelMedicineAdvisory.Server.domain.familytrip.dto.FamilyTripResponse;
import com.TravelMedicineAdvisory.Server.domain.travelplan.TravelPlanRepository;
import com.TravelMedicineAdvisory.Server.domain.travelplan.TravelPlanResponse;
import com.TravelMedicineAdvisory.Server.domain.travelplan.TravelPlanService;
import com.TravelMedicineAdvisory.Server.security.AppUserDetails;

@RestController
@RequestMapping("/api/v1/family-trips")
public class FamilyTripController {

    private final FamilyTripService familyTripService;
    private final FamilyMemberAuthService authService;
    private final TravelPlanService travelPlanService;
    private final TravelPlanRepository travelPlanRepository;

    public FamilyTripController(FamilyTripService familyTripService, FamilyMemberAuthService authService,
            TravelPlanService travelPlanService, TravelPlanRepository travelPlanRepository) {
        this.familyTripService = familyTripService;
        this.authService = authService;
        this.travelPlanService = travelPlanService;
        this.travelPlanRepository = travelPlanRepository;
    }

    @GetMapping
    public ResponseEntity<SuccessResponse> getUserTrips(@AuthenticationPrincipal AppUserDetails user) {
        var trips = familyTripService.getUserTrips(user.getUserId());
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", trips));
    }

    @PostMapping("/preview")
    public ResponseEntity<SuccessResponse> preview(@RequestBody FamilyTripRequest request, @AuthenticationPrincipal AppUserDetails user) {
        return ResponseEntity.ok(new SuccessResponse("Preview generated", familyTripService.preview(request, user.getUserId())));
    }

    @PostMapping("/drafts")
    public ResponseEntity<SuccessResponse> saveDraft(@RequestBody FamilyTripRequest request, @AuthenticationPrincipal AppUserDetails user) {
        return ResponseEntity.ok(new SuccessResponse("Draft saved", familyTripService.saveDraft(request, user.getUserId())));
    }

    @GetMapping("/drafts/latest")
    public ResponseEntity<SuccessResponse> getLatestDraft(@AuthenticationPrincipal AppUserDetails user) {
        FamilyTripResponse draft = familyTripService.getLatestDraft(user.getUserId());
        return ResponseEntity.ok(new SuccessResponse(draft != null ? "Latest draft" : "No draft found", draft));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse> getById(@PathVariable Long id, @AuthenticationPrincipal AppUserDetails user) {
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", familyTripService.getById(id, user.getUserId())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SuccessResponse> update(@PathVariable Long id, @RequestBody FamilyTripRequest request, @AuthenticationPrincipal AppUserDetails user) {
        return ResponseEntity.ok(new SuccessResponse("Updated successfully", familyTripService.updateTrip(id, request, user.getUserId())));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<SuccessResponse> submit(@PathVariable Long id, @AuthenticationPrincipal AppUserDetails user) {
        return ResponseEntity.ok(new SuccessResponse("Submitted successfully", familyTripService.submit(id, user.getUserId())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponse> delete(@PathVariable Long id, @AuthenticationPrincipal AppUserDetails user) {
        familyTripService.delete(id, user.getUserId());
        return ResponseEntity.ok(new SuccessResponse("Deleted successfully", null));
    }

    @GetMapping("/{id}/plans")
    public ResponseEntity<SuccessResponse> getTripPlans(@PathVariable Long id, @AuthenticationPrincipal AppUserDetails user) {
        List<TravelPlanResponse> plans = familyTripService.getTripPlans(id, user.getUserId());
        return ResponseEntity.ok(new SuccessResponse("Fetched plans", plans));
    }

    @PostMapping("/{id}/members/{memberId}/regenerate-code")
    public ResponseEntity<SuccessResponse> regenerateCode(@PathVariable Long id, @PathVariable Long memberId, @AuthenticationPrincipal AppUserDetails user) {
        String newCode = familyTripService.regenerateMemberCode(id, memberId, user.getUserId());
        return ResponseEntity.ok(new SuccessResponse("Code regenerated", Map.of("loginCode", newCode)));
    }

    // --- Member Endpoints ---

    @PostMapping("/members/login")
    public ResponseEntity<SuccessResponse> memberLogin(@RequestBody Map<String, String> request) {
        String email = request.get("mainApplicantEmail");
        String code = request.get("code");
        String token = authService.login(email, code);

        FamilyTripMember member = authService.requireMember(token);

        return ResponseEntity.ok(new SuccessResponse("Logged in", Map.of(
            "sessionToken", token,
            "expiresAt", member.getSessionExpiresAt().toString(),
            "member", Map.of(
                "id", member.getId(),
                "firstName", member.getFirstName(),
                "lastName", member.getLastName(),
                "relationship", member.getRelationship().name(),
                "familyTripId", member.getFamilyTrip().getId()
            )
        )));
    }

    @PostMapping("/members/logout")
    public ResponseEntity<SuccessResponse> memberLogout(@RequestHeader(value = "X-Family-Member-Token", required = false) String token) {
        authService.logout(token);
        return ResponseEntity.ok(new SuccessResponse("Logged out", null));
    }

    @GetMapping("/members/me")
    public ResponseEntity<SuccessResponse> memberMe(@RequestHeader("X-Family-Member-Token") String token) {
        FamilyTripMember member = authService.requireMember(token);
        return ResponseEntity.ok(new SuccessResponse("Current member", Map.of(
            "id", member.getId(),
            "firstName", member.getFirstName(),
            "lastName", member.getLastName(),
            "relationship", member.getRelationship().name(),
            "familyTripId", member.getFamilyTrip().getId()
        )));
    }

    @GetMapping("/members/plans")
    public ResponseEntity<SuccessResponse> getMemberPlans(@RequestHeader("X-Family-Member-Token") String token) {
        FamilyTripMember member = authService.requireMember(token);
        List<Long> planIds = travelPlanRepository
            .findByFamilyTripMemberIdAndDeletedAtIsNullOrderByCreatedAtDesc(member.getId())
            .stream()
            .map(p -> p.getId())
            .toList();
        return ResponseEntity.ok(new SuccessResponse("Fetched plans", planIds));
    }

    @GetMapping("/members/plans/{planId}")
    public ResponseEntity<SuccessResponse> getMemberPlanDetail(@PathVariable Long planId, @RequestHeader("X-Family-Member-Token") String token) {
        FamilyTripMember member = authService.requireMember(token);
        travelPlanRepository.findByIdAndFamilyTripMemberIdAndDeletedAtIsNull(planId, member.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Plan not found or access denied"));

        TravelPlanResponse plan = travelPlanService.findByIdInternal(planId);
        return ResponseEntity.ok(new SuccessResponse("Fetched plan", plan));
    }
}
