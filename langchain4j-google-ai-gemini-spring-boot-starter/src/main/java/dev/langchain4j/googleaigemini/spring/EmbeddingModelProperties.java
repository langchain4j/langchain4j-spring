package dev.langchain4j.googleaigemini.spring;

import dev.langchain4j.model.googleai.GoogleAiEmbeddingModel.TaskType;

import java.time.Duration;

public record EmbeddingModelProperties(
        String apiKey,
        String baseUrl,
        String modelName,
        String titleMetadataKey,
        Boolean logRequestsAndResponses,
        Boolean logRequests,
        Boolean logResponses,
        Integer maxRetries,
        Integer outputDimensionality,
        TaskType taskType,
        Duration timeout
) {
}