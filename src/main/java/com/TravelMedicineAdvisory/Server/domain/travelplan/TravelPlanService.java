package com.TravelMedicineAdvisory.Server.domain.travelplan;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import com.TravelMedicineAdvisory.Server.core.utils.QuestionnaireResponseSanitizer;
import com.TravelMedicineAdvisory.Server.domain.company.Company;
import com.TravelMedicineAdvisory.Server.domain.company.CompanyRepository;
import com.TravelMedicineAdvisory.Server.domain.companyuser.CompanyUser;
import com.TravelMedicineAdvisory.Server.domain.companyuser.CompanyUserRepository;
import com.TravelMedicineAdvisory.Server.domain.creditplan.CreditPlan;
import com.TravelMedicineAdvisory.Server.domain.doctor.AssignedDoctorDto;
import com.TravelMedicineAdvisory.Server.domain.familytrip.FamilyTrip;
import com.TravelMedicineAdvisory.Server.domain.familytrip.FamilyTripMember;
import com.TravelMedicineAdvisory.Server.domain.doctor.DoctorValidationStatus;
import com.TravelMedicineAdvisory.Server.domain.doctor.TravelPlanDoctorAssignment;
import com.TravelMedicineAdvisory.Server.domain.doctor.TravelPlanDoctorAssignmentRepository;
import com.TravelMedicineAdvisory.Server.domain.employee.Employee;
import com.TravelMedicineAdvisory.Server.domain.employee.EmployeeRepository;
import com.TravelMedicineAdvisory.Server.domain.plans.GeneratedPlan;
import com.TravelMedicineAdvisory.Server.domain.plans.GeneratedPlanRepository;
import com.TravelMedicineAdvisory.Server.domain.plans.PlanGenerationService;
import com.TravelMedicineAdvisory.Server.domain.plans.PlanTier;
import com.TravelMedicineAdvisory.Server.domain.travelplanquestionnaire.TravelPlanQuestionnaire;
import com.TravelMedicineAdvisory.Server.domain.travelplanquestionnaire.TravelPlanQuestionnaireRepository;
import com.TravelMedicineAdvisory.Server.domain.user.User;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityNotFoundException;

@Service
@Transactional
public class TravelPlanService {

    private final TravelPlanRepository repository;
    private final CompanyRepository companyRepository;
    private final CompanyUserRepository companyUserRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final PlanGenerationService planGenerationService;
    private final GeneratedPlanRepository generatedPlanRepository;
    private final TravelPlanPdfGenerator travelPlanPdfGenerator;
    private final TravelPlanQuestionnaireRepository travelPlanQuestionnaireRepository;
    private final TravelPlanDoctorAssignmentRepository assignmentRepository;
    private final ObjectMapper objectMapper;

    public TravelPlanService(TravelPlanRepository repository, CompanyRepository companyRepository,
            CompanyUserRepository companyUserRepository,
            EmployeeRepository employeeRepository, UserRepository userRepository,
            PlanGenerationService planGenerationService,
            GeneratedPlanRepository generatedPlanRepository,
            TravelPlanPdfGenerator travelPlanPdfGenerator,
            TravelPlanQuestionnaireRepository travelPlanQuestionnaireRepository,
            TravelPlanDoctorAssignmentRepository assignmentRepository,
            ObjectMapper objectMapper) {
        this.repository = repository;
        this.companyRepository = companyRepository;
        this.companyUserRepository = companyUserRepository;
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.planGenerationService = planGenerationService;
        this.generatedPlanRepository = generatedPlanRepository;
        this.travelPlanPdfGenerator = travelPlanPdfGenerator;
        this.travelPlanQuestionnaireRepository = travelPlanQuestionnaireRepository;
        this.assignmentRepository = assignmentRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public Page<TravelPlanListItemResponse> findAll(Long companyId, Long currentUserId, Pageable pageable) {
        if (companyId != null) {
            if (!isUserInCompany(currentUserId, companyId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "You do not have access to this company's plans");
            }
            return repository.findListItemsByCompanyId(companyId, pageable);
        }
        return repository.findListItemsByUserId(currentUserId, pageable);
    }

    @Transactional(readOnly = true)
    public TravelPlanResponse findById(Long id, Long currentUserId) {
        TravelPlan entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("TravelPlan not found"));
        if (!canAccessPlan(entity, currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to this travel plan");
        }
        GeneratedPlanPayload generated = generatedPlanRepository.findByTravelPlanId(id)
                .map(this::toGeneratedPayload)
                .orElse(null);
        return toResponse(entity, generated);
    }

    @Transactional(readOnly = true)
    public TravelPlanResponse findByIdInternal(Long id) {
        TravelPlan entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("TravelPlan not found"));
        GeneratedPlanPayload generated = generatedPlanRepository.findByTravelPlanId(id)
                .map(this::toGeneratedPayload)
                .orElse(null);
        return toResponse(entity, generated);
    }

    @Transactional(readOnly = true)
    public TravelPlanPdfExport exportPdfForUser(Long planId, Long currentUserId) {
        TravelPlan plan = repository.findById(planId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Travel plan not found"));
        if (plan.getUser() == null || !plan.getUser().getId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to this plan");
        }
        if (!isPlanCompleted(plan)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Travel plan PDF is only available when the plan is completed");
        }
        GeneratedPlan generated = requireActiveGeneratedPlanForPdf(planId);
        byte[] pdf = travelPlanPdfGenerator.generate(plan, generated);
        return new TravelPlanPdfExport(pdf, slugifyFilename(plan.getDestination()));
    }

    private GeneratedPlan requireActiveGeneratedPlanForPdf(Long planId) {
        GeneratedPlan generated = generatedPlanRepository.findByTravelPlanId(planId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
                        "Generated plan is not ready yet, please try again shortly"));
        if ("failed".equalsIgnoreCase(generated.getStatus())) {
            String message = StringUtils.hasText(generated.getErrorMessage())
                    ? "Generated plan failed: " + generated.getErrorMessage()
                    : "Generated plan failed";
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
        if (!"active".equalsIgnoreCase(generated.getStatus()) || !StringUtils.hasText(generated.getPlanJson())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Generated plan is not ready yet, please try again shortly");
        }
        try {
            JsonNode root = objectMapper.readTree(generated.getPlanJson());
            if (root == null || !root.isObject() || root.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Generated plan contains no renderable data");
            }
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Generated plan JSON is invalid");
        }
        return generated;
    }

    @Transactional(readOnly = true)
    public String exportSummaryPdfUrlForUser(Long planId, Long currentUserId) {
        TravelPlan plan = repository.findById(planId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Travel plan not found"));
        if (plan.getUser() == null || !plan.getUser().getId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to this plan");
        }
        if (!isPlanCompleted(plan)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Travel plan summary PDF is only available when the plan is completed");
        }
        if (plan.getPlanTier() == null || plan.getPlanTier() == PlanTier.FREE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Travel plan summary PDF is not available for this plan");
        }
        GeneratedPlan generated = generatedPlanRepository.findByTravelPlanId(planId).orElse(null);
        String url = generated != null ? generated.getSummaryPdfUrl() : null;
        if (!StringUtils.hasText(url)) {
            throw new ResponseStatusException(HttpStatus.ACCEPTED,
                    "Summary PDF is still being generated, please try again shortly");
        }
        return url;
    }

    private static String slugifyFilename(String destination) {
        if (!StringUtils.hasText(destination)) {
            return "travel-health-plan";
        }
        String s = destination.toLowerCase().replaceAll("[^a-z0-9]+", "-");
        s = s.replaceAll("^-+", "").replaceAll("-+$", "");
        if (!StringUtils.hasText(s)) {
            return "travel-health-plan";
        }
        return s.length() > 48 ? s.substring(0, 48) : s;
    }

    private boolean isPlanCompleted(TravelPlan plan) {
        String status = plan.getStatus();
        return status != null && "COMPLETED".equalsIgnoreCase(status);
    }

    private boolean canAccessPlan(TravelPlan plan, Long currentUserId) {
        if (plan.getUser() != null && plan.getUser().getId().equals(currentUserId)) {
            return true;
        }
        if (plan.getCompany() != null) {
            return isUserInCompany(currentUserId, plan.getCompany().getId());
        }
        return false;
    }

    private boolean isUserInCompany(Long userId, Long companyId) {
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found");
        }
        return companyUserRepository.existsActiveByUserIdAndCompanyId(userId, companyId);
    }

    @Transactional
    public TravelPlanResponse create(TravelPlanRequest request) {

        Long userId = request.userId();
        Long employeeId = request.employeeId();

        User user;

        if (request.employeeId() == null) {
            user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));
        } else {
            Employee employee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new EntityNotFoundException("Employee not found"));
            user = employee.getUser();
        }

        Optional<Employee> employee = employeeRepository.findByUser(user);

        if (user.getCredits() < 1) {
            throw new RuntimeException("Insufficient credits");
        }

        user.setCredits(user.getCredits() - 1);
        userRepository.save(user);

        if (employee.isPresent()) {
            Employee employeeEntity = employee.get();
            int used = employeeEntity.getCreditsUsed() != null ? employeeEntity.getCreditsUsed() : 0;
            employeeEntity.setCreditsUsed(used + 1);
            int plans = employeeEntity.getPlansGenerated() != null ? employeeEntity.getPlansGenerated() : 0;
            employeeEntity.setPlansGenerated(plans + 1);
            employeeRepository.save(employeeEntity);

            if (employeeEntity.getCompany() != null) {
                Company company = companyRepository.findById(employeeEntity.getCompany().getId())
                        .orElse(null);
                if (company != null) {
                    int companyUsed = company.getUsedCredits() != null ? company.getUsedCredits() : 0;
                    company.setUsedCredits(companyUsed + 1);
                    companyRepository.save(company);
                }
            }
        }

        validateReturnTripOrThrow(request);
        TravelPlanRequest normalized = normalizeDurationFromTripDetailsJson(normalizeDurationForReturnTrip(request));

        TravelPlan entity = new TravelPlan();
        mapRequestToEntity(normalized, entity);
        entity.setStatus("QUEUED");

        String tier = resolvePlanTier(user);
        entity.setPlanTier(PlanTier.valueOf(tier));
        entity.setDoctorValidationStatus(
                tier.equalsIgnoreCase(PlanTier.FREE.name()) ? DoctorValidationStatus.NOT_REQUIRED : DoctorValidationStatus.PENDING);

        TravelPlan saved = repository.save(entity);
        assignSelectedDoctors(saved, normalized.selectedDoctorIds());
        persistQuestionnaireResponses(normalized, saved);

        planGenerationService.enqueueGeneration(saved.getId(), user.getId());

        return toResponse(saved);
    }

    @Deprecated
    @Transactional
    public TravelPlan createForFamilyMember(FamilyTripMember member, FamilyTrip trip) {
        TravelPlan plan = new TravelPlan();
        plan.setUser(trip.getUser());
        plan.setDestination(trip.getDestination());
        plan.setCountry(trip.getCountry());
        plan.setDuration(trip.getDuration());
        plan.setPurpose(trip.getPurpose());
        plan.setTripType(trip.getTripType());
        plan.setTripDetailsJson(trip.getTripDetailsJson());
        plan.setStatus("QUEUED");
        plan.setFamilyTrip(trip);
        plan.setFamilyTripMember(member);
        plan.setTravellerDisplayName(member.getFirstName() + " " + member.getLastName());
        plan.setTravellerRelationship(member.getRelationship().name());
        
        String tier = resolvePlanTier(trip.getUser());
        plan.setPlanTier(PlanTier.valueOf(tier));
        plan.setDoctorValidationStatus(
                tier.equalsIgnoreCase(PlanTier.FREE.name()) ? DoctorValidationStatus.NOT_REQUIRED : DoctorValidationStatus.PENDING);

        return repository.save(plan);
    }

    @Transactional
    public TravelPlan createForFamilyTrip(com.TravelMedicineAdvisory.Server.domain.familytrip.FamilyTrip trip) {
        TravelPlan plan = new TravelPlan();
        plan.setUser(trip.getUser());
        plan.setDestination(trip.getDestination());
        plan.setCountry(trip.getCountry());
        plan.setDuration(trip.getDuration());
        plan.setPurpose(trip.getPurpose());
        plan.setTripType(trip.getTripType());
        plan.setTripDetailsJson(trip.getTripDetailsJson());
        plan.setStatus("QUEUED");
        plan.setFamilyTrip(trip);
        plan.setFamilyTripMember(null);
        plan.setTravellerDisplayName(trip.getUser().getFirstName() + " family");
        String tier = resolvePlanTier(trip.getUser());
        plan.setPlanTier(PlanTier.valueOf(tier));
        plan.setDoctorValidationStatus(
                tier.equalsIgnoreCase(PlanTier.FREE.name()) ? DoctorValidationStatus.NOT_REQUIRED : DoctorValidationStatus.PENDING);
        return repository.save(plan);
    }

    private void persistQuestionnaireResponses(TravelPlanRequest request, TravelPlan travelPlan) {
        if (!StringUtils.hasText(request.questionnaireResponses())) {
            return;
        }
        TravelPlanQuestionnaire questionnaire = travelPlanQuestionnaireRepository
                .findByTravelPlan_Id(travelPlan.getId())
                .orElseGet(TravelPlanQuestionnaire::new);
        questionnaire.setTravelPlan(travelPlan);
        questionnaire.setUser(travelPlan.getUser());
        questionnaire.setEmployee(travelPlan.getEmployee());
        questionnaire.setCompany(travelPlan.getCompany());
        questionnaire.setSource("create-plan");
        questionnaire.setResponsesJson(
                QuestionnaireResponseSanitizer.sanitize(request.questionnaireResponses(), objectMapper));
        travelPlanQuestionnaireRepository.save(questionnaire);
    }

    public TravelPlanResponse update(Long id, TravelPlanRequest request) {
        TravelPlan entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("TravelPlan not found"));
        validateReturnTripOrThrow(request);
        TravelPlanRequest normalized = normalizeDurationFromTripDetailsJson(normalizeDurationForReturnTrip(request));
        mapRequestToEntity(normalized, entity);
        TravelPlan saved = repository.save(entity);
        return toResponse(saved);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new NoSuchElementException("TravelPlan not found");
        }
        repository.deleteById(id);
    }

    public TravelPlanResponse duplicateForTest(Long sourcePlanId) {
        TravelPlan source = repository.findById(sourcePlanId)
                .orElseThrow(() -> new NoSuchElementException("TravelPlan not found"));

        TravelPlan entity = new TravelPlan();
        entity.setDestination(source.getDestination());
        entity.setCountry(source.getCountry());
        entity.setDuration(source.getDuration());
        entity.setPurpose(source.getPurpose());
        entity.setTripType(source.getTripType());
        entity.setTripDetailsJson(source.getTripDetailsJson());
        entity.setRiskScore(source.getRiskScore());
        entity.setStatus("QUEUED");
        entity.setPlanTier(PlanTier.PREMIUM);
        entity.setCompany(source.getCompany());
        entity.setEmployee(source.getEmployee());
        entity.setUser(source.getUser());
        TravelPlan saved = repository.save(entity);

        travelPlanQuestionnaireRepository.findByTravelPlan_Id(sourcePlanId).ifPresent(sourceQ -> {
            TravelPlanQuestionnaire questionnaire = new TravelPlanQuestionnaire();
            questionnaire.setTravelPlan(saved);
            questionnaire.setUser(saved.getUser());
            questionnaire.setEmployee(saved.getEmployee());
            questionnaire.setCompany(saved.getCompany());
            questionnaire.setSource(sourceQ.getSource());
            questionnaire.setResponsesJson(sourceQ.getResponsesJson());
            travelPlanQuestionnaireRepository.save(questionnaire);
        });

        if (saved.getUser() != null) {
            planGenerationService.enqueueGeneration(saved.getId(), saved.getUser().getId());
        }

        return toResponse(saved);
    }

    private TravelPlanResponse toResponse(TravelPlan entity) {
        return toResponse(entity, null);
    }

    private TravelPlanResponse toResponse(TravelPlan entity, GeneratedPlanPayload generatedPlan) {
        User validatedBy = entity.getValidatedBy();
        String validatedByName = validatedBy != null
                ? ((validatedBy.getFirstName() != null ? validatedBy.getFirstName() : "") + " " +
                        (validatedBy.getLastName() != null ? validatedBy.getLastName() : "")).trim()
                : null;
        String signedPdfUrl = generatedPlan != null ? generatedPlan.signedPdfUrl() : null;
        String summaryPdfUrl = generatedPlan != null ? generatedPlan.summaryPdfUrl() : null;
        List<AssignedDoctorDto> assignedDoctors = assignmentRepository.findByTravelPlanIdAndDeletedAtIsNull(entity.getId()).stream()
                .map(a -> {
                    User d = a.getDoctor();
                    return new AssignedDoctorDto(
                            d.getId(),
                            d.getFirstName(),
                            d.getLastName(),
                            d.getEmail(),
                            d.getAvatarUrl());
                })
                .toList();
        Boolean openToAllDoctors = assignedDoctors.isEmpty() ? true : null;
        return new TravelPlanResponse(
                entity.getId(),
                entity.getDestination(),
                entity.getCountry(),
                entity.getDuration(),
                entity.getPurpose(),
                entity.getTripType(),
                entity.getTripDetailsJson(),
                entity.getRiskScore(),
                entity.getStatus(),
                entity.getMedicalConsiderations(),
                entity.getVaccinations(),
                entity.getHealthAlerts(),
                entity.getSafetyAdvisories(),
                entity.getMedications(),
                entity.getWaterFood(),
                entity.getEmergencyContacts(),
                entity.getCompany() != null ? entity.getCompany().getId() : null,
                entity.getEmployee() != null ? entity.getEmployee().getId() : null,
                entity.getUser() != null ? entity.getUser().getId() : null,
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                generatedPlan,
                entity.getPlanTier() != null ? entity.getPlanTier().name() : null,
                entity.getDoctorValidationStatus() != null ? entity.getDoctorValidationStatus().name() : null,
                validatedByName,
                entity.getValidatedAt(),
                entity.getRejectionReason(),
                signedPdfUrl,
                summaryPdfUrl,
                assignedDoctors,
                openToAllDoctors);
    }

    private GeneratedPlanPayload toGeneratedPayload(GeneratedPlan g) {
        return new GeneratedPlanPayload(
                g.getStatus(),
                g.getPlanJson(),
                g.getProvider(),
                g.getModelUsed(),
                g.getTokensUsed(),
                g.getProcessingTimeMs(),
                g.getErrorMessage(),
                g.getSignedPdfUrl(),
                g.getSummaryPdfUrl(),
                g.getIsSigned());
    }

    private void mapRequestToEntity(TravelPlanRequest request, TravelPlan entity) {
        entity.setDestination(request.destination());
        entity.setCountry(request.country());
        entity.setDuration(request.duration());
        entity.setPurpose(request.purpose());
        entity.setTripType(request.tripType());
        entity.setTripDetailsJson(request.tripDetailsJson());
        entity.setRiskScore(request.riskScore());
        entity.setStatus(request.status());
        entity.setMedicalConsiderations(request.medicalConsiderations());
        entity.setVaccinations(request.vaccinations());
        entity.setHealthAlerts(request.healthAlerts());
        entity.setSafetyAdvisories(request.safetyAdvisories());
        entity.setMedications(request.medications());
        entity.setWaterFood(request.waterFood());
        entity.setEmergencyContacts(request.emergencyContacts());
        if (request.companyId() != null) {
            Company company = companyRepository.findById(request.companyId())
                    .orElseThrow(() -> new NoSuchElementException("Company not found"));
            entity.setCompany(company);
        }
        if (request.employeeId() != null) {
            Employee employee = employeeRepository.findById(request.employeeId())
                    .orElseThrow(() -> new NoSuchElementException("Employee not found"));
            User user = employee.getUser();
            entity.setEmployee(employee);
            entity.setUser(user);
            if (entity.getCompany() == null) {
                entity.setCompany(employee.getCompany());
            }
        } else if (request.userId() != null) {
            User user = userRepository.findById(request.userId())
                    .orElseThrow(() -> new NoSuchElementException("User not found"));
            entity.setUser(user);
            if (entity.getCompany() == null) {
                companyUserRepository.findAllByUser(user).stream()
                        .filter(companyUser -> companyUser.getDeletedAt() == null)
                        .findFirst()
                        .map(CompanyUser::getCompany)
                        .ifPresent(entity::setCompany);
            }
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Either employee or user must be provided");
        }
    }

    private static boolean isReturnTripType(String tripType) {
        return tripType != null && "return".equalsIgnoreCase(tripType.trim());
    }

    private void validateReturnTripOrThrow(TravelPlanRequest request) {
        if (!isReturnTripType(request.tripType())) {
            return;
        }
        if (!StringUtils.hasText(request.tripDetailsJson())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Round trip requires trip details with departure and return dates");
        }
        try {
            JsonNode root = objectMapper.readTree(request.tripDetailsJson());
            String dep = tripDetailText(root, "departureDate");
            String ret = tripDetailText(root, "returnDate");
            if (!StringUtils.hasText(dep) || !StringUtils.hasText(ret)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Round trip requires departureDate and returnDate (YYYY-MM-DD)");
            }
            LocalDate d0 = LocalDate.parse(dep.trim());
            LocalDate d1 = LocalDate.parse(ret.trim());
            if (d1.isBefore(d0)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Return date must be on or after the departure date");
            }
        } catch (DateTimeParseException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid date format; use YYYY-MM-DD for departure and return");
        } catch (JsonProcessingException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid trip details JSON");
        }
    }

    private TravelPlanRequest normalizeDurationForReturnTrip(TravelPlanRequest request) {
        if (!isReturnTripType(request.tripType()) || !StringUtils.hasText(request.tripDetailsJson())) {
            return request;
        }
        try {
            JsonNode root = objectMapper.readTree(request.tripDetailsJson());
            String dep = tripDetailText(root, "departureDate").trim();
            String ret = tripDetailText(root, "returnDate").trim();
            if (!StringUtils.hasText(dep) || !StringUtils.hasText(ret)) {
                return request;
            }
            LocalDate d0 = LocalDate.parse(dep);
            LocalDate d1 = LocalDate.parse(ret);
            if (d1.isBefore(d0)) {
                return request;
            }
            int inclusive = (int) ChronoUnit.DAYS.between(d0, d1) + 1;
            return copyRequestWithDuration(request, inclusive);
        } catch (JsonProcessingException ex) {
            return request;
        }
    }

    /**
     * When duration is missing or non-positive, derive days from tripDetailsJson (one-way, multi,
     * transit) so submission is not blocked.
     */
    private TravelPlanRequest normalizeDurationFromTripDetailsJson(TravelPlanRequest request) {
        Integer dur = request.duration();
        if (dur != null && dur > 0) {
            return request;
        }
        if (!StringUtils.hasText(request.tripDetailsJson())) {
            return request;
        }
        try {
            JsonNode root = objectMapper.readTree(request.tripDetailsJson());
            Integer inferred = inferDurationDaysFromTripDetails(root);
            if (inferred != null && inferred > 0) {
                return copyRequestWithDuration(request, inferred);
            }
        } catch (JsonProcessingException ignored) {
        }
        return request;
    }

    private static Integer inferDurationDaysFromTripDetails(JsonNode root) {
        String tt = tripDetailText(root, "tripType").trim();
        if (!StringUtils.hasText(tt)) {
            return null;
        }
        if ("return".equalsIgnoreCase(tt)) {
            return inclusiveDaysBetweenOrNull(tripDetailText(root, "departureDate"),
                    tripDetailText(root, "returnDate"));
        }
        if ("transit".equalsIgnoreCase(tt)) {
            Integer span = inclusiveDaysBetweenOrNull(tripDetailText(root, "departureDate"),
                    tripDetailText(root, "returnDate"));
            if (span != null && span > 0) {
                return span;
            }
            return transitDurationBucketDays(tripDetailText(root, "transitDuration"));
        }
        if ("multi".equalsIgnoreCase(tt)) {
            JsonNode stops = root.path("stops");
            String overall = tripDetailText(root, "overallReturnDate").trim();
            String firstArrival = "";
            if (stops.isArray() && stops.size() > 0) {
                firstArrival = tripDetailText(stops.get(0), "arrivalDate").trim();
            }
            Integer span = inclusiveDaysBetweenOrNull(firstArrival, overall);
            if (span != null && span > 0) {
                return span;
            }
            int totalNights = 0;
            if (stops.isArray()) {
                for (JsonNode leg : stops) {
                    String nightsRaw = tripDetailText(leg, "nights").trim();
                    try {
                        int n = Integer.parseInt(nightsRaw);
                        if (n > 0) {
                            totalNights += n;
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
            return totalNights > 0 ? totalNights + 1 : null;
        }
        if ("one-way".equalsIgnoreCase(tt)) {
            return lengthOfStayBucketDays(tripDetailText(root, "lengthOfStay"));
        }
        return null;
    }

    private static Integer inclusiveDaysBetweenOrNull(String start, String end) {
        if (!StringUtils.hasText(start) || !StringUtils.hasText(end)) {
            return null;
        }
        try {
            LocalDate d0 = LocalDate.parse(start.trim());
            LocalDate d1 = LocalDate.parse(end.trim());
            if (d1.isBefore(d0)) {
                return null;
            }
            return (int) ChronoUnit.DAYS.between(d0, d1) + 1;
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    private static Integer lengthOfStayBucketDays(String raw) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        return switch (raw.trim()) {
            case "<1m" -> 30;
            case "1-3m" -> 90;
            case "3-6m" -> 180;
            case "6-12m" -> 365;
            case "12m+", "open" -> 366;
            default -> null;
        };
    }

    private static Integer transitDurationBucketDays(String raw) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        return switch (raw.trim()) {
            case "<12h", "12-24h" -> 1;
            case ">24h" -> 2;
            default -> null;
        };
    }

    private static TravelPlanRequest copyRequestWithDuration(TravelPlanRequest request, int duration) {
        return new TravelPlanRequest(
                request.destination(),
                request.country(),
                duration,
                request.purpose(),
                request.tripType(),
                request.tripDetailsJson(),
                request.riskScore(),
                request.status(),
                request.medicalConsiderations(),
                request.vaccinations(),
                request.healthAlerts(),
                request.safetyAdvisories(),
                request.medications(),
                request.waterFood(),
                request.emergencyContacts(),
                request.questionnaireResponses(),
                request.companyId(),
                request.employeeId(),
                request.userId(),
                request.planTier(),
                request.selectedDoctorIds());
    }

    private static String tripDetailText(JsonNode root, String field) {
        JsonNode n = root.path(field);
        return n.isTextual() ? n.asText() : "";
    }

    private void assignSelectedDoctors(TravelPlan travelPlan, java.util.List<Long> selectedDoctorIds) {
        if (selectedDoctorIds == null || selectedDoctorIds.isEmpty()) {
            return;
        }
        for (Long doctorId : selectedDoctorIds.stream().distinct().toList()) {
            User doctor = userRepository.findById(doctorId)
                    .orElseThrow(() -> new NoSuchElementException("Doctor not found: " + doctorId));
            if (doctor.getRole() == null || doctor.getRole().getName() == null
                    || !"Doctor".equalsIgnoreCase(doctor.getRole().getName())) {
                throw new IllegalArgumentException("Selected reviewer is not a doctor: " + doctorId);
            }
            TravelPlanDoctorAssignment assignment = new TravelPlanDoctorAssignment();
            assignment.setTravelPlan(travelPlan);
            assignment.setDoctor(doctor);
            assignmentRepository.save(assignment);
        }
    }

    private String resolvePlanTier(User user) {
        if ("company".equalsIgnoreCase(user.getType())) {
            CompanyUser companyUser = companyUserRepository.findByUser(user).orElse(null);
            if (companyUser != null &&
                    companyUser.getCompany() != null &&
                    companyUser.getCompany().getCreditPlan() != null) {
                return switch (companyUser.getCompany().getCreditPlan().getCode()) {
                    case "ENTERPRISE_SILVER" -> PlanTier.STANDARD.name();
                    case "ENTERPRISE_PLUS" -> PlanTier.PREMIUM.name();
                    case "ENTERPRISE_GOLD" -> PlanTier.STANDARD.name();
                    case "ENTERPRISE_ELITE" -> PlanTier.PREMIUM.name();
                    case "ENTERPRISE_PLATINUM" -> PlanTier.STANDARD.name();
                    case "ENTERPRISE_SIGNATURE" -> PlanTier.PREMIUM.name();
                    default -> {
                        String serviceLevel = companyUser.getCompany().getCreditPlan().getServiceLevel();
                        yield "PREMIUM".equalsIgnoreCase(serviceLevel) ? PlanTier.PREMIUM.name()
                                : "STANDARD".equalsIgnoreCase(serviceLevel) ? PlanTier.STANDARD.name()
                                : PlanTier.FREE.name();
                    }
                };
            }
        }

        CreditPlan creditPlan = user.getCreditPlan();
        if (creditPlan == null || creditPlan.getCode() == null) {
            return PlanTier.FREE.name();
        }
        return switch (creditPlan.getCode()) {
            case "ESSENTIAL" -> PlanTier.FREE.name();
            case "STANDARD" -> PlanTier.STANDARD.name();
            case "PREMIUM" -> PlanTier.PREMIUM.name();
            default -> {
                String serviceLevel = creditPlan.getServiceLevel();
                yield "PREMIUM".equalsIgnoreCase(serviceLevel) ? PlanTier.PREMIUM.name()
                        : "STANDARD".equalsIgnoreCase(serviceLevel) ? PlanTier.STANDARD.name()
                        : PlanTier.FREE.name();
            }
        };
    }
}
