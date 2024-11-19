package dev.langchain4j.googleaigemini.spring;

import dev.langchain4j.model.googleai.GeminiHarmBlockThreshold;
import dev.langchain4j.model.googleai.GeminiHarmCategory;

public class GeminiSafetySetting {

    private GeminiHarmCategory geminiHarmCategory;
    private GeminiHarmBlockThreshold geminiHarmBlockThreshold;

    public GeminiHarmCategory getGeminiHarmCategory() {
        return geminiHarmCategory;
    }

    public void setGeminiHarmCategory(GeminiHarmCategory geminiHarmCategory) {
        this.geminiHarmCategory = geminiHarmCategory;
    }

    public GeminiHarmBlockThreshold getGeminiHarmBlockThreshold() {
        return geminiHarmBlockThreshold;
    }

    public void setGeminiHarmBlockThreshold(GeminiHarmBlockThreshold geminiHarmBlockThreshold) {
        this.geminiHarmBlockThreshold = geminiHarmBlockThreshold;
    }
}
