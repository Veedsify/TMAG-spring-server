package com.TravelMedicineAdvisory.Server.domain.admin.auth;

import com.TravelMedicineAdvisory.Server.domain.role.Role;
import com.TravelMedicineAdvisory.Server.domain.user.User;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;
import com.TravelMedicineAdvisory.Server.security.JwtService;
import com.TravelMedicineAdvisory.Server.domain.rolepermission.RolePermission;
import com.TravelMedicineAdvisory.Server.domain.rolepermission.RolePermissionRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminAuthService {

    private final UserRepository userRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final ObjectMapper objectMapper;

    public AdminAuthService(UserRepository userRepository,
            RolePermissionRepository rolePermissionRepository,
            JwtService jwtService,
            AuthenticationManager authenticationManager,
            UserDetailsService userDetailsService, ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> login(String email, String password) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (user.getRole() == null) {
            throw new RuntimeException("User does not have admin permissions");
        }

        if (!isSuperAdmin(user)) {
            throw new RuntimeException("Access denied. Super admin privileges required.");
        }

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        String jwtToken = jwtService.generateToken(userDetails);

        Map<String, Object> response = new HashMap<>();
        response.put("token", jwtToken);
        response.put("exp", System.currentTimeMillis() + jwtService.getJwtExpiration());
        response.put("user", mapUserToResponse(user));

        return response;
    }

    private boolean isSuperAdmin(User user) {
        Role role = user.getRole();
        if (role == null)
            return false;

        // Direct SuperAdmin role name check
        if ("SuperAdmin".equalsIgnoreCase(role.getName())) {
            return true;
        }

        // Check for "all" permission in role's JSON permissions field
        if (role.getPermissions() != null) {
            try {
                List<String> permissions = objectMapper.readValue(role.getPermissions(),
                        new TypeReference<List<String>>() {
                        });
                if (permissions.contains("all")) {
                    return true;
                }
            } catch (Exception e) {
            }
        }

        // Check for "all" permission in role-permission junction table
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

        List<String> permissions = List.of();
        if (user.getRole() != null && user.getRole().getPermissions() != null) {
            try {
                permissions = objectMapper.readValue(user.getRole().getPermissions(),
                        new TypeReference<List<String>>() {
                        });
            } catch (Exception e) {
                permissions = List.of();
            }
        }

        if (permissions.isEmpty() && user.getRole() != null) {
            try {
                List<RolePermission> rolePermissions = rolePermissionRepository.findByRoleId(user.getRole().getId());
                permissions = rolePermissions.stream()
                        .map(rp -> rp.getPermission() != null ? rp.getPermission().getName() : null)
                        .filter(p -> p != null)
                        .collect(Collectors.toList());
            } catch (Exception e) {
                permissions = List.of();
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("name",
                user.getFirstName() != null ? user.getFirstName() + " " + user.getLastName() : user.getEmail());
        response.put("email", user.getEmail());
        response.put("role", "super_admin");
        response.put("status", user.getDeletedAt() != null ? "inactive" : "active");
        response.put("lastLogin", user.getLastLogin());
        response.put("createdAt", user.getCreatedAt());
        response.put("permissions", permissions);

        return response;
    }
}
