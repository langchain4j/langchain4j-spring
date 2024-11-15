package dev.langchain4j.googleaigemini;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(prefix = Properties.PREFIX)
public class Properties {

    static final String PREFIX = "langchain4j.google-ai-gemini";

    @NestedConfigurationProperty
    private ChatModelProperties chatModel;

    @NestedConfigurationProperty
    private ChatModelProperties streamingChatModel;

    private String apiKey;

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