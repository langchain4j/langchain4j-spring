package dev.langchain4j.googleaigemini.spring;

import dev.langchain4j.model.googleai.GoogleAiEmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiEmbeddingModel.TaskType;

import java.time.Duration;

public record EmbeddingModelProperties( String titleMetadataKey,
                                        String modelName,
                                        Boolean logRequestsAndResponses,
                                        Integer maxRetries,
                                        Integer outputDimensionality,
                                        TaskType taskType,
                                        Duration timeout){}