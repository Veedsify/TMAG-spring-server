package com.TravelMedicineAdvisory.Server.core.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@Service
public class AiGenerationClient {

    private final WebClient webClient;
    private final AiGenerationProperties properties;

    private GoogleCredential cachedCredential;

    public AiGenerationClient(WebClient.Builder webClientBuilder, AiGenerationProperties properties) {
        this.webClient = webClientBuilder.build();
        this.properties = properties;
    }

    public AiGenerationResult generate(String systemPrompt, String userPrompt) {
        String provider = normalizeProvider(properties.getProvider());
        return switch (provider) {
            case "openai" -> generateOpenAi(systemPrompt, userPrompt);
            case "anthropic" -> generateAnthropic(systemPrompt, userPrompt);
            default -> generateVertex(systemPrompt, userPrompt);
        };
    }

    private AiGenerationResult generateVertex(String systemPrompt, String userPrompt) {
        String projectId = properties.getVertex().getProjectId();
        if (!StringUtils.hasText(projectId)) {
            throw new IllegalStateException("Vertex project id is required (app.ai.vertex.project-id)");
        }

        String location = properties.getVertex().getLocation();
        String model = firstNonBlank(properties.getVertex().getModel(), properties.getDefaultModel(), "gemini-2.5-pro");
        String token = getGoogleAccessToken();
        String url = buildVertexGenerateContentUrl(projectId, location, model);

        JsonNode response = webClient.post()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "contents", List.of(Map.of("role", "user", "parts", List.of(Map.of("text",
                                "SYSTEM:\n" + systemPrompt + "\n\nUSER:\n" + userPrompt)))),
                        "generationConfig", Map.of(
                                "temperature", properties.getTemperature(),
                                "maxOutputTokens", properties.getMaxOutputTokens())))
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse -> clientResponse.bodyToMono(String.class)
                        .defaultIfEmpty("")
                        .flatMap(body -> {
                            String hint404 = clientResponse.statusCode().value() == 404
                                    ? " Unknown model id, wrong location, or wrong host. "
                                            + "Regional models (e.g. gemini-2.5-pro): VERTEX_LOCATION=us-central1. "
                                            + "Gemini 3.1 Pro on Vertex uses id gemini-3.1-pro-preview with VERTEX_LOCATION=global "
                                            + "(global uses aiplatform.googleapis.com, not *-aiplatform.googleapis.com). "
                                            + "Confirm VERTEX_PROJECT_ID is your GCP project id."
                                    : "";
                            return Mono.error(new IllegalStateException(
                                    "Vertex AI " + clientResponse.statusCode() + " POST " + url + hint404
                                            + (body.isBlank() ? "" : " Body: " + body)));
                        }))
                .bodyToMono(JsonNode.class)
                .block();

        String text = readText(response, "/candidates/0/content/parts/0/text");
        int tokens = response != null && response.at("/usageMetadata/totalTokenCount").isNumber()
                ? response.at("/usageMetadata/totalTokenCount").asInt()
                : estimateTokens(text);
        return new AiGenerationResult(text, "vertex", model, tokens);
    }

    private AiGenerationResult generateOpenAi(String systemPrompt, String userPrompt) {
        String apiKey = properties.getOpenai().getApiKey();
        if (!StringUtils.hasText(apiKey)) {
            throw new IllegalStateException("OpenAI API key is required (app.ai.openai.api-key)");
        }

        String model = firstNonBlank(properties.getOpenai().getModel(), properties.getDefaultModel(), "gpt-4.1");
        String baseUrl = firstNonBlank(properties.getOpenai().getBaseUrl(), "https://api.openai.com/v1");

        JsonNode response = webClient.post()
                .uri(baseUrl + "/responses")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "model", model,
                        "temperature", properties.getTemperature(),
                        "reasoning", Map.of("effort", "high"),
                        "max_tokens", properties.getMaxOutputTokens(),
                        "instructions", systemPrompt,
                        "messages", List.of(
                                Map.of("role", "platform", "content", systemPrompt),
                                Map.of("role", "user", "content", userPrompt))))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        String text = readText(response, "/choices/0/message/content");
        int tokens = response != null && response.at("/usage/total_tokens").isNumber()
                ? response.at("/usage/total_tokens").asInt()
                : estimateTokens(text);
        return new AiGenerationResult(text, "openai", model, tokens);
    }

    private AiGenerationResult generateAnthropic(String systemPrompt, String userPrompt) {
        String apiKey = properties.getAnthropic().getApiKey();
        if (!StringUtils.hasText(apiKey)) {
            throw new IllegalStateException("Anthropic API key is required (app.ai.anthropic.api-key)");
        }

        String model = firstNonBlank(properties.getAnthropic().getModel(), properties.getDefaultModel(),
                "claude-sonnet-4-0");
        String baseUrl = firstNonBlank(properties.getAnthropic().getBaseUrl(), "https://api.anthropic.com/v1");

        JsonNode response = webClient.post()
                .uri(baseUrl + "/messages")
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "model", model,
                        "system", systemPrompt,
                        "max_tokens", properties.getMaxOutputTokens(),
                        "temperature", properties.getTemperature(),
                        "messages", List.of(Map.of("role", "user", "content", userPrompt))))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        String text = readText(response, "/content/0/text");
        int inputTokens = response != null && response.at("/usage/input_tokens").isNumber()
                ? response.at("/usage/input_tokens").asInt()
                : 0;
        int outputTokens = response != null && response.at("/usage/output_tokens").isNumber()
                ? response.at("/usage/output_tokens").asInt()
                : estimateTokens(text);
        return new AiGenerationResult(text, "anthropic", model, inputTokens + outputTokens);
    }

    private String readText(JsonNode root, String pointer) {
        if (root == null) {
            throw new IllegalStateException("AI provider returned an empty response");
        }
        JsonNode node = root.at(pointer);
        if (!node.isTextual()) {
            throw new IllegalStateException("AI provider response did not contain text content");
        }
        return node.asText("");
    }

    private synchronized String getGoogleAccessToken() {
        try {
            if (cachedCredential == null) {
                cachedCredential = loadGoogleCredential();
            }
            cachedCredential.refreshToken();
            String tokenValue = cachedCredential.getAccessToken();
            if (!StringUtils.hasText(tokenValue)) {
                throw new IllegalStateException("Unable to obtain Google Cloud access token");
            }
            return tokenValue;
        } catch (IOException ex) {
            String hint = "Set app.ai.vertex.credentials-path or GOOGLE_APPLICATION_CREDENTIALS to a service-account JSON file, "
                    + "or run: gcloud auth application-default login. Enable Vertex AI API on your project; SA needs aiplatform.user (or broader). ";
            throw new IllegalStateException(
                    "Failed to load Google Cloud credentials for Vertex AI. " + hint + "Cause: " + ex.getMessage(), ex);
        }
    }

    private GoogleCredential loadGoogleCredential() throws IOException {
        String configured = properties.getVertex().getCredentialsPath();
        String fromEnv = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
        String pathStr = StringUtils.hasText(configured) ? configured.trim() : (fromEnv != null ? fromEnv.trim() : "");

        if (StringUtils.hasText(pathStr)) {
            Path path = Paths.get(pathStr);
            if (!Files.isRegularFile(path)) {
                throw new IOException("Credential file does not exist or is not a file: " + path.toAbsolutePath());
            }
            try (var in = Files.newInputStream(path)) {
                return GoogleCredential.fromStream(in)
                        .createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));
            }
        }

        GoogleCredential credential = GoogleCredential.getApplicationDefault();
        if (credential.createScopedRequired()) {
            credential = credential.createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));
        }
        return credential;
    }

    /**
     * Regional Gemini calls use
     * {@code https://REGION-aiplatform.googleapis.com/.../locations/REGION/...}.
     * Gemini 3.x preview models on Vertex require {@code locations/global} and the
     * non-regional host
     * {@code https://aiplatform.googleapis.com/...} per Google Cloud docs.
     */
    static String buildVertexGenerateContentUrl(String projectId, String location, String model) {
        String loc = StringUtils.hasText(location) ? location.trim().toLowerCase() : "us-central1";
        if ("global".equals(loc)) {
            return "https://aiplatform.googleapis.com/v1/projects/" + projectId
                    + "/locations/global/publishers/google/models/" + model + ":generateContent";
        }
        return "https://" + loc + "-aiplatform.googleapis.com/v1/projects/" + projectId
                + "/locations/" + loc + "/publishers/google/models/" + model + ":generateContent";
    }

    private String normalizeProvider(String provider) {
        if (!StringUtils.hasText(provider)) {
            return "vertex";
        }
        return provider.trim().toLowerCase();
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return "";
    }

    private int estimateTokens(String text) {
        if (!StringUtils.hasText(text)) {
            return 0;
        }
        return Math.max(1, text.length() / 4);
    }
}
