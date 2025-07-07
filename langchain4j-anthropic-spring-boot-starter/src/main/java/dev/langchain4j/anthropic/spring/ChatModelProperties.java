package dev.langchain4j.anthropic.spring;

import java.time.Duration;
import java.util.List;

record ChatModelProperties(
    String baseUrl,
    String apiKey,
    String version,
    String beta,
    String modelName,
    Double temperature,
    Double topP,
    Integer topK,
    Integer maxTokens,
    List<String> stopSequences,
    Boolean cacheSystemMessages,
    Boolean cacheTools,
    String thinkingType,
    Integer thinkingBudgetTokens,
    Duration timeout,
    Integer maxRetries,
    Boolean logRequests,
    Boolean logResponses){
}