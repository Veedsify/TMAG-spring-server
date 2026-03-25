package com.TravelMedicineAdvisory.Server.domain.admin.abuse;

import com.TravelMedicineAdvisory.Server.domain.abuseflag.AbuseFlag;
import com.TravelMedicineAdvisory.Server.domain.abuseflag.AbuseFlagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminAbuseService {

    private final AbuseFlagRepository abuseFlagRepository;

    public AdminAbuseService(AbuseFlagRepository abuseFlagRepository) {
        this.abuseFlagRepository = abuseFlagRepository;
    }

    public List<AdminAbuseFlagResponse> findAll(Boolean resolved) {
        List<AbuseFlag> flags;

        if (resolved != null) {
            flags = abuseFlagRepository.findAll().stream()
                    .filter(f -> resolved.equals(f.getResolved()))
                    .toList();
        } else {
            flags = abuseFlagRepository.findAll();
        }

        return flags.stream().map(this::mapToResponse).toList();
    }

    public AdminAbuseFlagResponse findById(Long id) {
        AbuseFlag flag = abuseFlagRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Abuse flag not found"));
        return mapToResponse(flag);
    }

    @Transactional
    public void resolve(Long id) {
        AbuseFlag flag = abuseFlagRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Abuse flag not found"));
        flag.setResolved(true);
        flag.setResolvedAt(LocalDateTime.now());
        abuseFlagRepository.save(flag);
    }

    private AdminAbuseFlagResponse mapToResponse(AbuseFlag flag) {
        Long userId = null;
        String userName = null;

        if (flag.getUser() != null) {
            userId = flag.getUser().getId();
            userName = flag.getUser().getName() != null ? flag.getUser().getName() : flag.getUser().getEmail();
        }

        return new AdminAbuseFlagResponse(
                flag.getId(),
                userId,
                userName,
                flag.getType(),
                flag.getDescription(),
                flag.getSeverity(),
                flag.getResolved(),
                flag.getCreatedAt());
    }
}
