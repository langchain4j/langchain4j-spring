package dev.langchain4j.azure.openai.spring;

import dev.langchain4j.model.chat.Capability;

import java.util.List;
import java.util.Map;
import java.util.Set;

record ChatModelProperties(

    String endpoint,
    String serviceVersion,
    String apiKey,
    String deploymentName,
    Integer maxTokens,
    Double temperature,
    Double topP,
    Map<String, Integer> logitBias,
    String user,
    List<String> stop,
    Double presencePenalty,
    Double frequencyPenalty,
    Long seed,
    Boolean strictJsonSchema,
    Integer timeout, // TODO use Duration instead
    Integer maxRetries,
    Boolean logRequestsAndResponses,
    String userAgentSuffix,
    Map<String, String> customHeaders,
    String nonAzureApiKey,
    Set<Capability> supportedCapabilities
) {
}