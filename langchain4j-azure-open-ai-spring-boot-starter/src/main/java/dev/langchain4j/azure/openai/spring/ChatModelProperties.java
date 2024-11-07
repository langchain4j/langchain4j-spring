package dev.langchain4j.azure.openai.spring;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
class ChatModelProperties {

    String endpoint;
    String serviceVersion;
    String apiKey;
    String deploymentName;
    Integer maxTokens;
    Double temperature;
    Double topP;
    Map<String, Integer> logitBias;
    String user;
    List<String> stop;
    Double presencePenalty;
    Double frequencyPenalty;
    Long seed;
    String responseFormat;
    Integer timeout; // TODO use Duration instead
    Integer maxRetries;
    Boolean logRequestsAndResponses;
    String userAgentSuffix;
    Map<String, String> customHeaders;
    String nonAzureApiKey;
}