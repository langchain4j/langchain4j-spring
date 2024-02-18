package dev.langchain4j.azure.openai.spring;

import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Getter
@Setter
class ChatModelProperties {

    String endpoint;
    String apiKey;
    String organizationId;
    String deploymentName;
    Double temperature;
    Double topP;
    List<String> stop;
    Integer maxTokens;
    Double presencePenalty;
    Double frequencyPenalty;
    Map<String, Integer> logitBias;
    String responseFormat;
    Integer seed;
    String user;
    Duration timeout;
    Integer maxRetries;
    Boolean logRequestsAndResponses;
}