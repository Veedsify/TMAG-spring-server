package com.TravelMedicineAdvisory.Server.domain.familytrip;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.TravelMedicineAdvisory.Server.domain.familytrip.dto.*;
import com.TravelMedicineAdvisory.Server.domain.plans.PlanGenerationService;
import com.TravelMedicineAdvisory.Server.domain.travelplan.TravelPlan;
import com.TravelMedicineAdvisory.Server.domain.travelplan.TravelPlanResponse;
import com.TravelMedicineAdvisory.Server.domain.travelplan.TravelPlanService;
import com.TravelMedicineAdvisory.Server.domain.travelplanquestionnaire.TravelPlanQuestionnaire;
import com.TravelMedicineAdvisory.Server.domain.travelplanquestionnaire.TravelPlanQuestionnaireRepository;
import com.TravelMedicineAdvisory.Server.domain.user.User;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Transactional
public class FamilyTripService {

    private final FamilyTripRepository familyTripRepository;
    private final FamilyTripMemberRepository memberRepository;
    private final FamilyPackagePurchaseRepository purchaseRepository;
    private final UserRepository userRepository;
    private final TravelPlanService travelPlanService;
    private final TravelPlanQuestionnaireRepository questionnaireRepository;
    private final PlanGenerationService planGenerationService;
    private final FamilyMemberAuthService authService;
    private final ObjectMapper objectMapper;

    public FamilyTripService(FamilyTripRepository familyTripRepository,
                             FamilyTripMemberRepository memberRepository,
                             FamilyPackagePurchaseRepository purchaseRepository,
                             UserRepository userRepository,
                             TravelPlanService travelPlanService,
                             TravelPlanQuestionnaireRepository questionnaireRepository,
                             PlanGenerationService planGenerationService,
                             FamilyMemberAuthService authService,
                             ObjectMapper objectMapper) {
        this.familyTripRepository = familyTripRepository;
        this.memberRepository = memberRepository;
        this.purchaseRepository = purchaseRepository;
        this.userRepository = userRepository;
        this.travelPlanService = travelPlanService;
        this.questionnaireRepository = questionnaireRepository;
        this.planGenerationService = planGenerationService;
        this.authService = authService;
        this.objectMapper = objectMapper;
    }

    public FamilyTripPreviewResponse preview(FamilyTripRequest request, Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        LocalDate departureDate = extractDepartureDate(request.tripDetailsJson());
        
        PricingResult pricing = calculatePricing(request.members(), departureDate, user.getBillingCurrency());
        
        ActivePackageAllowanceDto allowance = getActiveAllowance(userId);
        
        boolean paymentRequired = false;
        List<PaymentBreakdownItem> breakdown = new ArrayList<>();
        
        if (allowance != null && allowance.tripsRemaining() > 0) {
            breakdown.add(new PaymentBreakdownItem("Family base package", 0L, "ALLOWANCE"));
        } else {
            paymentRequired = true;
            breakdown.add(new PaymentBreakdownItem("Family base package", pricing.baseFiatCost, "PAYMENT_REQUIRED"));
        }
        
        if (pricing.extraFiatCost > 0) {
            paymentRequired = true;
            breakdown.add(new PaymentBreakdownItem(pricing.additionalMembers + " additional traveller(s)", pricing.extraFiatCost, "PAYMENT_REQUIRED"));
        }
        
        return new FamilyTripPreviewResponse(
            pricing.includedMembers,
            pricing.additionalMembers,
            pricing.baseFiatCost,
            pricing.extraFiatCost,
            allowance != null && allowance.tripsRemaining() > 0 ? pricing.extraFiatCost : pricing.totalFiatCost,
            pricing.currency,
            user.getCredits(),
            allowance,
            paymentRequired,
            breakdown
        );
    }

    public FamilyTripResponse saveDraft(FamilyTripRequest request, Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        
        // Check if there's a draft already, if we wanted to enforce 1 draft.
        // For simplicity, create a new draft or update based on request if we had an ID.
        // The spec says POST /api/v1/family-trips/drafts
        
        FamilyTrip trip = new FamilyTrip();
        trip.setUser(user);
        trip.setStatus(FamilyTripStatus.DRAFT);
        trip.setDestination(request.destination());
        trip.setCountry(request.country());
        trip.setDuration(request.duration() != null ? request.duration() : 1);
        trip.setPurpose(request.purpose());
        trip.setTripType(request.tripType());
        trip.setTripDetailsJson(request.tripDetailsJson());
        
        LocalDate departureDate = extractDepartureDate(request.tripDetailsJson());
        PricingResult pricing = calculatePricing(request.members(), departureDate, user.getBillingCurrency());
        trip.setBaseFiatCost(pricing.baseFiatCost);
        trip.setExtraFiatCost(pricing.extraFiatCost);
        trip.setTotalFiatCost(pricing.totalFiatCost);
        trip.setExtraMemberCount(pricing.additionalMembers);
        trip.setCurrency(pricing.currency);
        
        familyTripRepository.save(trip);
        
        int order = 0;
        for (FamilyTripMemberRequest mReq : request.members()) {
            FamilyTripMember member = new FamilyTripMember();
            member.setFamilyTrip(trip);
            member.setRelationship(FamilyMemberRelationship.valueOf(mReq.relationship()));
            member.setFirstName(mReq.firstName());
            member.setLastName(mReq.lastName());
            member.setMemberEmail(mReq.memberEmail());
            member.setDateOfBirth(mReq.dateOfBirth());
            
            if (mReq.dateOfBirth() != null && departureDate != null) {
                member.setAgeAtDeparture((int) ChronoUnit.YEARS.between(mReq.dateOfBirth(), departureDate));
            }
            
            member.setQuestionnaireResponsesJson(mReq.questionnaireResponses());
            member.setQuestionnaireStatus(mReq.questionnaireResponses() != null && !mReq.questionnaireResponses().isEmpty() ? "COMPLETE" : "PENDING");
            member.setSortOrder(order++);
            
            // Re-eval if included in base here if needed, or rely on submission time logic.
            
            memberRepository.save(member);
            authService.generateAndSendCode(member);
            memberRepository.save(member);
        }
        
        return toResponse(trip);
    }

    public FamilyTripResponse getLatestDraft(Long userId) {
        return familyTripRepository.findFirstByUserIdAndStatusOrderByCreatedAtDesc(userId, FamilyTripStatus.DRAFT)
                .map(this::toResponse)
                .orElse(null);
    }

    @Transactional
    public FamilyTripResponse updateTrip(Long tripId, FamilyTripRequest request, Long userId) {
        FamilyTrip trip = familyTripRepository.findById(tripId).orElseThrow();
        if (!trip.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        if (trip.getStatus() != FamilyTripStatus.DRAFT && trip.getStatus() != FamilyTripStatus.PAYMENT_REQUIRED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only draft trips can be updated");
        }

        trip.setDestination(request.destination());
        trip.setCountry(request.country());
        trip.setDuration(request.duration() != null ? request.duration() : 1);
        trip.setPurpose(request.purpose());
        trip.setTripType(request.tripType());
        trip.setTripDetailsJson(request.tripDetailsJson());

        LocalDate departureDate = extractDepartureDate(request.tripDetailsJson());
        PricingResult pricing = calculatePricing(request.members(), departureDate, trip.getUser().getBillingCurrency());
        trip.setBaseFiatCost(pricing.baseFiatCost);
        trip.setExtraFiatCost(pricing.extraFiatCost);
        trip.setTotalFiatCost(pricing.totalFiatCost);
        trip.setExtraMemberCount(pricing.additionalMembers);
        trip.setCurrency(pricing.currency);

        familyTripRepository.save(trip);

        memberRepository.findByFamilyTripIdAndDeletedAtIsNullOrderBySortOrder(tripId)
            .forEach(m -> m.setDeletedAt(java.time.LocalDateTime.now()));
        memberRepository.flush();

        int order = 0;
        for (FamilyTripMemberRequest mReq : request.members()) {
            FamilyTripMember member = new FamilyTripMember();
            member.setFamilyTrip(trip);
            member.setRelationship(FamilyMemberRelationship.valueOf(mReq.relationship()));
            member.setFirstName(mReq.firstName());
            member.setLastName(mReq.lastName());
            member.setMemberEmail(mReq.memberEmail());
            member.setDateOfBirth(mReq.dateOfBirth());

            if (mReq.dateOfBirth() != null && departureDate != null) {
                member.setAgeAtDeparture((int) java.time.temporal.ChronoUnit.YEARS.between(mReq.dateOfBirth(), departureDate));
            }

            member.setQuestionnaireResponsesJson(mReq.questionnaireResponses());
            member.setQuestionnaireStatus(mReq.questionnaireResponses() != null && !mReq.questionnaireResponses().isEmpty() ? "COMPLETE" : "PENDING");
            member.setSortOrder(order++);

            memberRepository.save(member);
        }

        return toResponse(trip);
    }

    @Transactional
    public String regenerateMemberCode(Long tripId, Long memberId, Long userId) {
        FamilyTrip trip = familyTripRepository.findById(tripId).orElseThrow();
        if (!trip.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        FamilyTripMember member = memberRepository.findById(memberId).orElseThrow();
        if (!member.getFamilyTrip().getId().equals(tripId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Member does not belong to this trip");
        }

        member.setSessionTokenHash(null);
        member.setSessionExpiresAt(null);
        member.setFailedLoginAttempts(0);
        member.setLockedUntil(null);

        authService.generateAndSendCode(member);

        return member.getLoginCode();
    }

    @Transactional(readOnly = true)
    public List<com.TravelMedicineAdvisory.Server.domain.travelplan.TravelPlanResponse> getTripPlans(Long tripId, Long userId) {
        FamilyTrip trip = familyTripRepository.findById(tripId).orElseThrow();
        if (!trip.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        List<FamilyTripMember> members = memberRepository.findByFamilyTripIdAndDeletedAtIsNullOrderBySortOrder(tripId);
        return members.stream()
            .filter(m -> m.getTravelPlan() != null)
            .map(m -> {
                try {
                    return travelPlanService.findByIdInternal(m.getTravelPlan().getId());
                } catch (Exception e) {
                    return null;
                }
            })
            .filter(p -> p != null)
            .toList();
    }

    @Transactional
    public FamilyTripResponse submit(Long tripId, Long userId) {
        FamilyTrip trip = familyTripRepository.findById(tripId).orElseThrow();
        if (!trip.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        if (trip.getStatus() != FamilyTripStatus.DRAFT && trip.getStatus() != FamilyTripStatus.PAYMENT_REQUIRED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Trip is not in a submittable state");
        }
        
        List<FamilyTripMember> members = memberRepository.findByFamilyTripIdAndDeletedAtIsNullOrderBySortOrder(tripId);
        
        for (FamilyTripMember m : members) {
            if (!"COMPLETE".equals(m.getQuestionnaireStatus()) && (m.getQuestionnaireResponsesJson() == null || m.getQuestionnaireResponsesJson().isEmpty())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Member " + m.getFirstName() + " has incomplete questionnaire");
            }
        }
        
        User user = trip.getUser();
        LocalDate departureDate = extractDepartureDate(trip.getTripDetailsJson());
        
        long spouseCount = members.stream().filter(m -> m.getRelationship() == FamilyMemberRelationship.SPOUSE).count();
        if (spouseCount > 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only 1 spouse allowed");
        }
        
        // Determine included vs extra fiat costs
        long extraFiatCost = trip.getExtraFiatCost();
        
        // Handle allowance
        FamilyPackagePurchase allowance = findActiveAllowance(userId);
        if (allowance == null) {
            throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED, "No active family package allowance. Please purchase a Family Package.");
        }
        
        if (extraFiatCost > 0) {
            // Note: Since this requires fiat payment, the system should redirect to a checkout page 
            // if there's an extra cost. For now, we block submission if there's an unpaid fiat amount.
            throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED, "Additional payment required for extra members. Please complete checkout.");
        }
        
        allowance.setTripsUsed(allowance.getTripsUsed() + 1);
        if (allowance.getTripsUsed() >= allowance.getTripsAllowed()) {
            allowance.setStatus(FamilyPackagePurchaseStatus.EXHAUSTED);
        }
        purchaseRepository.save(allowance);
        
        trip.setFamilyPackagePurchase(allowance);
        trip.setStatus(FamilyTripStatus.QUEUED);
        trip.setSubmittedAt(LocalDateTime.now());
        familyTripRepository.save(trip);
        
        for (FamilyTripMember member : members) {
            TravelPlan plan = travelPlanService.createForFamilyMember(member, trip);
            member.setTravelPlan(plan);
            
            TravelPlanQuestionnaire questionnaire = new TravelPlanQuestionnaire();
            questionnaire.setTravelPlan(plan);
            questionnaire.setUser(user);
            questionnaire.setSource("family-plan");
            questionnaire.setResponsesJson(member.getQuestionnaireResponsesJson());
            questionnaireRepository.save(questionnaire);
            
            authService.generateAndSendCode(member);
            memberRepository.save(member);
            
            planGenerationService.enqueueGeneration(plan.getId(), user.getId());
        }
        
        return toResponse(trip);
    }
    
    public FamilyTripResponse getById(Long id, Long userId) {
        FamilyTrip trip = familyTripRepository.findById(id).orElseThrow();
        if (!trip.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return toResponse(trip);
    }
    
    public void delete(Long id, Long userId) {
        FamilyTrip trip = familyTripRepository.findById(id).orElseThrow();
        if (!trip.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        familyTripRepository.delete(trip);
    }

    private PricingResult calculatePricing(List<FamilyTripMemberRequest> members, LocalDate departureDate, com.TravelMedicineAdvisory.Server.domain.company.BillingCurrency billingCurrency) {
        if (members == null) members = new ArrayList<>();

        int memberCount = members.size();
        int includedMembers = Math.min(6, memberCount);
        int additionalMembers = Math.max(0, memberCount - 6);
        
        long baseFiatCost = 0L;
        long extraFiatCost = 0L;
        String currency = billingCurrency != null ? billingCurrency.name() : "NGN";
        
        if ("USD".equalsIgnoreCase(currency)) {
            baseFiatCost = 180 * 100L; // $180
            extraFiatCost = additionalMembers * (30 * 100L); // $30 per extra
        } else {
            baseFiatCost = 180000 * 100L; // NGN 180,000
            extraFiatCost = additionalMembers * (25000 * 100L); // NGN 25,000 per extra
        }
        
        long totalFiatCost = baseFiatCost + extraFiatCost;
        
        return new PricingResult(includedMembers, additionalMembers, baseFiatCost, extraFiatCost, totalFiatCost, currency);
    }

    private FamilyPackagePurchase findActiveAllowance(Long userId) {
        // simplified query logic
        return purchaseRepository.findAll().stream()
            .filter(p -> p.getUser().getId().equals(userId) && p.getStatus() == FamilyPackagePurchaseStatus.ACTIVE)
            .findFirst().orElse(null);
    }
    
    private ActivePackageAllowanceDto getActiveAllowance(Long userId) {
        FamilyPackagePurchase p = findActiveAllowance(userId);
        if (p == null) return null;
        return new ActivePackageAllowanceDto(p.getPackageType().name(), p.getTripsAllowed() - p.getTripsUsed());
    }

    private LocalDate extractDepartureDate(String tripDetailsJson) {
        if (tripDetailsJson == null || tripDetailsJson.isEmpty()) return null;
        try {
            JsonNode root = objectMapper.readTree(tripDetailsJson);
            if (root.has("departureDate")) {
                return LocalDate.parse(root.get("departureDate").asText());
            }
        } catch (Exception e) {}
        return null;
    }
    
    private FamilyTripResponse toResponse(FamilyTrip trip) {
        List<FamilyTripMemberResponse> memberResponses = memberRepository.findByFamilyTripIdAndDeletedAtIsNullOrderBySortOrder(trip.getId())
            .stream().map(m -> new FamilyTripMemberResponse(
                m.getId(),
                m.getRelationship().name(),
                m.getFirstName(),
                m.getLastName(),
                m.getMemberEmail(),
                m.getDateOfBirth(),
                m.getAgeAtDeparture(),
                m.getIncludedInBase(),
                m.getQuestionnaireStatus(),
                m.getTravelPlan() != null ? m.getTravelPlan().getId() : null,
                m.getLoginCode()
            )).collect(Collectors.toList());
            
        return new FamilyTripResponse(
            trip.getId(),
            trip.getStatus().name(),
            trip.getDestination(),
            trip.getCountry(),
            trip.getDuration(),
            trip.getPurpose(),
            trip.getTripType(),
            trip.getTripDetailsJson(),
            trip.getBaseFiatCost(),
            trip.getExtraMemberCount(),
            trip.getTotalFiatCost(),
            trip.getCurrency(),
            memberResponses
        );
    }

    private static class PricingResult {
        int includedMembers;
        int additionalMembers;
        long baseFiatCost;
        long extraFiatCost;
        long totalFiatCost;
        String currency;

        PricingResult(int inc, int add, long base, long extra, long total, String cur) {
            this.includedMembers = inc;
            this.additionalMembers = add;
            this.baseFiatCost = base;
            this.extraFiatCost = extra;
            this.totalFiatCost = total;
            this.currency = cur;
        }
    }
}
