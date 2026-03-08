package com.TravelMedicineAdvisory.Server.security;

import com.TravelMedicineAdvisory.Server.domain.rolepermission.RolePermission;
import com.TravelMedicineAdvisory.Server.domain.rolepermission.RolePermissionRepository;
import com.TravelMedicineAdvisory.Server.domain.user.User;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RolePermissionRepository rolePermissionRepository;

    public CustomUserDetailsService(UserRepository userRepository, RolePermissionRepository rolePermissionRepository) {
        this.userRepository = userRepository;
        this.rolePermissionRepository = rolePermissionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        // Add role authority (e.g. ROLE_SUPERADMIN)
        if (user.getRole() != null && user.getRole().getName() != null) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().getName().toUpperCase()));

            // Load all permissions for this role (e.g. users:read, countries:write)
            List<RolePermission> rolePermissions = rolePermissionRepository.findByRoleId(user.getRole().getId());
            for (RolePermission rp : rolePermissions) {
                if (rp.getPermission() != null && rp.getPermission().getName() != null) {
                    authorities.add(new SimpleGrantedAuthority(rp.getPermission().getName()));
                }
            }
        } else {
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword() != null ? user.getPassword() : "")
                .authorities(authorities)
                .build();
    }
}
