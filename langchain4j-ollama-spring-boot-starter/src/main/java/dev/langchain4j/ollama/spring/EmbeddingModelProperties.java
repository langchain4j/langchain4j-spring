package dev.langchain4j.ollama.spring;

import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.util.Map;

@Getter
@Setter
class EmbeddingModelProperties {

    String baseUrl;
    String modelName;
    Duration timeout;
    Integer maxRetries;
    Map<String, String> customHeaders;
    Boolean logRequests;
    Boolean logResponses;
}