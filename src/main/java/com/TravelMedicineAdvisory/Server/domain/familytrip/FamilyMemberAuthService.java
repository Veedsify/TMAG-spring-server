package com.TravelMedicineAdvisory.Server.domain.familytrip;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.TravelMedicineAdvisory.Server.core.email.EmailService;
import com.TravelMedicineAdvisory.Server.core.email.EmailTemplates;
import com.TravelMedicineAdvisory.Server.domain.user.User;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;

@Service
@Transactional
public class FamilyMemberAuthService {

    private final FamilyTripMemberRepository memberRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final EmailTemplates emailTemplates;
    private final SecureRandom secureRandom = new SecureRandom();
    private static final String CODE_CHARS = "ABCDEFGHJKMNPQRSTUVWXYZ23456789"; // excluded 0, O, 1, I, L

    public FamilyMemberAuthService(FamilyTripMemberRepository memberRepository, UserRepository userRepository,
                                    EmailService emailService, EmailTemplates emailTemplates) {
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.emailTemplates = emailTemplates;
    }

    public void generateAndSendCode(FamilyTripMember member) {
        String code = generateCode();
        member.setLoginCode(code);
        member.setLoginCodeConsumedAt(null);
        member.setSessionTokenHash(null);
        member.setFailedLoginAttempts(0);
        member.setLockedUntil(null);

        memberRepository.save(member);

        if (member.getMemberEmail() != null && !member.getMemberEmail().isBlank()) {
            String leadName = member.getFamilyTrip().getUser().getFullName();
            String destination = member.getFamilyTrip().getDestination();
            String tripId = String.valueOf(member.getFamilyTrip().getId());
            String html = emailTemplates.familyMemberInviteEmail(
                member.getFirstName(), leadName, destination, code, tripId
            );
            emailService.sendHtmlEmail(
                member.getMemberEmail(),
                "Your TMAG family trip access code — " + destination,
                html
            );
        }
    }

    public String login(String mainApplicantEmail, String code) {
        User user = userRepository.findByEmailIgnoreCase(mainApplicantEmail)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or code"));

        FamilyTripMember member = memberRepository.findByLoginCodeAndNotConsumedAndUserId(code, user.getId())
            .orElse(null);

        if (member == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or code");
        }

        if (member.getLockedUntil() != null && member.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.LOCKED, "Too many attempts. Try again later.");
        }

        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);

        member.setSessionTokenHash(sha256(token));
        member.setSessionExpiresAt(LocalDateTime.now().plusDays(7));
        member.setLoginCodeConsumedAt(LocalDateTime.now());
        member.setFailedLoginAttempts(0);

        memberRepository.save(member);

        return token;
    }
    
    public FamilyTripMember requireMember(String headerToken) {
        if (headerToken == null || headerToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing session token");
        }
        String hash = sha256(headerToken);
        FamilyTripMember m = memberRepository.findBySessionTokenHashAndDeletedAtIsNull(hash)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid session"));
            
        if (m.getSessionExpiresAt() == null || m.getSessionExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session expired");
        }
        
        m.setSessionExpiresAt(LocalDateTime.now().plusDays(7)); // sliding expiry
        return memberRepository.save(m);
    }
    
    public void logout(String headerToken) {
        if (headerToken == null || headerToken.isBlank()) return;
        String hash = sha256(headerToken);
        memberRepository.findBySessionTokenHashAndDeletedAtIsNull(hash).ifPresent(m -> {
            m.setSessionTokenHash(null);
            m.setSessionExpiresAt(null);
            memberRepository.save(m);
        });
    }

    private String generateCode() {
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            sb.append(CODE_CHARS.charAt(secureRandom.nextInt(CODE_CHARS.length())));
        }
        return sb.toString();
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
