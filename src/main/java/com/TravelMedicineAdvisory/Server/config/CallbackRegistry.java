package com.TravelMedicineAdvisory.Server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CallbackRegistry {

    private static final Map<String, String> BACKEND_PATHS = Map.ofEntries(
        Map.entry("CREDIT_PURCHASE", "/api/v1/credit-purchases/callback"),
        Map.entry("HR_BILLING", "/api/v1/company-admin/credits/callback"),
        Map.entry("ADMIN_CREDITS", "/api/v1/company-admin/credits/callback"),
        Map.entry("EBOOK_PURCHASE", "/api/v1/ebooks/callback"),
        Map.entry("COMPANY_ONBOARDING", "/api/v1/public/company-onboarding/callback")
    );

    private static final Map<String, String> FRONTEND_PATHS = Map.ofEntries(
        Map.entry("CREDIT_PURCHASE", "/payment/callback"),
        Map.entry("HR_BILLING", "/hr/billing/callback"),
        Map.entry("ADMIN_CREDITS", "/admin/credits/callback"),
        Map.entry("EBOOK_PURCHASE", "/shop/order-confirmation"),
        Map.entry("COMPANY_ONBOARDING", "/company-onboarding/callback")
    );

    private final String frontendBaseUrl;
    private final String backendBaseUrl;

    public CallbackRegistry(
            @Value("${app.frontend.url:http://localhost:3000}") String frontendBaseUrl,
            @Value("${app.host:http://localhost:8080}") String backendBaseUrl) {
        this.frontendBaseUrl = frontendBaseUrl.endsWith("/") ? frontendBaseUrl.substring(0, frontendBaseUrl.length() - 1) : frontendBaseUrl;
        this.backendBaseUrl = backendBaseUrl.endsWith("/") ? backendBaseUrl.substring(0, backendBaseUrl.length() - 1) : backendBaseUrl;
    }

    public String getBackendPath(String callbackType) {
        return BACKEND_PATHS.getOrDefault(callbackType, "/api/v1/credit-purchases/callback");
    }

    public String getFrontendPath(String callbackType) {
        return FRONTEND_PATHS.getOrDefault(callbackType, "/payment/callback");
    }

    public String getBackendCallbackUrl(String callbackType) {
        return backendBaseUrl + getBackendPath(callbackType);
    }

    public String getFrontendRedirectUrl(String callbackType) {
        return frontendBaseUrl + getFrontendPath(callbackType);
    }

    /** @deprecated Use getFrontendPath or getFrontendRedirectUrl instead */
    @Deprecated
    public String getRedirectPath(String callbackType) {
        return getFrontendPath(callbackType);
    }

    /** @deprecated Use getBackendCallbackUrl instead */
    @Deprecated
    public String buildCallbackUrl(String callbackType, String baseUrl) {
        return baseUrl + getRedirectPath(callbackType);
    }
}
