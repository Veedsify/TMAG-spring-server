package com.TravelMedicineAdvisory.Server.core.ai;

/**
 * Generic interface for AI providers that support structured output (JSON Schema-constrained responses).
 *
 * @param <T> the Java record type that the provider will return
 */
@FunctionalInterface
public interface StructuredAiClient {

    /**
     * Generate a structured (typed) response from an AI provider.
     *
     * @param systemPrompt system-level instructions
     * @param userPrompt   user/message content
     * @param spec         {@link AiOutputSpec} defining the expected type and schema
     * @param opts         call-time options (provider/model/token/temperature overrides)
     * @param <T>          the expected output type
     * @return a {@link StructuredAiResult} containing the typed value, raw JSON, and metadata
     */
    <T> StructuredAiResult<T> generate(
            String systemPrompt,
            String userPrompt,
            AiOutputSpec<T> spec,
            AiCallOptions opts);
}
