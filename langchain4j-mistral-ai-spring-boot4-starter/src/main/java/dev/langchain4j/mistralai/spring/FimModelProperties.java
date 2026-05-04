package dev.langchain4j.mistralai.spring;

import java.time.Duration;
import java.util.List;

public record FimModelProperties(
        String baseUrl,
        String apiKey,
        String modelName,
        Double temperature,
        Integer maxTokens,
        Integer minTokens,
        Double topP,
        Integer randomSeed,
        List<String> stop,
        Duration timeout,
        Boolean logRequests,
        Boolean logResponses,
        Integer maxRetries
) {
}
