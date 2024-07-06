package dev.langchain4j.spring.mistralai.client;

import org.springframework.util.Assert;

import java.net.URI;
import java.time.Duration;

/**
 * Options for configuring the Mistral AI client.
 */
public record MistralAiClientConfig(
        URI baseUrl,
        Duration connectTimeout,
        Duration readTimeout,
        String apiKey,
        boolean logRequests,
        boolean logResponses
) {

    public MistralAiClientConfig {
        Assert.notNull(baseUrl, "baseUrl must not be null");
        Assert.notNull(connectTimeout, "connectTimeout must not be null");
        Assert.notNull(readTimeout, "readTimeout must not be null");
        Assert.hasText(apiKey, "apiKey must not be null or empty");
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private URI baseUrl = URI.create("https://api.mistral.ai/v1");
        private Duration connectTimeout = Duration.ofSeconds(10);
        private Duration readTimeout = Duration.ofSeconds(60);
        private String apiKey;
        private boolean logRequests = false;
        private boolean logResponses = false;

        private Builder() {}

        public Builder baseUrl(URI baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder connectTimeout(Duration connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public Builder readTimeout(Duration readTimeout) {
            this.readTimeout = readTimeout;
            return this;
        }

        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public Builder logRequests(boolean logRequests) {
            this.logRequests = logRequests;
            return this;
        }

        public Builder logResponses(boolean logResponses) {
            this.logResponses = logResponses;
            return this;
        }

        public MistralAiClientConfig build() {
            return new MistralAiClientConfig(baseUrl, connectTimeout, readTimeout, apiKey, logRequests, logResponses);
        }
    }

}
