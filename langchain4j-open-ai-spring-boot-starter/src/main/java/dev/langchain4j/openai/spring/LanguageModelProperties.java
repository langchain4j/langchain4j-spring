package dev.langchain4j.openai.spring;

import java.time.Duration;
import java.util.Map;

record LanguageModelProperties(
        String baseUrl,
        String apiKey,
        String organizationId,
        String modelName,
        Double temperature,
        Duration timeout,
        Integer maxRetries,
        Boolean logRequests,
        Boolean logResponses,
        Map<String, String> customHeaders
) {
}