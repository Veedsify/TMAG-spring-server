package com.TravelMedicineAdvisory.Server.security;

import com.TravelMedicineAdvisory.Server.domain.companyuser.CompanyUserRepository;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

@Component("perm")
public class PermissionExpressions {

    private final CompanyUserRepository companyUserRepository;
    private final UserRepository userRepository;

    public PermissionExpressions(CompanyUserRepository companyUserRepository, UserRepository userRepository) {
        this.companyUserRepository = companyUserRepository;
        this.userRepository = userRepository;
    }

    public boolean has(Authentication authentication, String... permissions) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        if (hasAuthority(authorities, "all")) {
            return true;
        }

        return Arrays.stream(permissions)
                .filter(Objects::nonNull)
                .anyMatch(permission -> hasAuthority(authorities, permission));
    }

    public boolean admin(Authentication authentication, String... permissions) {
        return hasAnyRole(authentication, "ROLE_SUPERADMIN", "ROLE_ADMINISTRATOR") && has(authentication, permissions);
    }

    public boolean doctor(Authentication authentication, String... permissions) {
        return hasAnyRole(authentication, "ROLE_SUPERADMIN", "ROLE_DOCTOR") && has(authentication, permissions);
    }

    public boolean company(Authentication authentication, Long companyId, String... permissions) {
        if (!has(authentication, permissions)) {
            return false;
        }

        if (hasAuthority(authentication.getAuthorities(), "all")) {
            return true;
        }

        if (companyId == null || !(authentication.getPrincipal() instanceof AppUserDetails userDetails)) {
            return false;
        }

        Long userId = userDetails.getUserId();
        // Fallback: resolve userId from email if not set in principal
        if (userId == null && userDetails.getEmail() != null) {
            userId = userRepository.findByEmail(userDetails.getEmail()).map(com.TravelMedicineAdvisory.Server.domain.user.User::getId).orElse(null);
        }

        if (userId == null) {
            return false;
        }

        return companyUserRepository.existsActiveByUserIdAndCompanyId(userId, companyId);
    }

    private boolean hasAnyRole(Authentication authentication, String... roles) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        if (hasAuthority(authorities, "all")) {
            return true;
        }

        return Arrays.stream(roles).anyMatch(role -> hasAuthority(authorities, role));
    }

    private boolean hasAuthority(Collection<? extends GrantedAuthority> authorities, String authority) {
        return authorities.stream().anyMatch(grantedAuthority -> authority.equals(grantedAuthority.getAuthority()));
    }
}
