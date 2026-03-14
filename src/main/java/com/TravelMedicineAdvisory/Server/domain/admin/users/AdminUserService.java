package com.TravelMedicineAdvisory.Server.domain.admin.users;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public class AdminUserService {

    public List<Object> findAll() {
        // TODO: Implement
        return List.of();
    }

    public Object findById(Long id) {
        // TODO: Implement
        return null;
    }

    public Object update(Long id, Map<String, Object> updates) {
        // TODO: Implement
        return null;
    }

    public void suspend(Long id) {
        // TODO: Implement
    }

    public void activate(Long id) {
        // TODO: Implement
    }

    public void resetCredits(Long id, Integer amount) {
        // TODO: Implement
    }

    public void resetPassword(Long id) {
        // TODO: Implement - Send password reset email
    }

    public void delete(Long id) {
        // TODO: Implement
    }
}
