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
    String apiVersion;
    String organizationId;
    String modelName;
    Double temperature;
    Double topP;
    List<String> stop;
    Integer maxTokens;
    Integer maxCompletionTokens;
    Double presencePenalty;
    Double frequencyPenalty;
    Map<String, Integer> logitBias;
    String responseFormat;
    Boolean strictJsonSchema;
    Integer seed;
    String user;
    Boolean strictTools;
    Boolean parallelToolCalls;
    Duration timeout;
    Integer maxRetries;
    @NestedConfigurationProperty
    ProxyProperties proxy;
    Boolean logRequests;
    Boolean logResponses;
    Map<String, String> customHeaders;
}
