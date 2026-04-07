package com.TravelMedicineAdvisory.Server.domain.travelplan;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.NoSuchElementException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import com.TravelMedicineAdvisory.Server.domain.company.Company;
import com.TravelMedicineAdvisory.Server.domain.company.CompanyRepository;
import com.TravelMedicineAdvisory.Server.domain.employee.Employee;
import com.TravelMedicineAdvisory.Server.domain.employee.EmployeeRepository;
import com.TravelMedicineAdvisory.Server.domain.plans.GeneratedPlan;
import com.TravelMedicineAdvisory.Server.domain.plans.GeneratedPlanRepository;
import com.TravelMedicineAdvisory.Server.domain.plans.PlanGenerationService;
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
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final PlanGenerationService planGenerationService;
    private final GeneratedPlanRepository generatedPlanRepository;
    private final TravelPlanPdfGenerator travelPlanPdfGenerator;
    private final TravelPlanQuestionnaireRepository travelPlanQuestionnaireRepository;
    private final ObjectMapper objectMapper;

    public TravelPlanService(TravelPlanRepository repository, CompanyRepository companyRepository,
            EmployeeRepository employeeRepository, UserRepository userRepository,
            PlanGenerationService planGenerationService,
            GeneratedPlanRepository generatedPlanRepository,
            TravelPlanPdfGenerator travelPlanPdfGenerator,
            TravelPlanQuestionnaireRepository travelPlanQuestionnaireRepository,
            ObjectMapper objectMapper) {
        this.repository = repository;
        this.companyRepository = companyRepository;
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.planGenerationService = planGenerationService;
        this.generatedPlanRepository = generatedPlanRepository;
        this.travelPlanPdfGenerator = travelPlanPdfGenerator;
        this.travelPlanQuestionnaireRepository = travelPlanQuestionnaireRepository;
        this.objectMapper = objectMapper;
    }

    public Page<TravelPlanResponse> findAll(Long companyId, Pageable pageable) {
        if (companyId != null) {
            return repository.findAllByCompanyId(companyId, pageable)
                    .map(this::toResponse);
        }
        return repository.findAll(pageable)
                .map(this::toResponse);
    }

    public TravelPlanResponse findById(Long id) {
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
        String status = plan.getStatus();
        if (status == null || !"COMPLETED".equalsIgnoreCase(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Travel plan PDF is only available when the plan is completed");
        }
        GeneratedPlan generated = generatedPlanRepository.findByTravelPlanId(planId).orElse(null);
        byte[] pdf = travelPlanPdfGenerator.generate(plan, generated);
        return new TravelPlanPdfExport(pdf, slugifyFilename(plan.getDestination()));
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

        Employee employee = employeeRepository.findByUser(user)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));

        if (user.getCredits() < 1) {
            throw new RuntimeException("Insufficient credits");
        }

        user.setCredits(user.getCredits() - 1);
        userRepository.save(user);

        employee.setCreditsAllocated(user.getCredits()); // keep in sync, no extra deduction
        employeeRepository.save(employee);

        validateReturnTripOrThrow(request);
        TravelPlanRequest normalized = normalizeDurationForReturnTrip(request);

        TravelPlan entity = new TravelPlan();
        mapRequestToEntity(normalized, entity);
        entity.setStatus("QUEUED");
        TravelPlan saved = repository.save(entity);
        persistQuestionnaireResponses(normalized, saved);

        planGenerationService.enqueueGeneration(saved.getId(), user.getId());

        return toResponse(saved);
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
        questionnaire.setResponsesJson(request.questionnaireResponses().trim());
        travelPlanQuestionnaireRepository.save(questionnaire);
    }

    public TravelPlanResponse update(Long id, TravelPlanRequest request) {
        TravelPlan entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("TravelPlan not found"));
        validateReturnTripOrThrow(request);
        TravelPlanRequest normalized = normalizeDurationForReturnTrip(request);
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

    private TravelPlanResponse toResponse(TravelPlan entity) {
        return toResponse(entity, null);
    }

    private TravelPlanResponse toResponse(TravelPlan entity, GeneratedPlanPayload generatedPlan) {
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
                generatedPlan);
    }

    private GeneratedPlanPayload toGeneratedPayload(GeneratedPlan g) {
        return new GeneratedPlanPayload(
                g.getStatus(),
                g.getPlanJson(),
                g.getProvider(),
                g.getModelUsed(),
                g.getTokensUsed(),
                g.getProcessingTimeMs(),
                g.getErrorMessage());
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
        } else if (request.userId() != null) {
            User user = userRepository.findById(request.userId())
                    .orElseThrow(() -> new NoSuchElementException("User not found"));
            entity.setUser(user);
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
            if (!d1.isAfter(d0)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Return date must be after departure date");
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
            if (!d1.isAfter(d0)) {
                return request;
            }
            int inclusive = (int) ChronoUnit.DAYS.between(d0, d1) + 1;
            return new TravelPlanRequest(
                    request.destination(),
                    request.country(),
                    inclusive,
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
                    request.userId());
        } catch (Exception ex) {
            return request;
        }
    }

    private static String tripDetailText(JsonNode root, String field) {
        JsonNode n = root.path(field);
        return n.isTextual() ? n.asText() : "";
    }
}
