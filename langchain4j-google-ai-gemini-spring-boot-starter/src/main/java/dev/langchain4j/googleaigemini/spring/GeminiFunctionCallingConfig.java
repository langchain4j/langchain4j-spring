package dev.langchain4j.googleaigemini.spring;

import dev.langchain4j.model.googleai.GeminiMode;

import java.util.List;

public class GeminiFunctionCallingConfig {

    private GeminiMode geminiMode;
    private List<String> allowedFunctionNames;

    public GeminiMode getGeminiMode() {
        return geminiMode;
    }

    public void setGeminiMode(GeminiMode geminiMode) {
        this.geminiMode = geminiMode;
    }

    public List<String> getAllowedFunctionNames() {
        return allowedFunctionNames;
    }

    public void setAllowedFunctionNames(List<String> allowedFunctionNames) {
        this.allowedFunctionNames = allowedFunctionNames;
    }
}
