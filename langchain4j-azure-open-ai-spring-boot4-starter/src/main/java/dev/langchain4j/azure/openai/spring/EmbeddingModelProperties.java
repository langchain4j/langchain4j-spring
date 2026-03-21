package dev.langchain4j.azure.openai.spring;

import java.util.Map;

record EmbeddingModelProperties(

    String endpoint,
    String serviceVersion,
    String apiKey,
    String deploymentName,
    Integer timeout, // TODO use duration instead
    Integer maxRetries,
    Boolean logRequestsAndResponses,
    String userAgentSuffix,
    Integer dimensions,
    Map<String, String> customHeaders,
    String nonAzureApiKey
) {
}