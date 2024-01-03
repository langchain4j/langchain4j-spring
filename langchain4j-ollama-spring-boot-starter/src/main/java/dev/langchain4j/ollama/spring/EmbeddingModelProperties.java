package dev.langchain4j.ollama.spring;

import lombok.Getter;
import lombok.Setter;

import java.time.Duration;

@Getter
@Setter
class EmbeddingModelProperties {

    String baseUrl;
    String modelName;
    Duration timeout;
    Integer maxRetries;
}