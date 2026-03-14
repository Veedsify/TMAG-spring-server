package com.TravelMedicineAdvisory.Server.domain.admin.companies;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public class AdminCompanyService {

    public List<Object> findAll() {
        // TODO: Implement
        return List.of();
    }

    public Object findById(Long id) {
        // TODO: Implement
        return null;
    }

    public List<Object> getEmployees(Long id) {
        // TODO: Implement
        return List.of();
    }

    public Object update(Long id, Map<String, Object> updates) {
        // TODO: Implement
        return null;
    }

    public void freeze(Long id) {
        // TODO: Implement - Set billingStatus to FROZEN
    }

    public void unfreeze(Long id) {
        // TODO: Implement - Set billingStatus to ACTIVE
    }

    public void addCredits(Long id, Integer amount) {
        // TODO: Implement
    }

    public void upgradeTier(Long id) {
        // TODO: Implement - Upgrade to ENTERPRISE
    }

    public void delete(Long id) {
        // TODO: Implement
    }
}
