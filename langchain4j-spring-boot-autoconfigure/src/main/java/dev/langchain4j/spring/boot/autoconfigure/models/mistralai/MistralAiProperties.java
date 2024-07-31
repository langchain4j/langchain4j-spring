package dev.langchain4j.spring.boot.autoconfigure.models.mistralai;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;
import java.time.Duration;

/**
 * Common configuration properties for the Mistral AI integration.
 */
@ConfigurationProperties(MistralAiProperties.CONFIG_PREFIX)
public class MistralAiProperties {

    public static final String CONFIG_PREFIX = "langchain4j.mistralai";

    /**
     * Whether to enable the Mistral AI integration.
     */
    private boolean enabled = true;

    /**
     * Settings for the HTTP client.
     */
    private Client client = new Client();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public static class Client {

        /**
         * Base URL of the Mistral AI API.
         */
        private URI baseUrl = URI.create("https://api.mistral.ai/v1");

        /**
         * Maximum time to wait for a connection.
         */
        private Duration connectTimeout = Duration.ofSeconds(10);

        /**
         * Maximum time to wait for a response.
         */
        private Duration readTimeout = Duration.ofSeconds(60);

        /**
         * Mistral AI APY Key.
         */
        private String apiKey;

        /**
         * Maximum number of retries.
         */
        private int maxRetries = 3;

        /**
         * Whether to log requests.
         */
        private boolean logRequests = false;

        /**
         * Whether to log responses.
         */
        private boolean logResponses = false;

        public URI getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(URI baseUrl) {
            this.baseUrl = baseUrl;
        }

        public Duration getConnectTimeout() {
            return connectTimeout;
        }

        public void setConnectTimeout(Duration connectTimeout) {
            this.connectTimeout = connectTimeout;
        }

        public Duration getReadTimeout() {
            return readTimeout;
        }

        public void setReadTimeout(Duration readTimeout) {
            this.readTimeout = readTimeout;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public int getMaxRetries() {
            return maxRetries;
        }

        public void setMaxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
        }

        public boolean isLogRequests() {
            return logRequests;
        }

        public void setLogRequests(boolean logRequests) {
            this.logRequests = logRequests;
        }

        public boolean isLogResponses() {
            return logResponses;
        }

        public void setLogResponses(boolean logResponses) {
            this.logResponses = logResponses;
        }

    }

}
