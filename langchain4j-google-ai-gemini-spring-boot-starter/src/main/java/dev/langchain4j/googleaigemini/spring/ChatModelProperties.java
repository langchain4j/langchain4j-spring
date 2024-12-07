package dev.langchain4j.googleaigemini.spring;

import dev.langchain4j.model.chat.request.ResponseFormat;

import java.time.Duration;


public record ChatModelProperties(
        String modelName,
        Double temperature,
        Double topP,
        Integer topK,
        Integer maxOutputTokens,
        ResponseFormat responseFormat,
        Boolean logRequestsAndResponses,
        Integer maxRetries,
        Duration timeout,
        GeminiSafetySetting safetySetting,
        GeminiFunctionCallingConfig functionCallingConfig
) {
}