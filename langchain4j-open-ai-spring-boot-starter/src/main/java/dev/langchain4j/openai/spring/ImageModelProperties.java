package dev.langchain4j.openai.spring;

import java.time.Duration;
import java.util.Map;

record ImageModelProperties(
        String baseUrl,
        String apiKey,
        String organizationId,
        String projectId,
        String modelName,
        String size,
        String quality,
        String user,
        String background,
        String outputFormat,
        Integer outputCompression,
        String moderation,
        Duration timeout,
        Integer maxRetries,
        Boolean logRequests,
        Boolean logResponses,
        Map<String, String> customHeaders,
        Map<String, String> customQueryParams
) {
}