package dev.langchain4j.googleaigemini.spring;

import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.googleai.GeminiHarmBlockThreshold;
import dev.langchain4j.model.googleai.GeminiHarmCategory;

import java.time.Duration;
import java.util.Map;

record ChatModelProperties(
        String apiKey,
        String modelName,
        Double temperature,
        Double topP,
        Integer topK,
        Integer maxOutputTokens,
        ResponseFormat responseFormat,
        Boolean logRequestsAndResponses,
        Integer maxRetries,
        Duration timeout,
        Map<String,String> safetySetting,
        GeminiFunctionCallingConfig functionCallingConfig
) {
}