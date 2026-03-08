package com.TravelMedicineAdvisory.Server.domain.auth;

import com.TravelMedicineAdvisory.Server.domain.user.User;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;
import com.TravelMedicineAdvisory.Server.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setUsername("testuser");

        userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(testUser.getEmail())
                .password(testUser.getPassword())
                .roles("USER")
                .build();
    }

    @Test
    void register_ShouldReturnAuthResponse_WhenValidRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setFirstName("Test");
        request.setLastName("User");

        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("mockJwtToken");
        when(jwtService.getJwtExpiration()).thenReturn(86400000L);

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("mockJwtToken", response.getAccessToken());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("Test", response.getFirstName());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void login_ShouldReturnAuthResponse_WhenCredentialsAreValid() {
        LoginRequest request = new LoginRequest("test@example.com", "password123");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("mockJwtToken");
        when(jwtService.getJwtExpiration()).thenReturn(86400000L);

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("mockJwtToken", response.getAccessToken());
        assertEquals("test@example.com", response.getEmail());
        verify(authenticationManager, times(1)).authenticate(any());
        verify(userRepository, times(1)).save(testUser); // Updates lastLogin
    }
}
