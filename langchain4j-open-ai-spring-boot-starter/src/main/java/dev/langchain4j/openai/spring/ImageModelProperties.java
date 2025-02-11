package dev.langchain4j.openai.spring;

import java.time.Duration;
import java.util.Map;

record ImageModelProperties(
        String baseUrl,
        String apiKey,
        String organizationId,
        String modelName,
        String size,
        String quality,
        String style,
        String user,
        String responseFormat,
        Duration timeout,
        Integer maxRetries,
        Boolean logRequests,
        Boolean logResponses,
        Map<String, String> customHeaders
) {
}