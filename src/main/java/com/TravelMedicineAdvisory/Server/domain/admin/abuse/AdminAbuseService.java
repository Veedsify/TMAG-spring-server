package com.TravelMedicineAdvisory.Server.domain.admin.abuse;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AdminAbuseService {

    public List<Object> findAll(Boolean resolved) {
        // TODO: Implement - Filter by resolved status if provided
        return List.of();
    }

    public void resolve(Long id) {
        // TODO: Implement - Mark abuse flag as resolved
    }
}
