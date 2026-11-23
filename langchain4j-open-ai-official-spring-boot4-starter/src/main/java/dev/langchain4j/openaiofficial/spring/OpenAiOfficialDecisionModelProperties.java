package dev.langchain4j.openaiofficial.spring;

import java.time.Duration;
import java.util.Map;

record OpenAiOfficialDecisionModelProperties(
        String baseUrl,
        String apiKey,
        String organizationId,
        String modelName,
        Duration timeout,
        Integer maxRetries,
        Map<String, String> customHeaders
) {
}
