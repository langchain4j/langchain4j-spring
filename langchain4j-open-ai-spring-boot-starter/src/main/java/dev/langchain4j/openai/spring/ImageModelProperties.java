package dev.langchain4j.openai.spring;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;

@Getter
@Setter
class ImageModelProperties {

    String baseUrl;
    String apiKey;
    String organizationId;
    String modelName;
    String size;
    String quality;
    String style;
    String user;
    String responseFormat;
    Duration timeout;
    Integer maxRetries;
    @NestedConfigurationProperty
    ProxyProperties proxy;
    Boolean logRequests;
    Boolean logResponses;
    Boolean withPersisting;
    Path persistTo;
    Map<String, String> customHeaders;
}