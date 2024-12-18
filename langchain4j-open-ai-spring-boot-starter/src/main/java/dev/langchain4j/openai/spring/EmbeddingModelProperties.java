package dev.langchain4j.openai.spring;

import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.time.Duration;
import java.util.Map;


record EmbeddingModelProperties(

    String baseUrl,
    String apiKey,
    String organizationId,
    String modelName,
    Integer dimensions,
    Integer maxSegmentsPerBatch,
    String user,
    Duration timeout,
    Integer maxRetries,
    @NestedConfigurationProperty
    ProxyProperties proxy,
    Boolean logRequests,
    Boolean logResponses,
    Map<String, String> customHeaders
) {

}