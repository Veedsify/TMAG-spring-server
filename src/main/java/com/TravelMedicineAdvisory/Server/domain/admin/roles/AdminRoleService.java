package com.TravelMedicineAdvisory.Server.domain.admin.roles;

import com.TravelMedicineAdvisory.Server.domain.role.Role;
import com.TravelMedicineAdvisory.Server.domain.role.RoleRepository;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AdminRoleService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public AdminRoleService(RoleRepository roleRepository, UserRepository userRepository,
            ObjectMapper objectMapper) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    public List<AdminRoleResponse> findAll() {
        List<Role> roles = roleRepository.findAll();
        return roles.stream().map(this::mapToResponse).toList();
    }

    public AdminRoleResponse findById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        return mapToResponse(role);
    }

    @Transactional
    public AdminRoleResponse create(Map<String, Object> body) {
        Role role = new Role();

        if (body.containsKey("name")) {
            role.setName((String) body.get("name"));
        }
        if (body.containsKey("permissions")) {
            Object permissionsObj = body.get("permissions");
            if (permissionsObj instanceof List) {
                try {
                    String permissionsJson = objectMapper.writeValueAsString(permissionsObj);
                    role.setPermissions(permissionsJson);
                } catch (Exception e) {
                    role.setPermissions("[]");
                }
            } else {
                role.setPermissions((String) permissionsObj);
            }
        }

        role = roleRepository.save(role);
        return mapToResponse(role);
    }

    @Transactional
    public AdminRoleResponse update(Long id, Map<String, Object> updates) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        if (updates.containsKey("name")) {
            role.setName((String) updates.get("name"));
        }
        if (updates.containsKey("permissions")) {
            Object permissionsObj = updates.get("permissions");
            if (permissionsObj instanceof List) {
                try {
                    String permissionsJson = objectMapper.writeValueAsString(permissionsObj);
                    role.setPermissions(permissionsJson);
                } catch (Exception e) {
                    role.setPermissions("[]");
                }
            } else {
                role.setPermissions((String) permissionsObj);
            }
        }

        role = roleRepository.save(role);
        return mapToResponse(role);
    }

    @Transactional
    public void delete(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        roleRepository.delete(role);
    }

    private AdminRoleResponse mapToResponse(Role role) {
        List<String> permissions = new ArrayList<>();
        if (role.getPermissions() != null && !role.getPermissions().isEmpty()) {
            try {
                permissions = objectMapper.readValue(role.getPermissions(),
                        new TypeReference<List<String>>() {
                });
            } catch (Exception e) {
                permissions = new ArrayList<>();
            }
        }

        long userCount = userRepository.findAll().stream()
                .filter(u -> u.getRole() != null && u.getRole().getId().equals(role.getId()))
                .count();

        String description = role.getName() + " role";

        return new AdminRoleResponse(
                role.getId(),
                role.getName(),
                description,
                permissions,
                (int) userCount
        );
    }
}
