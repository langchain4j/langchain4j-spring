package dev.langchain4j.azure.openai.spring;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

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
    int timeout;
    Integer maxRetries;
    Boolean logRequestsAndResponses;
}