package dev.langchain4j.anthropic.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(prefix = AnthropicProperties.PREFIX)
public class AnthropicProperties {

    static final String PREFIX = "langchain4j.anthropic";

    @NestedConfigurationProperty
    AnthropicChatModelProperties chatModel;

    @NestedConfigurationProperty
    AnthropicChatModelProperties streamingChatModel;

    public AnthropicChatModelProperties getChatModel() {
        return chatModel;
    }

    public void setChatModel(AnthropicChatModelProperties chatModel) {
        this.chatModel = chatModel;
    }

    public AnthropicChatModelProperties getStreamingChatModel() {
        return streamingChatModel;
    }

    public void setStreamingChatModel(AnthropicChatModelProperties streamingChatModel) {
        this.streamingChatModel = streamingChatModel;
    }
}
