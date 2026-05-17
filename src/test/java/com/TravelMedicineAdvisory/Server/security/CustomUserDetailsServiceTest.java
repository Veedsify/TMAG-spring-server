package com.TravelMedicineAdvisory.Server.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import com.TravelMedicineAdvisory.Server.domain.rolepermission.RolePermissionRepository;
import com.TravelMedicineAdvisory.Server.domain.user.User;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

class CustomUserDetailsServiceTest {

    @Test
    void inactiveUsersLoadAsDisabledForAuthenticationAndJwtRequests() {
        User user = new User();
        user.setId(1L);
        user.setEmail("disabled@example.com");
        user.setPassword("encoded");
        user.setIsActive(false);

        UserRepository userRepository = mock(UserRepository.class);
        when(userRepository.findByEmail("disabled@example.com")).thenReturn(Optional.of(user));

        CustomUserDetailsService service = new CustomUserDetailsService(
                userRepository,
                mock(RolePermissionRepository.class),
                new ObjectMapper());

        UserDetails details = service.loadUserByUsername("disabled@example.com");

        assertThat(details.isEnabled()).isFalse();
    }
}
