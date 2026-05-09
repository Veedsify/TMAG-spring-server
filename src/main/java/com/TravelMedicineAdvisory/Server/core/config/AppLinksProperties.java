package com.TravelMedicineAdvisory.Server.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.links")
public record AppLinksProperties(
        String frontend,
        String superAdminApp,
        String adminApp,
        String affiliateApp,
        String supportApp) {

    public String frontendUrl() {
        return clean(frontend, "http://localhost:3000");
    }

    public String superAdminAppUrl() {
        return clean(superAdminApp, "http://localhost:3001");
    }

    public String adminAppUrl() {
        return clean(adminApp, "http://localhost:3002");
    }

    public String affiliateAppUrl() {
        return clean(affiliateApp, "http://localhost:3010");
    }

    public String supportAppUrl() {
        return clean(supportApp, "http://localhost:5173");
    }

    private String clean(String value, String fallback) {
        String resolved = value == null || value.isBlank() ? fallback : value.trim();
        return resolved.endsWith("/") ? resolved.substring(0, resolved.length() - 1) : resolved;
    }
}
