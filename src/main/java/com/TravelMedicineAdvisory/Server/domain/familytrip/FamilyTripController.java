package com.TravelMedicineAdvisory.Server.domain.familytrip;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.TravelMedicineAdvisory.Server.domain.plans.GeneratedPlan;
import com.TravelMedicineAdvisory.Server.domain.plans.GeneratedPlanRepository;
import com.TravelMedicineAdvisory.Server.domain.travelplan.TravelPlanPdfGenerator;

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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;

import com.TravelMedicineAdvisory.Server.core.types.PaginatedResponse;
import com.TravelMedicineAdvisory.Server.core.types.Pagination;
import com.TravelMedicineAdvisory.Server.core.types.SuccessResponse;
import com.TravelMedicineAdvisory.Server.domain.familytrip.dto.FamilyTripRequest;
import com.TravelMedicineAdvisory.Server.domain.familytrip.dto.FamilyTripResponse;
import com.TravelMedicineAdvisory.Server.domain.travelplan.TravelPlanPdfExport;
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
    private final GeneratedPlanRepository generatedPlanRepository;
    private final TravelPlanPdfGenerator pdfGenerator;
    private final ObjectMapper objectMapper;

    public FamilyTripController(FamilyTripService familyTripService, FamilyMemberAuthService authService,
            TravelPlanService travelPlanService, TravelPlanRepository travelPlanRepository,
            GeneratedPlanRepository generatedPlanRepository, TravelPlanPdfGenerator pdfGenerator,
            ObjectMapper objectMapper) {
        this.familyTripService = familyTripService;
        this.authService = authService;
        this.travelPlanService = travelPlanService;
        this.travelPlanRepository = travelPlanRepository;
        this.generatedPlanRepository = generatedPlanRepository;
        this.pdfGenerator = pdfGenerator;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public ResponseEntity<SuccessResponse> getUserTrips(
            @AuthenticationPrincipal AppUserDetails user,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<FamilyTripResponse> page = familyTripService.getUserTrips(user.getUserId(), pageable);
        Pagination pagination = new Pagination(
                (int) page.getTotalElements(),
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalPages());
        PaginatedResponse<List<FamilyTripResponse>> paginatedResponse = new PaginatedResponse<>(page.getContent(), pagination);
        return ResponseEntity.ok(new SuccessResponse("Fetched successfully", paginatedResponse));
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

    @GetMapping("/{id}/plan")
    public ResponseEntity<SuccessResponse> getFamilyPlan(@PathVariable Long id, @AuthenticationPrincipal AppUserDetails user) {
        var trip = familyTripService.getById(id, user.getUserId());
        if (trip.familyPlanId() == null) {
            return ResponseEntity.ok(new SuccessResponse("No plan yet", null));
        }
        var plan = travelPlanService.findByIdInternal(trip.familyPlanId());
        return ResponseEntity.ok(new SuccessResponse("Fetched plan", plan));
    }

    @GetMapping("/{id}/summary-pdf")
    public ResponseEntity<Void> getFamilySummaryPdf(@PathVariable Long id, @AuthenticationPrincipal AppUserDetails user) {
        var trip = familyTripService.getById(id, user.getUserId());
        if (trip.familyPlanId() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No family plan generated yet");
        }
        String url = travelPlanService.exportSummaryPdfUrlForUser(trip.familyPlanId(), user.getUserId());
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, url)
                .build();
    }

    @GetMapping(value = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadFamilyPdf(@PathVariable Long id, @AuthenticationPrincipal AppUserDetails user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        var trip = familyTripService.getById(id, user.getUserId());
        if (trip.familyPlanId() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No family plan generated yet");
        }
        TravelPlanPdfExport exp = travelPlanService.exportPdfForUser(trip.familyPlanId(), user.getUserId());
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename("family-" + exp.filenameBase() + "-travel-health.pdf", StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(MediaType.APPLICATION_PDF)
                .body(exp.pdfBytes());
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

    @Deprecated
    @GetMapping("/members/plans/{planId}")
    public ResponseEntity<SuccessResponse> getMemberPlanDetail(@PathVariable Long planId, @RequestHeader("X-Family-Member-Token") String token) {
        FamilyTripMember member = authService.requireMember(token);
        travelPlanRepository.findByIdAndFamilyTripMemberIdAndDeletedAtIsNull(planId, member.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Plan not found or access denied"));

        TravelPlanResponse plan = travelPlanService.findByIdInternal(planId);
        return ResponseEntity.ok(new SuccessResponse("Fetched plan", plan));
    }

    @GetMapping("/members/my-plan")
    public ResponseEntity<SuccessResponse> getMyMemberSection(@RequestHeader("X-Family-Member-Token") String token) {
        FamilyTripMember member = authService.requireMember(token);
        com.TravelMedicineAdvisory.Server.domain.familytrip.FamilyTrip trip = member.getFamilyTrip();

        // Try new aggregate plan first
        if (trip.getTravelPlan() != null) {
            Long planId = trip.getTravelPlan().getId();
            com.TravelMedicineAdvisory.Server.domain.travelplan.TravelPlan plan = travelPlanRepository.findById(planId).orElse(null);
            if (plan != null) {
                GeneratedPlan generated = generatedPlanRepository.findByTravelPlanId(planId).orElse(null);
                if (generated != null && generated.getPlanJson() != null) {
                    try {
                        JsonNode root = objectMapper.readTree(generated.getPlanJson());
                        JsonNode membersNode = root.path("members");
                        if (membersNode.isArray()) {
                            for (JsonNode memberNode : membersNode) {
                                if (memberNode.path("memberId").asLong() == member.getId()) {
                                    return ResponseEntity.ok(new SuccessResponse("Member section", Map.of(
                                        "destination", plan.getDestination(),
                                        "country", plan.getCountry(),
                                        "status", plan.getStatus(),
                                        "memberSection", memberNode,
                                        "tripSummary", root.path("tripSummary").asText(""),
                                        "generalVaccinations", root.path("generalVaccinations")
                                    )));
                                }
                            }
                        }
                    } catch (Exception e) {
                        // fall through
                    }
                }
            }
        }

        // Legacy: fall back to member's own plan
        if (member.getTravelPlan() != null) {
            var plan = travelPlanService.findByIdInternal(member.getTravelPlan().getId());
            return ResponseEntity.ok(new SuccessResponse("Member plan (legacy)", plan));
        }

        return ResponseEntity.ok(new SuccessResponse("Plan not yet available", null));
    }

    @GetMapping(value = "/members/my-pdf", produces = "application/pdf")
    public ResponseEntity<byte[]> downloadMyMemberPdf(@RequestHeader("X-Family-Member-Token") String token) {
        FamilyTripMember member = authService.requireMember(token);
        com.TravelMedicineAdvisory.Server.domain.familytrip.FamilyTrip trip = member.getFamilyTrip();

        Long planId = trip.getTravelPlan() != null ? trip.getTravelPlan().getId() : (member.getTravelPlan() != null ? member.getTravelPlan().getId() : null);
        if (planId == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No plan available");
        }
        com.TravelMedicineAdvisory.Server.domain.travelplan.TravelPlan plan = travelPlanRepository.findById(planId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plan not found"));
        GeneratedPlan generated = generatedPlanRepository.findByTravelPlanId(planId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Generated plan not ready"));
        byte[] pdf = pdfGenerator.generate(plan, generated);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"travel-health-plan.pdf\"")
                .body(pdf);
    }
}
