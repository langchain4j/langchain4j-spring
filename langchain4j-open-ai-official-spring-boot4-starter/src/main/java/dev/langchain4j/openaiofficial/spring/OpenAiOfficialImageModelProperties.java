package dev.langchain4j.openaiofficial.spring;

import java.time.Duration;
import java.util.Map;

record OpenAiOfficialImageModelProperties(
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
        Map<String, String> customHeaders
) {
}
