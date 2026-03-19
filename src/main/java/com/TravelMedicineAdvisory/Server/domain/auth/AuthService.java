package com.TravelMedicineAdvisory.Server.domain.auth;

import com.TravelMedicineAdvisory.Server.core.queue.JobType;
import com.TravelMedicineAdvisory.Server.core.queue.QueueService;
import com.TravelMedicineAdvisory.Server.domain.company.BillingCurrency;
import com.TravelMedicineAdvisory.Server.domain.credit.Credit;
import com.TravelMedicineAdvisory.Server.domain.credit.CreditRepository;
import com.TravelMedicineAdvisory.Server.domain.role.Role;
import com.TravelMedicineAdvisory.Server.domain.role.RoleRepository;
import com.TravelMedicineAdvisory.Server.domain.user.User;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;
import com.TravelMedicineAdvisory.Server.security.JwtService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final QueueService queueService;
    private final CreditRepository creditRepository;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository,
            PasswordEncoder passwordEncoder, JwtService jwtService,
            AuthenticationManager authenticationManager, UserDetailsService userDetailsService,
            QueueService queueService, CreditRepository creditRepository ) {
        this.userRepository = userRepository;
        this.creditRepository=  creditRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.queueService = queueService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("User already exists");
        }
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("User already exists");
        }

        Role role = determineUserRole();

        Credit newAssignedCredits = new Credit();

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setOnboardingStage(0);
        user.setOnboarded(false);
        user.setVerified(false);
        user.setType("INDIVIDUAL");
        user.setCredits(1);
        user.setBillingCurrency(BillingCurrency.NGN);
        user.setRole(role);
        user.setLastLogin(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        newAssignedCredits.setUser(user);
        newAssignedCredits.setType("new-user-bonus");
        newAssignedCredits.setReference(UUID.randomUUID().toString());
        newAssignedCredits.setBalanceAfter(1);
        newAssignedCredits.setAmount(1);

        creditRepository.save(newAssignedCredits);

        // Token saved synchronously; email dispatched to queue
        dispatchVerificationEmail(savedUser);

        UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getEmail());
        String jwtToken = jwtService.generateToken(Map.of("userId", savedUser.getId()), userDetails);

        return buildAuthResponse(savedUser, jwtToken);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (Boolean.FALSE.equals(user.getVerified())) {
            dispatchVerificationEmail(user);
        }

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String jwtToken = jwtService.generateToken(Map.of("userId", user.getId()), userDetails);

        return buildAuthResponse(user, jwtToken);
    }

    @Transactional
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        String token = generateToken(32);
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        String resetLink = frontendUrl + "/reset-password?token=" + token + "&email=" + email;

        queueService.dispatch(JobType.EMAIL_PASSWORD_RESET, Map.of(
                "to", email,
                "subject", "Reset your password",
                "variables", Map.of(
                        "firstName", user.getFirstName() != null ? user.getFirstName() : "there",
                        "link", resetLink)));
    }

    @Transactional
    public void resetPassword(String email, String token, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        if (user.getResetToken() == null || !user.getResetToken().equals(token)) {
            throw new IllegalArgumentException("Invalid reset token");
        }

        if (user.getResetTokenExpiry() == null || user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Reset token has expired");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);

        queueService.dispatch(JobType.EMAIL_PASSWORD_CHANGED, Map.of(
                "to", email,
                "subject", "Password changed successfully",
                "variables", Map.of(
                        "firstName", user.getFirstName() != null ? user.getFirstName() : "there")));
    }

    @Transactional
    public void resendVerification(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        if (Boolean.TRUE.equals(user.getVerified())) {
            throw new IllegalArgumentException("Email already verified");
        }

        dispatchVerificationEmail(user);
    }

    @Transactional
    public void verifyEmail(String email, String code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getVerificationToken() == null || !user.getVerificationToken().equals(code)) {
            throw new IllegalArgumentException("Invalid verification code");
        }

        if (user.getVerificationTokenExpiry() == null
                || user.getVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.GONE, "Verification code has expired");
        }

        user.setVerified(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiry(null);
        userRepository.save(user);
    }

    @Transactional
    public AuthResponse acceptInvitation(String token, String newPassword) {
        User user = userRepository.findByInvitationToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid invitation token"));

        if (user.getInvitationTokenExpiry() == null
                || user.getInvitationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Invitation has expired");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setVerified(true);
        user.setMustChangePassword(false);
        user.setInvitationToken(null);
        user.setInvitationTokenExpiry(null);
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String jwtToken = jwtService.generateToken(Map.of("userId", user.getId()), userDetails);

        return buildAuthResponse(user, jwtToken);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void dispatchVerificationEmail(User user) {
        String code = generateVerificationCode();
        user.setVerificationToken(code);
        user.setVerificationTokenExpiry(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        queueService.dispatch(JobType.EMAIL_VERIFICATION, Map.of(
                "to", user.getEmail(),
                "subject", "Your verification code",
                "variables", Map.of(
                        "firstName", user.getFirstName() != null ? user.getFirstName() : "there",
                        "code", code)));
    }

    private String generateVerificationCode() {
        int code = new SecureRandom().nextInt(900000) + 100000;
        return String.valueOf(code);
    }

    private String generateToken(int byteLength) {
        byte[] bytes = new byte[byteLength];
        new SecureRandom().nextBytes(bytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private Role determineUserRole() {
        long userCount = userRepository.count();
        if (userCount == 0) {
            return roleRepository.findByName("SuperAdmin")
                    .orElseGet(() -> roleRepository.findByName("Administrator").orElse(null));
        }
        return roleRepository.findByName("Individual").orElse(null);
    }

    private AuthResponse buildAuthResponse(User user, String jwtToken) {
        AuthResponse response = new AuthResponse();
        response.setId(user.getId());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setUsername(user.getUsername());
        response.setPhone(user.getPhone());
        response.setEmail(user.getEmail());

        Map<String, Object> extendedResponse = Map.of(
                "role_id", user.getRole().getId(),
                "role_name", user.getRole().getName());

        if (user.getRole() != null) {
            response.setExtend(extendedResponse);
            response.setRoleId(user.getRole().getId());
            response.setRoleName(user.getRole().getName());
        }

        response.setAvatarUrl(user.getAvatarUrl());
        response.setOnboardingStage(user.getOnboardingStage() != null ? user.getOnboardingStage() : 0);
        response.setIsVerified(user.getVerified() != null ? user.getVerified() : false);
        response.setLastLogin(user.getLastLogin() != null ? user.getLastLogin().toString() : null);
        response.setAccessToken(jwtToken);
        response.setExp(System.currentTimeMillis() + jwtService.getJwtExpiration());
        response.setBillingCurrency(user.getBillingCurrency());
        response.setMustChangePassword(user.getMustChangePassword() != null ? user.getMustChangePassword() : false);

        return response;
    }
}
