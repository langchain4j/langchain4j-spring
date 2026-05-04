package dev.langchain4j.googleaigemini.spring;

import java.time.Duration;
import java.util.List;
import java.util.Map;

public record ChatModelProperties(
        String apiKey,
        String baseUrl,
        String modelName,
        Double temperature,
        Double topP,
        Double frequencyPenalty,
        Double presencePenalty,
        Integer topK,
        Integer seed,
        Integer maxOutputTokens,
        Boolean allowCodeExecution,
        Boolean includeCodeExecutionOutput,
        Boolean logRequestsAndResponses,
        Integer maxRetries,
        Duration timeout,
        List<String> stopSequences,
        Map<String, String> safetySetting,
        GeminiFunctionCallingConfig functionCallingConfig
) {
}