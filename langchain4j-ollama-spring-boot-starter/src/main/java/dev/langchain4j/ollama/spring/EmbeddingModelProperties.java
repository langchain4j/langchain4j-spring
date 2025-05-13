package dev.langchain4j.ollama.spring;


import java.time.Duration;
import java.util.Map;

record EmbeddingModelProperties(
    String baseUrl,
    String modelName,
    Duration timeout,
    Integer maxRetries,
    Map<String, String> customHeaders,
    Boolean logRequests,
    Boolean logResponses
){
}