package dev.langchain4j.ollama.spring;

import java.time.Duration;
import java.util.List;
import java.util.Map;

record LanguageModelProperties(
    String baseUrl,
    String modelName,
    Double temperature,
    Integer topK,
    Double topP,
    Double repeatPenalty,
    Integer seed,
    Integer numPredict,
    List<String> stop,
    String format,
    Duration timeout,
    Integer maxRetries,
    Map<String, String> customHeaders,
    Boolean logRequests,
    Boolean logResponses) {

}
