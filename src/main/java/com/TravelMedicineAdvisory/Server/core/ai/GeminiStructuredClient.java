package com.TravelMedicineAdvisory.Server.core.ai;

import com.TravelMedicineAdvisory.Server.domain.plans.PlanGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.FinishReason;
import com.google.genai.types.Part;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class GeminiStructuredClient implements StructuredAiClient {

    private static final Logger log = LoggerFactory.getLogger(GeminiStructuredClient.class);
    private static final int MIN_PLAN_OUTPUT_TOKENS = 24_576;
    private static final int MIN_SUMMARY_OUTPUT_TOKENS = 4_096;

    private volatile Client client;
    private final AiGenerationProperties properties;
    private final ObjectMapper objectMapper;
 
    public GeminiStructuredClient(AiGenerationProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public <T> StructuredAiResult<T> generate(
            String systemPrompt, String userPrompt, AiOutputSpec<T> spec, AiCallOptions opts) {
        long startedAt = System.currentTimeMillis();

        String model = firstNonBlank(opts.modelOverride(),
                properties.getGemini().getModel(),
                properties.getVertex().getModel(),
                properties.getMainModel(),
                properties.getDefaultModel(),
                "gemini-2.5-pro");
        int maxOutputTokens = effectiveMaxOutputTokens(spec, opts.maxOutputTokens());
 

        Content systemContent = Content.fromParts(Part.builder().text(systemPrompt).build());

        GenerateContentConfig config = GenerateContentConfig.builder()
                .systemInstruction(systemContent)
                .responseMimeType("application/json")
                .responseSchema(spec.geminiSchema())
                .candidateCount(1)
                .temperature((float) opts.temperature())
                .maxOutputTokens(maxOutputTokens)
                .build();

        String effectiveProvider = StringUtils.hasText(properties.getGemini().getApiKey())
                ? "gemini-ai-studio" : "vertex";

        try {
            GenerateContentResponse response = client().models.generateContent(model, userPrompt, config);
            if (response == null) {
                throw new PlanGenerationException("Gemini returned a null response", null);
            }

            FinishReason finishReason = response.finishReason();
            if (finishReason.knownEnum() == FinishReason.Known.MAX_TOKENS) {
                throw new PlanGenerationException(
                        "Gemini stopped at maxOutputTokens=" + maxOutputTokens
                                + " before completing structured JSON for spec=" + spec.schemaName()
                                + ". Increase app.ai.max-output-tokens or reduce generated content length.",
                        null);
            }
            if (finishReason.knownEnum() != FinishReason.Known.STOP
                    && finishReason.knownEnum() != FinishReason.Known.FINISH_REASON_UNSPECIFIED) {
                throw new PlanGenerationException(
                        "Gemini stopped before returning structured JSON for spec=" + spec.schemaName()
                                + " finishReason=" + finishReason
                                + finishMessage(response),
                        null);
            }
            if (response.text() == null) {
                throw new PlanGenerationException("Gemini returned an empty response", null);
            }
 
            String text = response.text();
            T value;
            try {
                value = objectMapper.readValue(text, spec.type());
            } catch (JsonProcessingException ex) {
                throw new PlanGenerationException(
                        "Gemini returned invalid JSON for spec=" + spec.schemaName()
                                + " finishReason=" + finishReason
                                + " outputChars=" + text.length()
                                + truncationHint(text, ex)
                                + ": " + ex.getOriginalMessage(),
                        ex);
            }

            Integer tokens = null;
            if (response.usageMetadata().isPresent()
                    && response.usageMetadata().get().totalTokenCount().isPresent()) {
                tokens = response.usageMetadata().get().totalTokenCount().get();
            }

            long elapsed = System.currentTimeMillis() - startedAt;
            log.info("Gemini structured generation: model={} spec={} tokens≈{} maxOutputTokens={} ms={}",
                    model, spec.schemaName(), tokens, maxOutputTokens, elapsed);

            String rawJson = spec.toJson(value, objectMapper);
            return new StructuredAiResult<>(value, rawJson, effectiveProvider, model, tokens);

        } catch (PlanGenerationException e) {
            throw e;
        } catch (Exception ex) {
            throw new PlanGenerationException(
                    "Gemini generation failed for model=" + model + ": " + ex.getMessage(), ex);
        }
    }
 
    static int effectiveMaxOutputTokens(AiOutputSpec<?> spec, int requested) {
        int minimum = "action-sheet".equals(spec.schemaName())
                ? MIN_SUMMARY_OUTPUT_TOKENS
                : MIN_PLAN_OUTPUT_TOKENS;
        return Math.max(requested, minimum);
    }
 
    private static String truncationHint(String text, JsonProcessingException ex) {
        String message = ex.getOriginalMessage() != null ? ex.getOriginalMessage().toLowerCase() : "";
        String trimmed = text != null ? text.trim() : "";
        if (message.contains("end-of-input")
                || (!trimmed.isEmpty() && !trimmed.endsWith("}") && !trimmed.endsWith("]"))) {
            return " (response appears truncated; check finishReason/maxOutputTokens)";
        }
        return "";
    }
 
    private static String finishMessage(GenerateContentResponse response) {
        if (response.candidates().isPresent() && !response.candidates().get().isEmpty()
                && response.candidates().get().getFirst().finishMessage().isPresent()) {
            return " finishMessage=" + response.candidates().get().getFirst().finishMessage().get();
        }
        return "";
    }
 
    private Client client() {
        Client existing = client;
        if (existing != null) {
            return existing;
        }
        synchronized (this) {
            if (client == null) {
                String geminiKey = properties.getGemini().getApiKey();
                String vertexProject = properties.getVertex().getProjectId();
                if (StringUtils.hasText(geminiKey)) {
                    client = Client.builder().apiKey(geminiKey).build();
                    log.info("Gemini client initialised: AI Studio backend");
                } else if (StringUtils.hasText(vertexProject)) {
                    String location = firstNonBlank(properties.getVertex().getLocation(), "us-central1");
                    client = Client.builder()
                            .vertexAI(true)
                            .project(vertexProject)
                            .location(location)
                            .build();
                    log.info("Gemini client initialised: Vertex AI backend project={} location={}",
                            vertexProject, location);
                } else {
                    throw new PlanGenerationException(
                            "Gemini requires either app.ai.gemini.api-key (AI Studio) or app.ai.vertex.project-id (Vertex AI)",
                            null);
                }
            }
            return client;
        }
    }

    private static String firstNonBlank(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) return v;
        }
        return "gemini-2.5-pro";
    }
}
