package dev.langchain4j.mistralai.spring;

import dev.langchain4j.model.chat.Capability;
import dev.langchain4j.model.chat.request.ResponseFormat;

import java.time.Duration;
import java.util.List;
import java.util.Set;

public record ChatModelProperties(
        String baseUrl,
        String apiKey,
        String modelName,
        Double temperature,
        Double topP,
        Integer maxTokens,
        Boolean safePrompt,
        Integer randomSeed,
        ResponseFormat responseFormat,
        List<String> stopSequences,
        Double frequencyPenalty,
        Double presencePenalty,
        Duration timeout,
        Boolean logRequests,
        Boolean logResponses,
        Integer maxRetries,
        Set<Capability> supportedCapabilities
) {
}
