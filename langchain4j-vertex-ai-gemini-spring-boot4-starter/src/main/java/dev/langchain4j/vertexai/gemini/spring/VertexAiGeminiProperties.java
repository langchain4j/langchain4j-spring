package dev.langchain4j.vertexai.gemini.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(prefix = VertexAiGeminiProperties.PREFIX)
public class VertexAiGeminiProperties {

    static final String PREFIX = "langchain4j.vertex-ai-gemini";

    @NestedConfigurationProperty
    VertexAiGeminiChatModelProperties chatModel;

    @NestedConfigurationProperty
    VertexAiGeminiChatModelProperties streamingChatModel;

    public VertexAiGeminiChatModelProperties getChatModel() {
        return chatModel;
    }

    public void setChatModel(VertexAiGeminiChatModelProperties chatModel) {
        this.chatModel = chatModel;
    }

    public VertexAiGeminiChatModelProperties getStreamingChatModel() {
        return streamingChatModel;
    }

    public void setStreamingChatModel(VertexAiGeminiChatModelProperties streamingChatModel) {
        this.streamingChatModel = streamingChatModel;
    }
}
