package dev.langchain4j.ollama.spring;

import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Getter
@Setter
class LanguageModelProperties {

    String baseUrl;
    String modelName;
    Double temperature;
    Integer topK;
    Double topP;
    Double repeatPenalty;
    Integer seed;
    Integer numPredict;
    List<String> stop;
    String format;
    Duration timeout;
    Integer maxRetries;
    Map<String, String> customHeaders;
    Boolean logRequests;
    Boolean logResponses;
}