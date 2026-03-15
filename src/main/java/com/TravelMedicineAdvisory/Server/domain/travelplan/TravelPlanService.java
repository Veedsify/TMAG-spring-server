package com.TravelMedicineAdvisory.Server.domain.travelplan;

import java.util.NoSuchElementException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.TravelMedicineAdvisory.Server.domain.company.Company;
import com.TravelMedicineAdvisory.Server.domain.company.CompanyRepository;
import com.TravelMedicineAdvisory.Server.domain.employee.Employee;
import com.TravelMedicineAdvisory.Server.domain.employee.EmployeeRepository;
import com.TravelMedicineAdvisory.Server.domain.user.User;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;

@Service
@Transactional
public class TravelPlanService {

    private final TravelPlanRepository repository;
    private final CompanyRepository companyRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;

    public TravelPlanService(TravelPlanRepository repository, CompanyRepository companyRepository, EmployeeRepository employeeRepository, UserRepository userRepository) {
        this.repository = repository;
        this.companyRepository = companyRepository;
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
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
        return toResponse(entity);
    }

    @Transactional
    public TravelPlanResponse create(TravelPlanRequest request) {
        Long currentUserId = request.userId();
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getCredits() < 1) {
            throw new RuntimeException("Insufficient credits");
        }

        user.setCredits(user.getCredits() - 1);
        userRepository.save(user);

        TravelPlan entity = new TravelPlan();
        mapRequestToEntity(request, entity);
        entity.setStatus(String.valueOf(Travel_Plan.PENDING));

        TravelPlan saved = repository.save(entity);
        return toResponse(saved);
    }

    public TravelPlanResponse update(Long id, TravelPlanRequest request) {
        TravelPlan entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("TravelPlan not found"));
        mapRequestToEntity(request, entity);
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
        return new TravelPlanResponse(
            entity.getId(),
            entity.getDestination(),
            entity.getCountry(),
            entity.getDuration(),
            entity.getPurpose(),
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
            entity.getUpdatedAt()
        );
    }

    private void mapRequestToEntity(TravelPlanRequest request, TravelPlan entity) {
        entity.setDestination(request.destination());
        entity.setCountry(request.country());
        entity.setDuration(request.duration());
        entity.setPurpose(request.purpose());
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
            entity.setEmployee(employee);
        }
        if (request.userId() != null) {
            User user = userRepository.findById(request.userId())
                    .orElseThrow(() -> new NoSuchElementException("User not found"));
            entity.setUser(user);
        }
    }
}
