package dev.langchain4j.googleaigemini.spring;

import dev.langchain4j.model.googleai.GeminiMode;

import java.util.List;

public record GoogleAiGeminiFunctionCallingConfig(
        GeminiMode geminiMode,
        List<String> allowedFunctionNames
) {
}
