package com.TravelMedicineAdvisory.Server.domain.admin.credits;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public class AdminCreditService {

    public List<Object> getLedger(Long userId, Long companyId) {
        // TODO: Implement - Filter by userId or companyId if provided
        return List.of();
    }

    public void adjustCredits(Map<String, Object> body) {
        // TODO: Implement - Add/deduct credits with reason
        // Expected body: { userId?, companyId?, amount, reason }
    }
}
