package com.TravelMedicineAdvisory.Server.core.cache;

/**
 * Spring Cache region names; TTLs are set in {@link CacheConfig}.
 */
public final class CacheNames {

    private CacheNames() {}

    public static final String ADMIN_ROLES = "adminRoles";
    public static final String AUTH_GOOGLE_URL = "authGoogleUrl";
    public static final String BLOG_POSTS = "blogPosts";
    public static final String COMPANY_ADMIN_CREDITS_PRICING = "companyAdminCreditsPricing";
    public static final String COMPANY_CODE_VALIDATION = "companyCodeValidation";
    public static final String COMPANY_SETTINGS = "companySettings";
    public static final String COUNTRIES = "countries";
    public static final String COUNTRY_ACCOMMODATIONS = "countryAccommodations";
    public static final String COUNTRY_HEALTH_ALERTS = "countryHealthAlerts";
    public static final String CREDIT_PRICING = "creditPricing";
    public static final String CURRENCIES = "currencies";
    public static final String EBOOKS = "ebooks";
    public static final String EXCHANGE_RATES = "exchangeRates";
    public static final String FAQ_ITEMS = "faqItems";
    public static final String ONBOARDING_QUESTIONS = "onboardingQuestions";
    public static final String PAYMENTS_CONFIG = "paymentsConfig";
    public static final String PERMISSIONS = "permissions";
    public static final String RESOURCE_PERMISSIONS = "resourcePermissions";
    public static final String ROLE_PERMISSIONS = "rolePermissions";
    public static final String ROLES = "roles";
    public static final String SYSTEM_SETTINGS = "systemSettings";
    public static final String TRANSLATIONS = "translations";
    public static final String USER_CREDIT_PLANS = "userCreditPlans";

    /** Used by {@link org.springframework.cache.concurrent.ConcurrentMapCacheManager} (no per-entry TTL). */
    public static final String[] ALL = {
            ADMIN_ROLES,
            AUTH_GOOGLE_URL,
            BLOG_POSTS,
            COMPANY_ADMIN_CREDITS_PRICING,
            COMPANY_CODE_VALIDATION,
            COMPANY_SETTINGS,
            COUNTRIES,
            COUNTRY_ACCOMMODATIONS,
            COUNTRY_HEALTH_ALERTS,
            CREDIT_PRICING,
            CURRENCIES,
            EBOOKS,
            EXCHANGE_RATES,
            FAQ_ITEMS,
            ONBOARDING_QUESTIONS,
            PAYMENTS_CONFIG,
            PERMISSIONS,
            RESOURCE_PERMISSIONS,
            ROLE_PERMISSIONS,
            ROLES,
            SYSTEM_SETTINGS,
            TRANSLATIONS,
            USER_CREDIT_PLANS,
    };
}
