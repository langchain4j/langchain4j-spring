package dev.langchain4j.azure.openai.spring;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
class EmbeddingModelProperties {

    String endpoint;
    String apiKey;
    String nonAzureApiKey;
    String deploymentName;
    Integer dimensions;
    Integer timeout;
    Integer maxRetries;
    Boolean logRequestsAndResponses;
}