package com.TravelMedicineAdvisory.Server.domain.admin.auth;

import org.springframework.stereotype.Service;

@Service
public class AdminAuthService {

    public Object login(String email, String password) {
        // TODO: Implement - Authenticate admin user
        // Return: { token, exp, user }
        return null;
    }

    public void logout() {
        // TODO: Implement - Invalidate admin session
    }

    public Object getCurrentUser() {
        // TODO: Implement - Get current authenticated admin user
        return null;
    }
}
