package dev.langchain4j.qianfan.spring;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.time.Duration;

@Getter
@Setter
class EmbeddingModelProperties {

    String baseUrl;
    String apiKey;
    String secretKey;
    Integer maxRetries;
    String modelName;
    String endpoint;
    String user;
    Boolean logRequests;
    Boolean logResponses;
}