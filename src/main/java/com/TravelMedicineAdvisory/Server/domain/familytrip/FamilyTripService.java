package com.TravelMedicineAdvisory.Server.domain.familytrip;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.TravelMedicineAdvisory.Server.domain.familytrip.dto.ActivePackageAllowanceDto;
import com.TravelMedicineAdvisory.Server.domain.familytrip.dto.FamilyTripMemberRequest;
import com.TravelMedicineAdvisory.Server.domain.familytrip.dto.FamilyTripMemberResponse;
import com.TravelMedicineAdvisory.Server.domain.familytrip.dto.FamilyTripPreviewResponse;
import com.TravelMedicineAdvisory.Server.domain.familytrip.dto.FamilyTripRequest;
import com.TravelMedicineAdvisory.Server.domain.familytrip.dto.FamilyTripResponse;
import com.TravelMedicineAdvisory.Server.domain.familytrip.dto.PaymentBreakdownItem;
import com.TravelMedicineAdvisory.Server.domain.plans.PlanGenerationService;
import com.TravelMedicineAdvisory.Server.domain.travelplan.TravelPlan;
import com.TravelMedicineAdvisory.Server.domain.travelplan.TravelPlanService;
import com.TravelMedicineAdvisory.Server.domain.travelplanquestionnaire.TravelPlanQuestionnaire;
import com.TravelMedicineAdvisory.Server.domain.travelplanquestionnaire.TravelPlanQuestionnaireRepository;
import com.TravelMedicineAdvisory.Server.domain.user.User;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
@Transactional
public class FamilyTripService {

    private final FamilyTripRepository familyTripRepository;
    private final FamilyTripMemberRepository memberRepository;
    private final FamilyPackagePurchaseRepository purchaseRepository;
    private final UserRepository userRepository;
    private final TravelPlanService travelPlanService;
    private final TravelPlanQuestionnaireRepository questionnaireRepository;
    private final FamilyTripMemberQuestionnaireRepository memberQuestionnaireRepository;
    private final PlanGenerationService planGenerationService;
    private final FamilyMemberAuthService authService;
    private final ObjectMapper objectMapper;

    public FamilyTripService(FamilyTripRepository familyTripRepository,
                             FamilyTripMemberRepository memberRepository,
                             FamilyPackagePurchaseRepository purchaseRepository,
                             UserRepository userRepository,
                             TravelPlanService travelPlanService,
                             TravelPlanQuestionnaireRepository questionnaireRepository,
                             FamilyTripMemberQuestionnaireRepository memberQuestionnaireRepository,
                             PlanGenerationService planGenerationService,
                             FamilyMemberAuthService authService,
                             ObjectMapper objectMapper) {
        this.familyTripRepository = familyTripRepository;
        this.memberRepository = memberRepository;
        this.purchaseRepository = purchaseRepository;
        this.userRepository = userRepository;
        this.travelPlanService = travelPlanService;
        this.questionnaireRepository = questionnaireRepository;
        this.memberQuestionnaireRepository = memberQuestionnaireRepository;
        this.planGenerationService = planGenerationService;
        this.authService = authService;
        this.objectMapper = objectMapper;
    }

    public FamilyTripPreviewResponse preview(FamilyTripRequest request, Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        LocalDate departureDate = extractDepartureDate(request.tripDetailsJson());
        
        FamilyPackagePurchase activePurchase = findActiveAllowance(userId);
        int purchaseIncludedMembers = activePurchase != null ? activePurchase.getTotalMembers() : 6;
        PricingResult pricing = calculatePricing(request.members(), departureDate, user.getBillingCurrency(), purchaseIncludedMembers);
        
        ActivePackageAllowanceDto allowance = activePurchase != null
            ? new ActivePackageAllowanceDto(activePurchase.getPackageType().name(), activePurchase.getTripsAllowed() - activePurchase.getTripsUsed())
            : null;
        
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
        FamilyPackagePurchase activePurchase = findActiveAllowance(userId);
        int purchaseIncludedMembers = activePurchase != null ? activePurchase.getTotalMembers() : 6;
        PricingResult pricing = calculatePricing(request.members(), departureDate, user.getBillingCurrency(), purchaseIncludedMembers);
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
        FamilyPackagePurchase activePurchase = findActiveAllowance(userId);
        int purchaseIncludedMembers = activePurchase != null ? activePurchase.getTotalMembers() : 6;
        PricingResult pricing = calculatePricing(request.members(), departureDate, trip.getUser().getBillingCurrency(), purchaseIncludedMembers);
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

        if (trip.getTravelPlan() != null) {
            return List.of(travelPlanService.findByIdInternal(trip.getTravelPlan().getId()));
        }

        return List.of();
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

        long spouseCount = members.stream().filter(m -> m.getRelationship() == FamilyMemberRelationship.SPOUSE).count();
        if (spouseCount > 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only 1 spouse allowed");
        }

        FamilyPackagePurchase allowance = findActiveAllowance(userId);
        if (allowance == null) {
            throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED, "No active family package allowance. Please purchase a Family Package.");
        }

        int memberCount = members.size();
        int coveredByPurchase = allowance.getTotalMembers();
        int additionalMembers = Math.max(0, memberCount - coveredByPurchase);
        long extraFiatCost = 0L;
        if (additionalMembers > 0) {
            String currency = allowance.getCurrency();
            if ("USD".equalsIgnoreCase(currency)) {
                extraFiatCost = additionalMembers * (30 * 100L);
            } else {
                extraFiatCost = additionalMembers * (25000 * 100L);
            }
        }

        if (extraFiatCost > 0) {
            throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED, "Additional payment required for extra members beyond your plan. Please complete checkout.");
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

        // ONE plan for the whole family
        TravelPlan familyPlan = travelPlanService.createForFamilyTrip(trip);
        trip.setTravelPlan(familyPlan);
        familyTripRepository.save(trip);

        // Aggregate all member questionnaire data into one JSON array.
        // Include second-person relationship labels so future AI output reads as advice
        // to the main applicant, not detached third-person biographies.
        ArrayNode membersJson = buildFamilyMembersGenerationPayload(members);

        ObjectNode aggregateResponses = objectMapper.createObjectNode();
        aggregateResponses.put("type", "family");
        aggregateResponses.set("members", membersJson);

        TravelPlanQuestionnaire questionnaire = new TravelPlanQuestionnaire();
        questionnaire.setTravelPlan(familyPlan);
        questionnaire.setUser(user);
        questionnaire.setSource("family-aggregate");
        questionnaire.setResponsesJson(aggregateResponses.toString());
        questionnaireRepository.save(questionnaire);

        // Persist per-member questionnaire records
        for (FamilyTripMember m : members) {
            FamilyTripMemberQuestionnaire mq = new FamilyTripMemberQuestionnaire();
            mq.setFamilyTripMember(m);
            mq.setTravelPlan(familyPlan);
            mq.setResponsesJson(m.getQuestionnaireResponsesJson());
            mq.setSource("family-member");
            memberQuestionnaireRepository.save(mq);
        }

        // Generate login codes for all members (skip MAIN_APPLICANT)
        for (FamilyTripMember member : members) {
            if (member.getRelationship() == FamilyMemberRelationship.MAIN_APPLICANT) {
                continue;
            }
            authService.generateAndSendCode(member);
            memberRepository.save(member);
        }

        planGenerationService.enqueueGeneration(familyPlan.getId(), user.getId());

        return toResponse(trip);
    }

    private ArrayNode buildFamilyMembersGenerationPayload(List<FamilyTripMember> members) {
        ArrayNode payload = objectMapper.createArrayNode();
        for (FamilyTripMember member : members) {
            JsonNode responses = parseResponsesJson(member.getQuestionnaireResponsesJson());
            String displayLabel = familyMemberDisplayLabel(member, responses);

            ObjectNode memberNode = payload.addObject();
            memberNode.put("memberId", member.getId());
            memberNode.put("firstName", nullSafe(member.getFirstName()));
            memberNode.put("lastName", nullSafe(member.getLastName()));
            memberNode.put("fullName", memberFullName(member));
            memberNode.put("relationship", member.getRelationship() != null ? member.getRelationship().name() : "");
            memberNode.put("relationshipToMainApplicant", relationshipLabel(member.getRelationship(), responses));
            memberNode.put("displayLabel", displayLabel);
            memberNode.put("writingPerspective", "Address this member as '" + displayLabel + "' and write to the main applicant in second person.");
            memberNode.put("ageAtDeparture", member.getAgeAtDeparture() != null ? member.getAgeAtDeparture() : 0);
            memberNode.set("responses", responses);
        }
        return payload;
    }

    private JsonNode parseResponsesJson(String responsesJson) {
        if (responsesJson == null || responsesJson.isBlank()) {
            return objectMapper.createObjectNode();
        }
        try {
            return objectMapper.readTree(responsesJson);
        } catch (Exception ex) {
            ObjectNode fallback = objectMapper.createObjectNode();
            fallback.put("_raw", responsesJson);
            return fallback;
        }
    }

    private String familyMemberDisplayLabel(FamilyTripMember member, JsonNode responses) {
        String name = memberPreferredName(member);
        FamilyMemberRelationship relationship = member.getRelationship();
        if (relationship == null) {
            return appendName("Your family member", name);
        }
        return switch (relationship) {
            case MAIN_APPLICANT -> name.isBlank() ? "You" : "You, " + name;
            case SPOUSE -> appendName("Your spouse", name);
            case CHILD, ADDITIONAL_CHILD -> appendName(childRelationshipLabel(responses), name);
            case PARENT -> appendName(parentRelationshipLabel(responses), name);
            case DEPENDENT -> appendName("Your dependent", name);
            case ADDITIONAL_ADULT -> appendName("Your family member", name);
        };
    }

    private String childRelationshipLabel(JsonNode responses) {
        String gender = inferGender(responses);
        if ("male".equals(gender)) {
            return "Your son";
        }
        if ("female".equals(gender)) {
            return "Your daughter";
        }
        return "Your child";
    }

    private String parentRelationshipLabel(JsonNode responses) {
        String gender = inferGender(responses);
        if ("male".equals(gender)) {
            return "Your father";
        }
        if ("female".equals(gender)) {
            return "Your mother";
        }
        return "Your parent";
    }

    private String relationshipLabel(FamilyMemberRelationship relationship, JsonNode responses) {
        if (relationship == null) {
            return "family member";
        }
        return switch (relationship) {
            case MAIN_APPLICANT -> "you";
            case SPOUSE -> "spouse";
            case CHILD, ADDITIONAL_CHILD -> childRelationshipLabel(responses).replace("Your ", "");
            case PARENT -> parentRelationshipLabel(responses).replace("Your ", "");
            case DEPENDENT -> "dependent";
            case ADDITIONAL_ADULT -> "family member";
        };
    }

    private String inferGender(JsonNode responses) {
        String value = findTextValue(responses, "gender", "sex", "biologicalSex", "biological_sex");
        if (value == null) {
            return "";
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (normalized.contains("female") || normalized.equals("f") || normalized.contains("woman")) {
            return "female";
        }
        if (normalized.contains("male") || normalized.equals("m") || normalized.contains("man")) {
            return "male";
        }
        return "";
    }

    private String findTextValue(JsonNode node, String... fieldsToFind) {
        if (node == null || node.isNull() || node.isMissingNode()) {
            return null;
        }
        if (node.isObject()) {
            String keyedAnswer = keyedAnswerText(node, fieldsToFind);
            if (keyedAnswer != null && !keyedAnswer.isBlank()) {
                return keyedAnswer;
            }
            var fields = node.fields();
            while (fields.hasNext()) {
                var entry = fields.next();
                if (matchesField(entry.getKey(), fieldsToFind)) {
                    String value = readableText(entry.getValue());
                    if (value != null && !value.isBlank()) {
                        return value;
                    }
                }
                String nested = findTextValue(entry.getValue(), fieldsToFind);
                if (nested != null && !nested.isBlank()) {
                    return nested;
                }
            }
        } else if (node.isArray()) {
            for (JsonNode child : node) {
                String nested = findTextValue(child, fieldsToFind);
                if (nested != null && !nested.isBlank()) {
                    return nested;
                }
            }
        }
        return null;
    }

    private String keyedAnswerText(JsonNode node, String... fieldsToFind) {
        String key = firstReadableChild(node, "key", "questionKey", "field", "name");
        if (!matchesField(key, fieldsToFind)) {
            return null;
        }
        return firstReadableChild(node, "value", "answer", "response", "selectedValue", "selected", "label");
    }

    private String firstReadableChild(JsonNode node, String... fieldNames) {
        if (node == null || !node.isObject()) {
            return null;
        }
        for (String fieldName : fieldNames) {
            String value = readableText(node.path(fieldName));
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private boolean matchesField(String candidate, String... fieldsToFind) {
        if (candidate == null) {
            return false;
        }
        String normalizedCandidate = normalizeFieldName(candidate);
        for (String field : fieldsToFind) {
            if (normalizedCandidate.equals(normalizeFieldName(field))) {
                return true;
            }
        }
        return false;
    }

    private String readableText(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode()) {
            return null;
        }
        if (node.isTextual() || node.isNumber() || node.isBoolean()) {
            return node.asText();
        }
        if (node.isObject()) {
            String value = findTextValue(node, "value", "label", "text", "answer");
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private String normalizeFieldName(String value) {
        return value == null ? "" : value.replace("_", "").replace("-", "").toLowerCase(Locale.ROOT);
    }

    private String appendName(String label, String name) {
        return name == null || name.isBlank() ? label : label + " " + name;
    }

    private String memberPreferredName(FamilyTripMember member) {
        String firstName = member.getFirstName() == null ? "" : member.getFirstName().trim();
        if (!firstName.isBlank()) {
            return firstName;
        }
        String fullName = memberFullName(member);
        return fullName.startsWith("Member #") ? "" : fullName;
    }

    private String memberFullName(FamilyTripMember member) {
        String fullName = (nullSafe(member.getFirstName()) + " " + nullSafe(member.getLastName())).trim();
        return fullName.isBlank() ? "Member #" + member.getId() : fullName;
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }
    
    public List<FamilyTripResponse> getUserTrips(Long userId) {
        return familyTripRepository.findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(userId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    public org.springframework.data.domain.Page<FamilyTripResponse> getUserTrips(Long userId, org.springframework.data.domain.Pageable pageable) {
        return familyTripRepository.findByUserIdAndDeletedAtIsNull(userId, pageable)
            .map(this::toResponse);
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

    private PricingResult calculatePricing(List<FamilyTripMemberRequest> members, LocalDate departureDate, com.TravelMedicineAdvisory.Server.domain.company.BillingCurrency billingCurrency, int baseMemberCount) {
        if (members == null) members = new ArrayList<>();

        int memberCount = members.size();
        int includedMembers = Math.min(baseMemberCount, memberCount);
        int additionalMembers = Math.max(0, memberCount - baseMemberCount);
        
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
        return purchaseRepository.findAvailableByUserId(userId).orElse(null);
    }
    
    // private ActivePackageAllowanceDto getActiveAllowance(Long userId) {
    //     FamilyPackagePurchase p = findActiveAllowance(userId);
    //     if (p == null) return null;
    //     return new ActivePackageAllowanceDto(p.getPackageType().name(), p.getTripsAllowed() - p.getTripsUsed());
    // }

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

        Long familyPlanId = trip.getTravelPlan() != null ? trip.getTravelPlan().getId() : null;

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
            familyPlanId,
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
