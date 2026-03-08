package com.TravelMedicineAdvisory.Server.domain.auth;

import com.TravelMedicineAdvisory.Server.core.email.EmailService;
import com.TravelMedicineAdvisory.Server.domain.role.Role;
import com.TravelMedicineAdvisory.Server.domain.role.RoleRepository;
import com.TravelMedicineAdvisory.Server.domain.user.User;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;
import com.TravelMedicineAdvisory.Server.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final EmailService emailService;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder, JwtService jwtService,
                       AuthenticationManager authenticationManager, UserDetailsService userDetailsService,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.emailService = emailService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Validate required fields
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }

        // Validate uniqueness before attempting insert
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("User already exists");
        }
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("User already exists");
        }

        // Determine role: first user gets SuperAdmin (1), subsequent get Individual (5)
        Role role = determineUserRole();

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setUsername(request.getUsername());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setOnboardingStage(0);
        user.setOnboarded(false);
        user.setVerified(false);
        user.setType("INDIVIDUAL");
        user.setCredits(0);
        user.setRole(role);
        user.setLastLogin(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        // Send verification email
        sendVerificationEmail(savedUser);

        UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getEmail());
        String jwtToken = jwtService.generateToken(userDetails);

        return buildAuthResponse(savedUser, jwtToken);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        // Block unverified users and send a verification email
        if (Boolean.FALSE.equals(user.getVerified())) {
            sendVerificationEmail(user);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Email not verified. A new verification link has been sent.");
        }

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String jwtToken = jwtService.generateToken(userDetails);

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
        String title = "Reset your password";
        String content = String.format("""
            <p>Hi %s,</p>
            <p>We received a request to reset your password. Click the button below to set a new password:</p>
            <div style="text-align: center;">
                <a href="%s" style="display: inline-block; padding: 12px 32px; background-color: #2a1e14; color: #f6f0e9 !important; text-decoration: none; border-radius: 12px; font-weight: 600; font-size: 16px; margin: 24px 0;">Reset Password</a>
            </div>
            <p>Or copy and paste this link into your browser:</p>
            <p style="word-break: break-all; color: #2a7a6a; font-size: 14px;">%s</p>
            <p>This link will expire in 15 minutes.</p>
            <p>If you didn't request a password reset, please ignore this email or contact support if you have concerns.</p>
        """, user.getFirstName(), resetLink, resetLink);
        
        emailService.sendHtmlEmail(email, title, getEmailHtmlWrapper(title, content));
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

        String title = "Password Reset Confirmation";
        String content = String.format("""
            <p>Hi %s,</p>
            <p>Your password has been successfully reset. If you did not make this change, please contact support immediately.</p>
        """, user.getFirstName());
        
        emailService.sendHtmlEmail(email, title, getEmailHtmlWrapper(title, content));
    }

    @Transactional
    public void resendVerification(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        if (Boolean.TRUE.equals(user.getVerified())) {
            throw new IllegalArgumentException("Email already verified");
        }

        sendVerificationEmail(user);
    }

    @Transactional
    public void verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid verification token"));

        if (user.getVerificationTokenExpiry() == null || user.getVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.GONE, "Verification link has expired");
        }

        user.setVerified(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiry(null);
        userRepository.save(user);
    }

    private void sendVerificationEmail(User user) {
        String token = generateToken(16);
        user.setVerificationToken(token);
        user.setVerificationTokenExpiry(LocalDateTime.now().plusHours(24));
        userRepository.save(user);

        String verifyLink = frontendUrl + "/auth/verify-email?token=" + token;
        String title = "Verify your email address";
        String content = String.format("""
            <p>Hi %s,</p>
            <p>Please verify your email address by clicking the button below:</p>
            <div style="text-align: center;">
                <a href="%s" style="display: inline-block; padding: 12px 32px; background-color: #2a1e14; color: #f6f0e9 !important; text-decoration: none; border-radius: 12px; font-weight: 600; font-size: 16px; margin: 24px 0;">Verify Email</a>
            </div>
            <p>Or copy and paste this link into your browser:</p>
            <p style="word-break: break-all; color: #2a7a6a; font-size: 14px;">%s</p>
            <p>This link will expire in 24 hours.</p>
            <p>If you did not create an account, you can safely ignore this email.</p>
        """, user.getFirstName(), verifyLink, verifyLink);

        emailService.sendHtmlEmail(user.getEmail(), title, getEmailHtmlWrapper(title, content));
    }

    private String getEmailHtmlWrapper(String title, String content) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; line-height: 1.6; color: #2c3e50; margin: 0; padding: 0; background-color: #f6f0e9; }
                    .container { max-width: 600px; margin: 20px auto; padding: 40px; background-color: #ffffff; border-radius: 24px; box-shadow: 0 4px 6px rgba(0,0,0,0.05); }
                    .header { text-align: center; margin-bottom: 40px; }
                    .logo { font-size: 24px; font-weight: bold; color: #2a1e14; text-decoration: none; }
                    .title { font-size: 22px; color: #2a1e14; margin-bottom: 24px; font-weight: 600; }
                    .content { font-size: 16px; color: #4a5568; }
                    .footer { text-align: center; margin-top: 40px; font-size: 14px; color: #a0aec0; }
                    .divider { height: 1px; background-color: #e2e8f0; margin: 32px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <a href="%s" class="logo">TMAG GLOBAL</a>
                    </div>
                    <div class="title">%s</div>
                    <div class="content">
                        %s
                    </div>
                    <div class="divider"></div>
                    <div class="footer">
                        &copy; %d Travel Medicine Advisory Global. All rights reserved.
                    </div>
                </div>
            </body>
            </html>
        """, frontendUrl, title, content, java.time.Year.now().getValue());
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
                "role_name", user.getRole().getName()
        );

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

        return response;
    }
}
