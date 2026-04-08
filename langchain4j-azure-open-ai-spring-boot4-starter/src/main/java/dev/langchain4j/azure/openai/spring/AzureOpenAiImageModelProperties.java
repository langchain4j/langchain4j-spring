package dev.langchain4j.azure.openai.spring;

import java.util.Map;

record AzureOpenAiImageModelProperties(

    String endpoint,
    String serviceVersion,
    String apiKey,
    String deploymentName,
    String quality,
    String size,
    String user,
    String style,
    String responseFormat,
    Integer timeout,
    Integer maxRetries,
    Boolean logRequestsAndResponses,
    String userAgentSuffix,
    Map<String, String> customHeaders,
    String nonAzureApiKey
){
}