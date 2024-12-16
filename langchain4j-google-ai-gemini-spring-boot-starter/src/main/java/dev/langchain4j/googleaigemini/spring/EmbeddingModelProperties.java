package dev.langchain4j.googleaigemini.spring;

import dev.langchain4j.model.googleai.GoogleAiEmbeddingModel.TaskType;

import java.time.Duration;

public record EmbeddingModelProperties(
        String apiKey,
        String modelName,
        String titleMetadataKey,
        Boolean logRequestsAndResponses,
        Integer maxRetries,
        Integer outputDimensionality,
        TaskType taskType,
        Duration timeout
) {
}