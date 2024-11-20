package dev.langchain4j.googleaigemini.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(prefix = Properties.PREFIX)
public class Properties {

    static final String PREFIX = "langchain4j.google-ai-gemini";

    @NestedConfigurationProperty
    private ChatModelProperties chatModel;

    @NestedConfigurationProperty
    private ChatModelProperties streamingChatModel;

    @NestedConfigurationProperty
    private GeminiSafetySetting safetySetting;

    @NestedConfigurationProperty
    private EmbeddingModelProperties embeddingModel;

    @NestedConfigurationProperty
    private GeminiFunctionCallingConfig functionCallingConfig;

    private String apiKey;

    public EmbeddingModelProperties getEmbeddingModel() {
        return embeddingModel;
    }

    public void setEmbeddingModel(EmbeddingModelProperties embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    public GeminiSafetySetting getSafetySetting() {
        return safetySetting;
    }

    public void setSafetySetting(GeminiSafetySetting safetySetting) {
        this.safetySetting = safetySetting;
    }

    public GeminiFunctionCallingConfig getFunctionCallingConfig() {
        return functionCallingConfig;
    }

    public void setFunctionCallingConfig(GeminiFunctionCallingConfig functionCallingConfig) {
        this.functionCallingConfig = functionCallingConfig;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public ChatModelProperties getStreamingChatModel() {
        return streamingChatModel;
    }

    public void setStreamingChatModel(ChatModelProperties streamingChatModel) {
        this.streamingChatModel = streamingChatModel;
    }

    public ChatModelProperties getChatModel() {
        return chatModel;
    }

    public void setChatModel(ChatModelProperties chatModel) {
        this.chatModel = chatModel;
    }
}