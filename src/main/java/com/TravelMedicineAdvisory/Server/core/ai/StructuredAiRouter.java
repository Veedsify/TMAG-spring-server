package com.TravelMedicineAdvisory.Server.core.ai;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Routes structured-generation requests to the correct provider client based on
 * configuration and per-call options. Mirrors the tier semantics already encoded
 * in {@code PlanGenerationService.modelForTier(...)}.
 */
@Component
public class StructuredAiRouter {

    private static final Logger log = LoggerFactory.getLogger(StructuredAiRouter.class);

    private final List<StructuredAiClient> clients;
    private final AiGenerationProperties properties;

    public StructuredAiRouter(List<StructuredAiClient> clients, AiGenerationProperties properties) {
        this.clients = clients;
        this.properties = properties;
    }

    /**
     * Resolve the provider and model for a main-plan generation call (single or family).
     *
     * @param tier   "free", "standard", or "premium"
     * @return call options with the resolved provider, model, and defaults
     */
    public AiCallOptions resolvePlanOptions(String tier) {
        String provider;
        String model;
        int maxTokens = properties.getMaxOutputTokens();

        if ("STANDARD".equalsIgnoreCase(tier) || "PREMIUM".equalsIgnoreCase(tier)) {
            provider = firstNonBlank(
                    properties.getStandardPremiumProvider(),
                    properties.getMainProvider(),
                    properties.getProvider());
            model = firstNonBlank(
                    properties.getStandardPremiumModel(),
                    properties.getMainModel(),
                    properties.getDefaultModel());
        } else {
            provider = firstNonBlank(
                    properties.getFreeProvider(),
                    properties.getMainProvider(),
                    properties.getProvider());
            model = firstNonBlank(
                    properties.getFreeModel(),
                    properties.getMainModel(),
                    properties.getDefaultModel());
        }
        return new AiCallOptions(provider, model, maxTokens, properties.getTemperature());
    }

    /**
     * Resolve the provider and model for a summary / Action Sheet generation call.
     */
    public AiCallOptions resolveSummaryOptions() {
        String provider = firstNonBlank(
                properties.getSummaryProvider(),
                properties.getMainProvider(),
                properties.getProvider());
        String model = firstNonBlank(
                properties.getSummaryModel(),
                properties.getMainModel(),
                properties.getDefaultModel());
        return new AiCallOptions(provider, model, properties.getSummaryMaxOutputTokens(), properties.getTemperature());
    }

    /**
     * Generate a structured result by resolving the appropriate client from the
     * options.
     */
    public <T> StructuredAiResult<T> generate(
            String systemPrompt,
            String userPrompt,
            AiOutputSpec<T> spec,
            AiCallOptions opts) {

        String effectiveProvider = normalizeProvider(opts.providerOverride());
        log.debug("Routing structured generation: provider={} model={} spec={}",
                effectiveProvider, opts.modelOverride(), spec.schemaName());

        for (StructuredAiClient client : clients) {
            boolean matches = client instanceof AnthropicStructuredClient
                    ? "anthropic".equals(effectiveProvider)
                    : client instanceof GeminiStructuredClient
                            ? "gemini".equals(effectiveProvider) || "vertex".equals(effectiveProvider)
                            : false;
            if (matches) {
                return client.generate(systemPrompt, userPrompt, spec, opts);
            }
        }

        throw new IllegalStateException("No StructuredAiClient found for provider=" + effectiveProvider
                + " (available: " + clients.stream()
                        .map(c -> c.getClass().getSimpleName())
                        .toList()
                + ")");
    }

    private String normalizeProvider(String provider) {
        if (!StringUtils.hasText(provider)) return "gemini";
        String p = provider.trim().toLowerCase();
        // map old "vertex" → "gemini" since the google-genai SDK supports both
        if ("vertex".equals(p)) return "gemini";
        return p;
    }

    private static String firstNonBlank(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) return v;
        }
        return "";
    }
}
