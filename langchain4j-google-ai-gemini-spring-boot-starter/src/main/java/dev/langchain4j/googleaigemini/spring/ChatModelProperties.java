package dev.langchain4j.googleaigemini.spring;

import dev.langchain4j.model.chat.request.ResponseFormat;

import java.time.Duration;
import java.util.Map;

record ChatModelProperties(
        String apiKey,
        String modelName,
        Double temperature,
        Double topP,
        Integer topK,
        Integer maxOutputTokens,
        Boolean logRequestsAndResponses,
        Integer maxRetries,
        Duration timeout,
        Map<String,String> safetySetting,
        GeminiFunctionCallingConfig functionCallingConfig
) {
}