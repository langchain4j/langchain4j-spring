package dev.langchain4j.openai.spring;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Getter
@Setter
class ChatModelProperties {

    String baseUrl;
    String apiKey;
    String organizationId;
    String modelName;
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
    @NestedConfigurationProperty
    ProxyProperties proxy;
    Boolean logRequests;
    Boolean logResponses;
}