package dev.langchain4j.googleaigemini.spring;

import dev.langchain4j.model.googleai.GeminiHarmBlockThreshold;
import dev.langchain4j.model.googleai.GeminiHarmCategory;

public record GeminiSafetySetting(
        GeminiHarmCategory geminiHarmCategory,
        GeminiHarmBlockThreshold geminiHarmBlockThreshold
) {
}