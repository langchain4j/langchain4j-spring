package dev.langchain4j.googleaigemini.spring;

import dev.langchain4j.googleaigemini.spring.GeminiSafetySetting;
import dev.langchain4j.googleaigemini.spring.GeminiFunctionCallingConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.List;

@ConfigurationProperties(prefix = Properties.PREFIX)
public class Properties {

    static final String PREFIX = "langchain4j.google-ai-gemini";

    @NestedConfigurationProperty
    private ChatModelProperties chatModel;

    @NestedConfigurationProperty
    private ChatModelProperties streamingChatModel;

    private String apiKey;

    @NestedConfigurationProperty
    private GeminiSafetySetting safetySetting;

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

    @NestedConfigurationProperty
    private GeminiFunctionCallingConfig functionCallingConfig;

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