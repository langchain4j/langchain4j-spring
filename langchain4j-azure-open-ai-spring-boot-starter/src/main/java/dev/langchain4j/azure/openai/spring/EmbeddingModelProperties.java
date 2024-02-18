package dev.langchain4j.azure.openai.spring;

import lombok.Getter;
import lombok.Setter;

import java.time.Duration;

@Getter
@Setter
class EmbeddingModelProperties {

    String endpoint;
    String apiKey;
    String organizationId;
    String deploymentName;
    Integer dimensions;
    String user;
    Duration timeout;
    Integer maxRetries;
    Boolean logRequestsAndResponses;
}