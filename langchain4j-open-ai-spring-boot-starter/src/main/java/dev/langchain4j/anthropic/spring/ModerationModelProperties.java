package dev.langchain4j.anthropic.spring;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.time.Duration;

@Getter
@Setter
class ModerationModelProperties {

    String baseUrl;
    String apiKey;
    String organizationId;
    String modelName;
    Duration timeout;
    Integer maxRetries;
    @NestedConfigurationProperty
    ProxyProperties proxy;
    Boolean logRequests;
    Boolean logResponses;
}