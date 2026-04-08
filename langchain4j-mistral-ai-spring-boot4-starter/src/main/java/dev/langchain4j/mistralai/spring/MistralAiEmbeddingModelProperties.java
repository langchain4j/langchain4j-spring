package dev.langchain4j.mistralai.spring;

import java.time.Duration;

public record MistralAiEmbeddingModelProperties(
        String baseUrl,
        String apiKey,
        String modelName,
        Duration timeout,
        Boolean logRequests,
        Boolean logResponses,
        Integer maxRetries
) {
}
