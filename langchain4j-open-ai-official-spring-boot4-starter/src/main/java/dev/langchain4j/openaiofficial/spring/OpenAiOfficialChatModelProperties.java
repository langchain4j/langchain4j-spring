package dev.langchain4j.openaiofficial.spring;

import dev.langchain4j.model.chat.Capability;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;

record OpenAiOfficialChatModelProperties(
        String baseUrl,
        String apiKey,
        String organizationId,
        Boolean isMicrosoftFoundry,
        String microsoftFoundryDeploymentName,
        String modelName,
        Double temperature,
        Double topP,
        List<String> stop,
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
        Set<Capability> supportedCapabilities,
        Duration timeout,
        Integer maxRetries,
        Map<String, String> customHeaders
) {
}
