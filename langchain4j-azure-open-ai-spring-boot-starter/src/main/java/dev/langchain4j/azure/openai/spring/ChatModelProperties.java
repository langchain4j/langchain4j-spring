package dev.langchain4j.azure.openai.spring;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
class ChatModelProperties {

    String endpoint;
    String apiKey;
    String nonAzureApiKey;
    String organizationId;
    String deploymentName;
    Double temperature;
    Double topP;
    Integer maxTokens;
    Double presencePenalty;
    Double frequencyPenalty;
    String responseFormat;
    Integer seed;
    List<String> stop;
    Integer timeout;
    Integer maxRetries;
    Boolean logRequestsAndResponses;
    Map<String, String> customHeaders;
}