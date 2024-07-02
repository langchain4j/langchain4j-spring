package dev.langchain4j.qianfan.spring;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.time.Duration;

@Getter
@Setter
class LanguageModelProperties {

    String baseUrl;
    String apiKey;
    String secretKey;
    Double temperature;
    Integer maxRetries;
    Integer topK;
    Double topP;
    String modelName;
    String endpoint;
    Double penaltyScore;
    Boolean logRequests;
    Boolean logResponses;
}