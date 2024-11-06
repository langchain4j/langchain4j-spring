package dev.langchain4j.azure.openai.spring;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

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
    Map<String, String> customHeaders;
}