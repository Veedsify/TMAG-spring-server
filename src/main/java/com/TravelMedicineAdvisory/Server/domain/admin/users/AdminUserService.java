package com.TravelMedicineAdvisory.Server.domain.admin.users;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.TravelMedicineAdvisory.Server.core.queue.JobType;
import com.TravelMedicineAdvisory.Server.core.queue.QueueService;
import com.TravelMedicineAdvisory.Server.domain.abuseflag.AbuseFlag;
import com.TravelMedicineAdvisory.Server.domain.abuseflag.AbuseFlagRepository;
import com.TravelMedicineAdvisory.Server.domain.admin.credits.AdminCreditService;
import com.TravelMedicineAdvisory.Server.domain.companyuser.CompanyUser;
import com.TravelMedicineAdvisory.Server.domain.companyuser.CompanyUserRepository;
import com.TravelMedicineAdvisory.Server.domain.credit.Credit;
import com.TravelMedicineAdvisory.Server.domain.credit.CreditRepository;
import com.TravelMedicineAdvisory.Server.domain.employee.Employee;
import com.TravelMedicineAdvisory.Server.domain.employee.EmployeeRepository;
import com.TravelMedicineAdvisory.Server.domain.travelplan.TravelPlanRepository;
import com.TravelMedicineAdvisory.Server.domain.user.AvatarUrlService;
import com.TravelMedicineAdvisory.Server.domain.user.User;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;

@Service
public class AdminUserService {

    private final UserRepository userRepository;
    private final CreditRepository creditRepository;
    private final AdminCreditService adminCreditService;
    private final EmployeeRepository employeeRepository;
    private final CompanyUserRepository companyUserRepository;
    private final AbuseFlagRepository abuseFlagRepository;
    private final TravelPlanRepository travelPlanRepository;
    private final PasswordEncoder passwordEncoder;
    private final QueueService queueService;
    private final AvatarUrlService avatarUrlService;

    public AdminUserService(UserRepository userRepository, CreditRepository creditRepository,
            AdminCreditService adminCreditService, EmployeeRepository employeeRepository,
            CompanyUserRepository companyUserRepository, AbuseFlagRepository abuseFlagRepository,
            TravelPlanRepository travelPlanRepository, PasswordEncoder passwordEncoder,
            QueueService queueService, AvatarUrlService avatarUrlService) {
        this.userRepository = userRepository;
        this.creditRepository = creditRepository;
        this.adminCreditService = adminCreditService;
        this.employeeRepository = employeeRepository;
        this.companyUserRepository = companyUserRepository;
        this.abuseFlagRepository = abuseFlagRepository;
        this.travelPlanRepository = travelPlanRepository;
        this.passwordEncoder = passwordEncoder;
        this.queueService = queueService;
        this.avatarUrlService = avatarUrlService;
    }

    public List<AdminUserResponse> findAll() {
        List<User> users = userRepository.findAllActive();
        return users.stream().map(this::mapToResponse).toList();
    }

    @Transactional
    public AdminUserResponse create(Map<String, Object> body) {
        User user = new User();
        if (body.containsKey("firstName"))
            user.setFirstName((String) body.get("firstName"));
        if (body.containsKey("lastName"))
            user.setLastName((String) body.get("lastName"));
        if (body.containsKey("email"))
            user.setEmail((String) body.get("email"));
        if (body.containsKey("phone"))
            user.setPhone((String) body.get("phone"));
        if (body.containsKey("password")) {
            user.setPassword(passwordEncoder.encode((String) body.get("password")));
        }
        user.setType(body.containsKey("type") ? (String) body.get("type") : "INDIVIDUAL");
        user.setCredits(0);
        user.setIsActive(true);
        user = userRepository.save(user);
        return mapToResponse(user);
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

        if (updates.containsKey("firstName")) {
            user.setFirstName((String) updates.get("firstName"));
        }
        if (updates.containsKey("lastName")) {
            user.setLastName((String) updates.get("lastName"));
        }
        if (updates.containsKey("email")) {
            user.setEmail((String) updates.get("email"));
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

    /**
     * Sets the user's remaining credit balance to {@code targetRemaining}.
     * The admin UI sends the desired balance (not a delta); ledger entries are
     * written via
     * {@link AdminCreditService#adjustCredits(Map)}.
     */
    @Transactional
    public void resetCredits(Long id, Integer targetRemaining) {
        if (targetRemaining == null) {
            throw new IllegalArgumentException("amount is required");
        }
        if (targetRemaining < 0) {
            throw new IllegalArgumentException("amount must be >= 0");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        int current = user.getCredits() != null ? user.getCredits() : 0;
        int delta = targetRemaining - current;
        if (delta == 0) {
            return;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("userId", id);
        body.put("amount", delta);
        body.put("reason", "Admin set remaining credits to " + targetRemaining);
        body.put("action", delta > 0 ? "admin_add" : "admin_deduct");
        adminCreditService.adjustCredits(body);
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

        // Get unresolved abuse flags for this user
        List<String> riskFlags = abuseFlagRepository.findAll().stream()
                .filter(f -> f.getUser() != null && f.getUser().getId().equals(user.getId()))
                .filter(f -> f.getResolved() == null || !f.getResolved())
                .map(AbuseFlag::getType)
                .toList();

        // Get actual plans generated count
        int plansGenerated = (int) travelPlanRepository.countByUserId(user.getId());

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
                plansGenerated,
                user.getLastLogin(),
                status,
                riskFlags,
                user.getCreatedAt(),
                avatarUrlService.toFullUrl(user.getAvatarUrl()),
                null,
                null);
    }
}
