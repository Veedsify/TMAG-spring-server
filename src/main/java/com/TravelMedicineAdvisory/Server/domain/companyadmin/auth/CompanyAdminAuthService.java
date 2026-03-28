package com.TravelMedicineAdvisory.Server.domain.companyadmin.auth;

import com.TravelMedicineAdvisory.Server.core.queue.JobType;
import com.TravelMedicineAdvisory.Server.core.queue.QueueService;
import com.TravelMedicineAdvisory.Server.domain.role.Role;
import com.TravelMedicineAdvisory.Server.domain.user.User;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;
import com.TravelMedicineAdvisory.Server.domain.companyuser.CompanyUser;
import com.TravelMedicineAdvisory.Server.domain.companyuser.CompanyUserRepository;
import com.TravelMedicineAdvisory.Server.security.JwtService;
import com.TravelMedicineAdvisory.Server.domain.rolepermission.RolePermission;
import com.TravelMedicineAdvisory.Server.domain.rolepermission.RolePermissionRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CompanyAdminAuthService {

    private final UserRepository userRepository;
    private final CompanyUserRepository companyUserRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final ObjectMapper objectMapper;
    private final QueueService queueService;

    public CompanyAdminAuthService(UserRepository userRepository,
                                   CompanyUserRepository companyUserRepository,
                                   RolePermissionRepository rolePermissionRepository,
                                   JwtService jwtService,
                                   AuthenticationManager authenticationManager,
                                   UserDetailsService userDetailsService,
                                   ObjectMapper objectMapper,
                                   QueueService queueService) {
        this.userRepository = userRepository;
        this.companyUserRepository = companyUserRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.objectMapper = objectMapper;
        this.queueService = queueService;
    }

    public Map<String, Object> login(String email, String password) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (user.getRole() == null) {
            throw new RuntimeException("User does not have the required permissions");
        }

        if (!hasCompanyAdminAccess(user)) {
            throw new RuntimeException("Access denied. SuperAdmin or Administrator privileges required.");
        }

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        String firstName = user.getFirstName() != null ? user.getFirstName() : "there";
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm:ss"));
        queueService.dispatch(JobType.EMAIL_LOGIN_ALERT, Map.of(
                "to", user.getEmail(),
                "subject", "New login to your TMAG account",
                "variables", Map.of(
                        "firstName", firstName,
                        "location", "Unknown",
                        "device", "Web Browser",
                        "timestamp", timestamp)));

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        String jwtToken = jwtService.generateToken(userDetails);

        Map<String, Object> response = new HashMap<>();
        response.put("token", jwtToken);
        response.put("exp", System.currentTimeMillis() + jwtService.getJwtExpiration());
        response.put("user", mapUserToResponse(user));

        return response;
    }

    private boolean hasCompanyAdminAccess(User user) {
        Role role = user.getRole();
        if (role == null) return false;

        String roleName = role.getName();

        // SuperAdmin and Administrator roles have direct access
        if ("SuperAdmin".equalsIgnoreCase(roleName) || "Administrator".equalsIgnoreCase(roleName)) {
            return true;
        }

        // Check for "all" permission in role's JSON permissions field
        if (role.getPermissions() != null) {
            try {
                List<String> permissions = objectMapper.readValue(role.getPermissions(),
                        new TypeReference<List<String>>() {});
                if (permissions.contains("all")) {
                    return true;
                }
            } catch (Exception e) {
            }
        }

        // Check for "all" permission via role-permission junction table
        List<RolePermission> rolePermissions = rolePermissionRepository.findByRoleId(role.getId());
        for (RolePermission rp : rolePermissions) {
            if (rp.getPermission() != null && "all".equals(rp.getPermission().getName())) {
                return true;
            }
        }

        return false;
    }

    public void logout() {
    }

    public Map<String, Object> getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapUserToResponse(user);
    }

    private Map<String, Object> mapUserToResponse(User user) {
        if (user == null) {
            return new HashMap<>();
        }

        List<String> permissions = resolvePermissions(user);

        String role = resolveRole(user, permissions);

        // Include company info if the user belongs to a company
        List<CompanyUser> companyLinks = companyUserRepository.findAllByUser(user);
        List<Map<String, Object>> companies = companyLinks.stream()
                .filter(cu -> cu.getCompany() != null)
                .map(cu -> {
                    Map<String, Object> c = new HashMap<>();
                    c.put("id", cu.getCompany().getId());
                    c.put("name", cu.getCompany().getName());
                    c.put("companyRole", cu.getRole());
                    return c;
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("name", user.getName() != null ? user.getName() : user.getEmail());
        response.put("email", user.getEmail());
        response.put("role", role);
        response.put("status", user.getDeletedAt() != null ? "inactive" : "active");
        response.put("lastLogin", user.getLastLogin());
        response.put("createdAt", user.getCreatedAt());
        response.put("permissions", permissions);
        response.put("companies", companies);

        return response;
    }

    private List<String> resolvePermissions(User user) {
        if (user.getRole() != null && user.getRole().getPermissions() != null) {
            try {
                List<String> perms = objectMapper.readValue(user.getRole().getPermissions(),
                        new TypeReference<List<String>>() {});
                if (!perms.isEmpty()) return perms;
            } catch (Exception e) {
            }
        }

        if (user.getRole() != null) {
            try {
                return rolePermissionRepository.findByRoleId(user.getRole().getId()).stream()
                        .map(rp -> rp.getPermission() != null ? rp.getPermission().getName() : null)
                        .filter(p -> p != null)
                        .collect(Collectors.toList());
            } catch (Exception e) {
            }
        }

        return List.of();
    }

    private String resolveRole(User user, List<String> permissions) {
        String roleName = user.getRole() != null ? user.getRole().getName() : "";
        if (permissions.contains("all") || "SuperAdmin".equalsIgnoreCase(roleName)) {
            return "super_admin";
        } else if ("Administrator".equalsIgnoreCase(roleName)) {
            return "client_admin";
        } else if (permissions.contains("users.write")) {
            return "client_admin";
        }
        return "support_admin";
    }
}
