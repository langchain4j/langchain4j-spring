package dev.langchain4j.googleaigemini.spring;

import dev.langchain4j.model.googleai.GeminiMode;

import java.util.List;

public record GeminiFunctionCallingConfig(
        GeminiMode geminiMode,
        List<String> allowedFunctionNames
) {
}
