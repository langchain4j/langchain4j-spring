package dev.langchain4j.qianfan.spring;

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
    String secretKey;
    Double temperature;
    Integer maxRetries;
    Double topP;
    String modelName;
    String endpoint;
    String responseFormat;
    Double penaltyScore;
    Boolean logRequests;
    Boolean logResponses;
}