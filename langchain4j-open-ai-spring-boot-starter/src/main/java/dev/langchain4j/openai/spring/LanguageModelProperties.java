package dev.langchain4j.openai.spring;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.time.Duration;

@Getter
@Setter
class LanguageModelProperties {

    String baseUrl;
    String apiKey;
    String organizationId;
    String modelName;
    Double temperature;
    Duration timeout;
    Integer maxRetries;
    @NestedConfigurationProperty
    ProxyProperties proxy;
    Boolean logRequests;
    Boolean logResponses;
}