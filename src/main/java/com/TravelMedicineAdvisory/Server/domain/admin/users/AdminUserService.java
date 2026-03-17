package com.TravelMedicineAdvisory.Server.domain.admin.users;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.TravelMedicineAdvisory.Server.core.queue.JobType;
import com.TravelMedicineAdvisory.Server.core.queue.QueueService;
import com.TravelMedicineAdvisory.Server.domain.companyuser.CompanyUser;
import com.TravelMedicineAdvisory.Server.domain.companyuser.CompanyUserRepository;
import com.TravelMedicineAdvisory.Server.domain.credit.Credit;
import com.TravelMedicineAdvisory.Server.domain.credit.CreditRepository;
import com.TravelMedicineAdvisory.Server.domain.employee.Employee;
import com.TravelMedicineAdvisory.Server.domain.employee.EmployeeRepository;
import com.TravelMedicineAdvisory.Server.domain.user.User;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;

@Service
public class AdminUserService {

    private final UserRepository userRepository;
    private final CreditRepository creditRepository;
    private final EmployeeRepository employeeRepository;
    private final CompanyUserRepository companyUserRepository;
    private final QueueService queueService;

    public AdminUserService(UserRepository userRepository, CreditRepository creditRepository,
            EmployeeRepository employeeRepository,
            CompanyUserRepository companyUserRepository, QueueService queueService) {
        this.userRepository = userRepository;
        this.creditRepository = creditRepository;
        this.employeeRepository = employeeRepository;
        this.companyUserRepository = companyUserRepository;
        this.queueService = queueService;
    }

    public List<AdminUserResponse> findAll() {
        List<User> users = userRepository.findAllActive();
        return users.stream().map(this::mapToResponse).toList();
    }

    public AdminUserResponse findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapToResponse(user);
    }

    @Transactional
    public AdminUserResponse update(Long id, Map<String, Object> updates) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (updates.containsKey("name")) {
            user.setName((String) updates.get("name"));
        }
        if (updates.containsKey("phone")) {
            user.setPhone((String) updates.get("phone"));
        }

        user = userRepository.save(user);
        return mapToResponse(user);
    }

    @Transactional
    public void suspend(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Transactional
    public void activate(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setDeletedAt(null);
        userRepository.save(user);
    }

    @Transactional
    public void resetCredits(Long id, Integer amount) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Integer currentCredits = user.getCredits() != null ? user.getCredits() : 0;

        Credit credit = new Credit();
        credit.setUser(user);
        credit.setAmount(amount);
        credit.setType("admin_add");
        credit.setBalanceAfter(currentCredits + amount);
        credit.setReference("Admin credit reset for user " + id);

        creditRepository.save(credit);

        user.setCredits(currentCredits + amount);
        userRepository.save(user);
    }

    public void resetPassword(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Object> payload = new HashMap<>();
        payload.put("email", user.getEmail());

        queueService.dispatch(JobType.EMAIL_PASSWORD_RESET, payload);
    }

    @Transactional
    public void delete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        userRepository.delete(user);
    }

    private AdminUserResponse mapToResponse(User user) {
        String planType = "individual";
        Long companyId = null;
        String companyName = null;
        List<String> riskFlags = new ArrayList<>();

        if ("COMPANY".equals(user.getType())) {
            planType = "corporate";
            Optional<Employee> employee = employeeRepository.findByUser(user);
            if (employee.isPresent()) {
                companyId = employee.get().getCompany().getId();
                companyName = employee.get().getCompany().getName();
            } else {
                Optional<CompanyUser> companyUser = companyUserRepository.findByUser(user);
                if (companyUser.isPresent()) {
                    companyId = companyUser.get().getCompany().getId();
                    companyName = companyUser.get().getCompany().getName();
                }
            }
        }

        Integer creditsUsed = 0;
        Integer creditsRemaining = user.getCredits() != null ? user.getCredits() : 0;

        List<Credit> creditLedger = creditRepository.findLedgerByUserId(user.getId());
        for (Credit credit : creditLedger) {
            if ("consume".equals(credit.getType())) {
                creditsUsed += Math.abs(credit.getAmount());
            }
        }

        String role = "individual";
        if (user.getRole() != null) {
            role = user.getRole().getName().toLowerCase();
        }

        String status = user.getDeletedAt() != null ? "suspended" : "active";

        String name = user.getName();
        if (name == null || name.isEmpty()) {
            name = (user.getFirstName() != null ? user.getFirstName() : "")
                    + (user.getLastName() != null ? " " + user.getLastName() : "");
            name = name.trim();
        }
        if (name.isEmpty()) {
            name = user.getEmail();
        }

        return new AdminUserResponse(
                user.getId(),
                name,
                user.getEmail(),
                user.getPhone(),
                role,
                planType,
                companyId,
                companyName,
                creditsUsed,
                creditsRemaining,
                0,
                user.getLastLogin(),
                status,
                riskFlags,
                user.getCreatedAt(),
                user.getAvatarUrl(),
                null,
                null
        );
    }
}
