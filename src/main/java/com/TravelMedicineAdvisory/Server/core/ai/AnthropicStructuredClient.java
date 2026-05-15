package com.TravelMedicineAdvisory.Server.core.ai;

import com.TravelMedicineAdvisory.Server.domain.plans.PlanGenerationException;
import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.errors.AnthropicInvalidDataException;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.StructuredMessage;
import com.anthropic.models.messages.StructuredMessageCreateParams;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AnthropicStructuredClient implements StructuredAiClient {

    private static final Logger log = LoggerFactory.getLogger(AnthropicStructuredClient.class);
    private static final long MIN_STRUCTURED_OUTPUT_TOKENS = 8192L;

    private volatile AnthropicClient client;
    private final AiGenerationProperties properties;
    private final ObjectMapper objectMapper;
 
    public AnthropicStructuredClient(AiGenerationProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public <T> StructuredAiResult<T> generate(
            String systemPrompt, String userPrompt, AiOutputSpec<T> spec, AiCallOptions opts) {
        long startedAt = System.currentTimeMillis();

        String model = firstNonBlank(opts.modelOverride(),
                properties.getAnthropic().getModel(),
                properties.getMainModel(),
                properties.getDefaultModel(),
                "claude-sonnet-4-6");
        long maxTokens = Math.max((long) opts.maxOutputTokens(), MIN_STRUCTURED_OUTPUT_TOKENS);
 

        StructuredMessageCreateParams<T> params = StructuredMessageCreateParams.<T>builder()
                .model(model)
                .maxTokens(maxTokens)
                .system(MessageCreateParams.System.ofString(systemPrompt))
                .addUserMessage(userPrompt)
                .outputConfig(spec.type())
                .build();

        StructuredMessage<T> response = client().messages().create(params);

        String stopReason = response.stopReason().map(Object::toString).orElse("");
        if (stopReason.toLowerCase().contains("max_tokens")) {
            throw new PlanGenerationException(
                    "Anthropic stopped at max_tokens before completing structured JSON for spec="
                            + spec.schemaName() + "; reduce output length or raise app.ai.max-output-tokens",
                    null);
        }
 
        T value;
        try {
            value = response.content().stream()
                    .filter(cb -> cb.isText())
                    .findFirst()
                    .map(cb -> cb.asText().text())
                    .orElseThrow(() -> new PlanGenerationException(
                            "Anthropic returned no structured content block", null));
        } catch (AnthropicInvalidDataException ex) {
            throw new PlanGenerationException(
                    "Anthropic returned JSON that did not deserialize for spec="
                            + spec.schemaName() + ": " + ex.getMessage(),
                    ex);
        }

        String rawJson = spec.toJson(value, objectMapper);

        long inputTokens = response.usage().inputTokens();
        long outputTokens = response.usage().outputTokens();
        Integer estimatedTokens = (int) (inputTokens + outputTokens);

        long elapsed = System.currentTimeMillis() - startedAt;
        log.info("Anthropic structured generation: model={} spec={} tokens≈{} ms={}",
                model, spec.schemaName(), estimatedTokens, elapsed);

        return new StructuredAiResult<>(value, rawJson, "anthropic", model, estimatedTokens);
    }
 
    private AnthropicClient client() {
        AnthropicClient existing = client;
        if (existing != null) {
            return existing;
        }
        synchronized (this) {
            if (client == null) {
                String apiKey = properties.getAnthropic().getApiKey();
                if (apiKey == null || apiKey.isBlank()) {
                    throw new PlanGenerationException(
                            "Anthropic API key is required (app.ai.anthropic.api-key)", null);
                }
                client = AnthropicOkHttpClient.builder().apiKey(apiKey).build();
            }
            return client;
        }
    }

    private static String firstNonBlank(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) return v;
        }
        return "claude-sonnet-4-6";
    }
}
