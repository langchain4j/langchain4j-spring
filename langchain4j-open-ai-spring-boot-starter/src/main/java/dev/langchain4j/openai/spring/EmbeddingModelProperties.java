package dev.langchain4j.openai.spring;

import java.time.Duration;
import java.util.Map;

record EmbeddingModelProperties(
        String baseUrl,
        String apiKey,
        String organizationId,
        String projectId,
        String modelName,
        Integer dimensions,
        Integer maxSegmentsPerBatch,
        String user,
        Duration timeout,
        Integer maxRetries,
        Boolean logRequests,
        Boolean logResponses,
        Map<String, String> customHeaders,
        Map<String, String> customQueryParams
) {

}