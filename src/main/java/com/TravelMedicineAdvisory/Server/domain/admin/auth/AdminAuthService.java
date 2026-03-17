package com.TravelMedicineAdvisory.Server.domain.admin.auth;

import com.TravelMedicineAdvisory.Server.domain.role.Role;
import com.TravelMedicineAdvisory.Server.domain.role.RoleRepository;
import com.TravelMedicineAdvisory.Server.domain.user.User;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;
import com.TravelMedicineAdvisory.Server.security.JwtService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminAuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final ObjectMapper objectMapper;

    public AdminAuthService(UserRepository userRepository, RoleRepository roleRepository,
                          PasswordEncoder passwordEncoder, JwtService jwtService,
                          AuthenticationManager authenticationManager,
                          UserDetailsService userDetailsService, ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
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

    public void logout() {
    }

    public Map<String, Object> getCurrentUser() {
        return mapUserToResponse(null);
    }

    private Map<String, Object> mapUserToResponse(User user) {
        if (user == null) {
            return new HashMap<>();
        }

        List<String> permissions = List.of();
        if (user.getRole() != null && user.getRole().getPermissions() != null) {
            try {
                permissions = objectMapper.readValue(user.getRole().getPermissions(),
                        new TypeReference<List<String>>() {});
            } catch (Exception e) {
                permissions = List.of();
            }
        }

        String role = "support_admin";
        if (permissions.contains("all")) {
            role = "super_admin";
        } else if (permissions.contains("users.write")) {
            role = "client_admin";
        }

        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("name", user.getName() != null ? user.getName() : user.getEmail());
        response.put("email", user.getEmail());
        response.put("role", role);
        response.put("status", user.getDeletedAt() != null ? "inactive" : "active");
        response.put("lastLogin", user.getLastLogin());
        response.put("createdAt", user.getCreatedAt());
        response.put("permissions", permissions);

        return response;
    }
}
