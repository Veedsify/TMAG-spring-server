package com.TravelMedicineAdvisory.Server.domain.admin.adminusers;

import com.TravelMedicineAdvisory.Server.domain.role.Role;
import com.TravelMedicineAdvisory.Server.domain.role.RoleRepository;
import com.TravelMedicineAdvisory.Server.domain.user.User;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AdminAdminUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    public AdminAdminUserService(UserRepository userRepository, RoleRepository roleRepository,
                                PasswordEncoder passwordEncoder, ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
    }

    public List<AdminAdminUserResponse> findAll() {
        List<Role> adminRoles = roleRepository.findAll().stream()
                .filter(r -> r.getPermissions() != null && r.getPermissions().contains("all"))
                .toList();
        
        List<User> adminUsers = new ArrayList<>();
        for (Role role : adminRoles) {
            adminUsers.addAll(userRepository.findAll().stream()
                    .filter(u -> u.getRole() != null && u.getRole().getId().equals(role.getId()))
                    .toList());
        }
        
        return adminUsers.stream().map(this::mapToResponse).toList();
    }

    public AdminAdminUserResponse findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Admin user not found"));
        return mapToResponse(user);
    }

    @Transactional
    public AdminAdminUserResponse create(Map<String, Object> body) {
        User user = new User();
        
        if (body.containsKey("email")) {
            user.setEmail((String) body.get("email"));
        }
        if (body.containsKey("name")) {
            user.setName((String) body.get("name"));
        }
        if (body.containsKey("firstName")) {
            user.setFirstName((String) body.get("firstName"));
        }
        if (body.containsKey("lastName")) {
            user.setLastName((String) body.get("lastName"));
        }
        if (body.containsKey("password")) {
            user.setPassword(passwordEncoder.encode((String) body.get("password")));
        }
        if (body.containsKey("roleId")) {
            Long roleId = ((Number) body.get("roleId")).longValue();
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new RuntimeException("Role not found"));
            user.setRole(role);
        }
        
        user = userRepository.save(user);
        return mapToResponse(user);
    }

    @Transactional
    public AdminAdminUserResponse update(Long id, Map<String, Object> updates) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Admin user not found"));
        
        if (updates.containsKey("name")) {
            user.setName((String) updates.get("name"));
        }
        if (updates.containsKey("firstName")) {
            user.setFirstName((String) updates.get("firstName"));
        }
        if (updates.containsKey("lastName")) {
            user.setLastName((String) updates.get("lastName"));
        }
        if (updates.containsKey("email")) {
            user.setEmail((String) updates.get("email"));
        }
        if (updates.containsKey("roleId")) {
            Long roleId = ((Number) updates.get("roleId")).longValue();
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new RuntimeException("Role not found"));
            user.setRole(role);
        }
        
        user = userRepository.save(user);
        return mapToResponse(user);
    }

    @Transactional
    public void delete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Admin user not found"));
        userRepository.delete(user);
    }

    private AdminAdminUserResponse mapToResponse(User user) {
        String role = "super_admin";
        List<String> permissions = new ArrayList<>();
        
        if (user.getRole() != null) {
            role = user.getRole().getName().toLowerCase();
            if (user.getRole().getPermissions() != null) {
                try {
                    permissions = objectMapper.readValue(user.getRole().getPermissions(),
                            new TypeReference<List<String>>() {});
                } catch (Exception e) {
                    permissions = new ArrayList<>();
                }
            }
        }
        
        if (permissions.contains("all")) {
            role = "super_admin";
        }
        
        String status = user.getDeletedAt() != null ? "inactive" : "active";
        
        String name = user.getName();
        if (name == null || name.isEmpty()) {
            name = (user.getFirstName() != null ? user.getFirstName() : "")
                    + (user.getLastName() != null ? " " + user.getLastName() : "");
            name = name.trim();
        }
        if (name == null || name.isEmpty()) {
            name = user.getEmail();
        }

        return new AdminAdminUserResponse(
            user.getId(),
            name,
            user.getFirstName(),
            user.getLastName(),
            user.getEmail(),
            role,
            status,
            user.getLastLogin(),
            user.getCreatedAt(),
            permissions
        );
    }
}
