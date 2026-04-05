package com.TravelMedicineAdvisory.Server.core.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ai")
public class AiGenerationProperties {

    private String provider = "vertex";
    private String defaultModel = "gemini-2.5-pro";
    private double temperature = 0.2d;
    private int maxOutputTokens = 8192;

    private final Vertex vertex = new Vertex();
    private final OpenAi openai = new OpenAi();
    private final Anthropic anthropic = new Anthropic();

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getDefaultModel() {
        return defaultModel;
    }

    public void setDefaultModel(String defaultModel) {
        this.defaultModel = defaultModel;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public int getMaxOutputTokens() {
        return maxOutputTokens;
    }

    public void setMaxOutputTokens(int maxOutputTokens) {
        this.maxOutputTokens = maxOutputTokens;
    }

    public Vertex getVertex() {
        return vertex;
    }

    public OpenAi getOpenai() {
        return openai;
    }

    public Anthropic getAnthropic() {
        return anthropic;
    }

    public static class Vertex {
        private String projectId = "";
        private String location = "us-central1";
        private String model = "gemini-2.5-pro";
        /**
         * Absolute or relative path to a service account JSON key. If empty, {@code GOOGLE_APPLICATION_CREDENTIALS}
         * is tried, then Application Default Credentials.
         */
        private String credentialsPath = "";

        public String getProjectId() {
            return projectId;
        }

        public void setProjectId(String projectId) {
            this.projectId = projectId;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getCredentialsPath() {
            return credentialsPath;
        }

        public void setCredentialsPath(String credentialsPath) {
            this.credentialsPath = credentialsPath;
        }
    }

    public static class OpenAi {
        private String apiKey = "";
        private String model = "gpt-4.1";
        private String baseUrl = "https://api.openai.com/v1";

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }
    }

    public static class Anthropic {
        private String apiKey = "";
        private String model = "claude-sonnet-4-0";
        private String baseUrl = "https://api.anthropic.com/v1";

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }
    }
}
