package dev.langchain4j.openaiofficial.spring;

import java.time.Duration;
import java.util.Map;

record ImageModelProperties(
        String baseUrl,
        String apiKey,
        String organizationId,
        String modelName,
        String size,
        String quality,
        String user,
        String background,
        String outputFormat,
        Long outputCompression,
        String moderation,
        Duration timeout,
        Integer maxRetries,
        Map<String, String> customHeaders
) {
}
