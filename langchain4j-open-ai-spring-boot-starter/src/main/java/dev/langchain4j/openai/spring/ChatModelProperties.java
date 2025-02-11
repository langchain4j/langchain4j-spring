package dev.langchain4j.openai.spring;

import java.time.Duration;
import java.util.List;
import java.util.Map;

record ChatModelProperties(
        String baseUrl,
        String apiKey,
        String organizationId,
        String modelName,
        Double temperature,
        Double topP,
        List<String> stop,
        Integer maxTokens,
        Integer maxCompletionTokens,
        Double presencePenalty,
        Double frequencyPenalty,
        Map<String, Integer> logitBias,
        String responseFormat,
        Boolean strictJsonSchema,
        Integer seed,
        String user,
        Boolean strictTools,
        Boolean parallelToolCalls,
        Boolean store,
        Map<String, String> metadata,
        String serviceTier,
        String reasoningEffort,
        Duration timeout,
        Integer maxRetries,
        Boolean logRequests,
        Boolean logResponses,
        Map<String, String> customHeaders
) {
}