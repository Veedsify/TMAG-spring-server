package com.TravelMedicineAdvisory.Server.core.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.types.Schema;

/**
 * Carries both the Java type and the Gemini Schema for a structured-output
 * call.
 * Anthropic derives its schema from {@code type} via
 * {@code outputConfig(Class<T>)}.
 * Gemini uses the pre-built {@code geminiSchema} directly.
 *
 * @param type         the Java record class that responses are deserialised
 *                     into
 * @param geminiSchema the SDK {@link Schema} object to pass in
 *                     {@code responseSchema}
 * @param schemaName   a stable human-readable identifier for logging
 * @param <T>          the output record type
 */
public record AiOutputSpec<T>(
        Class<T> type,
        Schema geminiSchema,
        String schemaName) {

    /**
     * Convert the typed value to canonical JSON once.
     */
    public String toJson(T value, ObjectMapper mapper) {
        return mapper.valueToTree(value).toString();
    }
}
