package dev.langchain4j.googleaigemini.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(prefix = GoogleAiGeminiProperties.PREFIX)
public class GoogleAiGeminiProperties {

    static final String PREFIX = "langchain4j.google-ai-gemini";

    @NestedConfigurationProperty
    private GoogleAiGeminiChatModelProperties chatModel;

    @NestedConfigurationProperty
    private GoogleAiGeminiChatModelProperties streamingChatModel;

    @NestedConfigurationProperty
    private GoogleAiGeminiEmbeddingModelProperties embeddingModel;

    public GoogleAiGeminiEmbeddingModelProperties getEmbeddingModel() {
        return embeddingModel;
    }

    public void setEmbeddingModel(GoogleAiGeminiEmbeddingModelProperties embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    public GoogleAiGeminiChatModelProperties getStreamingChatModel() {
        return streamingChatModel;
    }

    public void setStreamingChatModel(GoogleAiGeminiChatModelProperties streamingChatModel) {
        this.streamingChatModel = streamingChatModel;
    }

    public GoogleAiGeminiChatModelProperties getChatModel() {
        return chatModel;
    }

    public void setChatModel(GoogleAiGeminiChatModelProperties chatModel) {
        this.chatModel = chatModel;
    }
}