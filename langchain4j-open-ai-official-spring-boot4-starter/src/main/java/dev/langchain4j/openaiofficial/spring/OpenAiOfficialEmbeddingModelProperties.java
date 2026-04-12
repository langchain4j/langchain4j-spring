package dev.langchain4j.openaiofficial.spring;

import java.time.Duration;
import java.util.Map;

record OpenAiOfficialEmbeddingModelProperties(
        String baseUrl,
        String apiKey,
        String organizationId,
        String modelName,
        Integer dimensions,
        String user,
        Integer maxSegmentsPerBatch,
        Duration timeout,
        Integer maxRetries,
        Map<String, String> customHeaders
) {
}
