package dev.langchain4j.openai.spring;

import java.time.Duration;
import java.util.Map;

record ModerationModelProperties(
        String baseUrl,
        String apiKey,
        String organizationId,
        String projectId,
        String modelName,
        Duration timeout,
        Integer maxRetries,
        Boolean logRequests,
        Boolean logResponses,
        Map<String, String> customHeaders
) {
}