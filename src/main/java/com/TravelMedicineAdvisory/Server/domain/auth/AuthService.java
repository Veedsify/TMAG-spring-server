package com.TravelMedicineAdvisory.Server.domain.auth;

import com.TravelMedicineAdvisory.Server.core.notifications.AdminNotificationService;
import com.TravelMedicineAdvisory.Server.core.queue.JobType;
import com.TravelMedicineAdvisory.Server.core.queue.QueueService;
import com.TravelMedicineAdvisory.Server.domain.company.BillingCurrency;
import com.TravelMedicineAdvisory.Server.domain.companyuser.CompanyUser;
import com.TravelMedicineAdvisory.Server.domain.companyuser.CompanyUserRepository;
import com.TravelMedicineAdvisory.Server.domain.credit.Credit;
import com.TravelMedicineAdvisory.Server.domain.credit.CreditRepository;
import com.TravelMedicineAdvisory.Server.domain.role.Role;
import com.TravelMedicineAdvisory.Server.domain.role.RoleRepository;
import com.TravelMedicineAdvisory.Server.domain.user.User;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;
import com.TravelMedicineAdvisory.Server.domain.usersetting.UserSettingService;
import com.TravelMedicineAdvisory.Server.domain.usersetting.UserSettingResponse;
import com.TravelMedicineAdvisory.Server.domain.creditplan.CreditPlan;
import com.TravelMedicineAdvisory.Server.domain.creditplan.CreditPlanCode;
import com.TravelMedicineAdvisory.Server.domain.creditplan.CreditPlanRepository;
import com.TravelMedicineAdvisory.Server.domain.creditplan.CreditPlanResponse;
import com.TravelMedicineAdvisory.Server.security.JwtService;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
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
    private final CompanyUserRepository companyUserRepository;
    private final AdminNotificationService adminNotificationService;
    private final CreditPlanRepository userCreditPlanRepository;
    private final UserSettingService userSettingService;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${google.client-id:}")
    private String googleClientId;

    @Value("${google.client-secret:}")
    private String googleClientSecret;

    @Value("${google.redirect-uri:}")
    private String googleRedirectUri;

    private final WebClient webClient = WebClient.create();

    public AuthService(UserRepository userRepository, RoleRepository roleRepository,
            PasswordEncoder passwordEncoder, JwtService jwtService,
            AuthenticationManager authenticationManager, UserDetailsService userDetailsService,
            QueueService queueService, CreditRepository creditRepository,
            CompanyUserRepository companyUserRepository,
            AdminNotificationService adminNotificationService,
            CreditPlanRepository userCreditPlanRepository,
            UserSettingService userSettingService) {
        this.userRepository = userRepository;
        this.creditRepository = creditRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.queueService = queueService;
        this.companyUserRepository = companyUserRepository;
        this.adminNotificationService = adminNotificationService;
        this.userCreditPlanRepository = userCreditPlanRepository;
        this.userSettingService = userSettingService;

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
        Optional<CreditPlan> userCreditPlan = userCreditPlanRepository.findByCode(CreditPlanCode.ESSENTIAL);

        if (userCreditPlan.isEmpty()) {
            throw new IllegalArgumentException("Plan Does'nt Exist");
        }

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
        user.setCreditPlan(userCreditPlan.get());
        user.setBillingCurrency(
                request.getBillingCurrency() != null ? request.getBillingCurrency() : BillingCurrency.NGN);
        user.setRole(role);
        user.setLastLogin(LocalDateTime.now());
        user.setCreditPlan(resolveCreditPlan(request.getPlanCode()));

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

        sendLoginAlertEmail(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String jwtToken = jwtService.generateToken(Map.of("userId", user.getId()), userDetails);

        return buildAuthResponse(user, jwtToken);
    }

    private void sendLoginAlertEmail(User user) {
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
    }

    /**
     * Not cached: each call embeds a fresh OAuth {@code state} token for CSRF
     * protection.
     */
    public String googleAuthUrl() {
        if (googleClientId == null || googleClientId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Google sign-in is not configured");
        }

        String scope = URLEncoder.encode("openid email profile", StandardCharsets.UTF_8);
        String redirectUri = URLEncoder.encode(googleRedirectUri, StandardCharsets.UTF_8);
        String state = generateToken(16);

        return "https://accounts.google.com/o/oauth2/v2/auth"
                + "?client_id=" + googleClientId
                + "&redirect_uri=" + redirectUri
                + "&response_type=code"
                + "&scope=" + scope
                + "&access_type=offline"
                + "&state=" + state
                + "&prompt=select_account";
    }

    @Transactional
    public AuthResponse googleCallback(String code, String planCode) {
        if (googleClientId == null || googleClientId.isBlank() || googleClientSecret == null
                || googleClientSecret.isBlank()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Google sign-in is not configured");
        }

        try {
            // Exchange authorization code for tokens
            Map<String, Object> tokenResponse = webClient.post()
                    .uri("https://oauth2.googleapis.com/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue("code=" + URLEncoder.encode(code, StandardCharsets.UTF_8)
                            + "&client_id=" + URLEncoder.encode(googleClientId, StandardCharsets.UTF_8)
                            + "&client_secret=" + URLEncoder.encode(googleClientSecret, StandardCharsets.UTF_8)
                            + "&redirect_uri=" + URLEncoder.encode(googleRedirectUri, StandardCharsets.UTF_8)
                            + "&grant_type=authorization_code")
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (tokenResponse == null || tokenResponse.get("id_token") == null) {
                throw new IllegalArgumentException("Failed to exchange Google authorization code");
            }

            String idTokenString = (String) tokenResponse.get("id_token");

            // Verify the ID token
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                throw new IllegalArgumentException("Invalid Google ID token");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String googleUserId = payload.getSubject();
            String email = (String) payload.getEmail();
            String firstName = (String) payload.get("given_name");
            String lastName = (String) payload.get("family_name");
            String pictureUrl = (String) payload.get("picture");
            boolean emailVerified = payload.getEmailVerified();

            // Find or create user
            User user = userRepository.findByProviderAndProviderId("google", googleUserId).orElse(null);

            if (user == null) {
                user = userRepository.findByEmail(email).orElse(null);
                if (user != null) {
                    user.setProvider("google");
                    user.setProviderId(googleUserId);
                }
            }

            if (user == null) {
                Role role = determineUserRole();

                user = new User();
                user.setFirstName(firstName);
                user.setLastName(lastName);
                user.setName((firstName != null ? firstName : "") + " " + (lastName != null ? lastName : ""));
                user.setEmail(email);
                user.setUsername(email.split("@")[0] + googleUserId.substring(0, Math.min(4, googleUserId.length())));
                user.setPassword(null);
                user.setProvider("google");
                user.setProviderId(googleUserId);
                user.setOnboardingStage(0);
                user.setOnboarded(false);
                user.setVerified(emailVerified);
                user.setType("INDIVIDUAL");
                user.setCredits(1);
                user.setBillingCurrency(BillingCurrency.NGN);
                user.setRole(role);
                user.setLastLogin(LocalDateTime.now());
                user.setCreditPlan(resolveCreditPlan(planCode));
                if (pictureUrl != null) {
                    user.setAvatarUrl(pictureUrl);
                }

                user = userRepository.save(user);

                Credit newAssignedCredits = new Credit();
                newAssignedCredits.setUser(user);
                newAssignedCredits.setType("new-user-bonus");
                newAssignedCredits.setReference(UUID.randomUUID().toString());
                newAssignedCredits.setBalanceAfter(1);
                newAssignedCredits.setAmount(1);
                creditRepository.save(newAssignedCredits);
            } else {
                user.setLastLogin(LocalDateTime.now());
                if (pictureUrl != null && user.getAvatarUrl() == null) {
                    user.setAvatarUrl(pictureUrl);
                }
                if (user.getProvider() == null) {
                    user.setProvider("google");
                    user.setProviderId(googleUserId);
                }
                user = userRepository.save(user);
            }

            UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
            String jwtToken = jwtService.generateToken(Map.of("userId", user.getId()), userDetails);

            return buildAuthResponse(user, jwtToken);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Google authentication failed: " + e.getMessage());
        }
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

        sendInvitationAcceptedEmail(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String jwtToken = jwtService.generateToken(Map.of("userId", user.getId()), userDetails);

        return buildAuthResponse(user, jwtToken);
    }

    private void sendInvitationAcceptedEmail(User user) {
        var companyUserLinks = companyUserRepository.findAllByUser(user);

        for (CompanyUser link : companyUserLinks) {
            if (link.getCompany() != null) {
                String userName = user.getName() != null ? user.getName() : user.getEmail();
                adminNotificationService.notifyCompanyAdmins(
                        link.getCompany().getId(),
                        userName + " has joined " + link.getCompany().getName() + " on TMAG",
                        JobType.EMAIL_INVITATION_ACCEPTED,
                        Map.of("employeeName", userName));
            }
        }
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

    private CreditPlan resolveCreditPlan(String planCode) {
        if (planCode != null && !planCode.isBlank()) {
            try {
                CreditPlanCode code = CreditPlanCode.valueOf(planCode);
                CreditPlan requested = userCreditPlanRepository.findByCode(code).orElse(null);
                if (requested != null) {
                    return requested;
                }
            } catch (IllegalArgumentException ignored) {
                // Invalid plan code, fall through to default
            }
        }
        return resolveDefaultCreditPlan();
    }

    private CreditPlan resolveDefaultCreditPlan() {
        return userCreditPlanRepository.findByIsDefaultTrue()
                .orElseGet(() -> userCreditPlanRepository.findByCode(CreditPlanCode.STANDARD).orElse(null));
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

        if (user.getRole() != null) {
            Map<String, Object> extendedResponse = Map.of(
                    "role_id", user.getRole().getId(),
                    "role_name", user.getRole().getName());
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

        if (user.getCreditPlan() != null) {
            response.setUserCreditPlan(CreditPlanResponse.from(user.getCreditPlan()));
        }

        response.setSettings(UserSettingResponse.from(userSettingService.getOrCreateByUserId(user.getId())));

        return response;
    }
}
