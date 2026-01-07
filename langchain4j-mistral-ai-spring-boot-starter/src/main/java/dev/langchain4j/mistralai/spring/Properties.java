package dev.langchain4j.mistralai.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(prefix = Properties.PREFIX)
public class Properties {

    public static final String PREFIX = "langchain4j.mistral-ai";

    @NestedConfigurationProperty
    ChatModelProperties chatModel;

    @NestedConfigurationProperty
    ChatModelProperties streamingChatModel;

    @NestedConfigurationProperty
    EmbeddingModelProperties embeddingModel;

    @NestedConfigurationProperty
    FimModelProperties fimModel;

    @NestedConfigurationProperty
    FimModelProperties streamingFimModel;

    @NestedConfigurationProperty
    ModerationModelProperties moderationModel;

    public ChatModelProperties getChatModel() {
        return this.chatModel;
    }

    public ChatModelProperties getStreamingChatModel() {
        return this.streamingChatModel;
    }

    public EmbeddingModelProperties getEmbeddingModel() {
        return this.embeddingModel;
    }

    public FimModelProperties getFimModel() {
        return this.fimModel;
    }

    public FimModelProperties getStreamingFimModel() {
        return this.streamingFimModel;
    }

    public ModerationModelProperties getModerationModel() {
        return this.moderationModel;
    }

    public void setChatModel(ChatModelProperties chatModel) {
        this.chatModel = chatModel;
    }

    public void setStreamingChatModel(ChatModelProperties streamingChatModel) {
        this.streamingChatModel = streamingChatModel;
    }

    public void setEmbeddingModel(EmbeddingModelProperties embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    public void setFimModel(FimModelProperties fimModel) {
        this.fimModel = fimModel;
    }

    public void setStreamingFimModel(FimModelProperties streamingFimModel) {
        this.streamingFimModel = streamingFimModel;
    }

    public void setModerationModel(ModerationModelProperties moderationModel) {
        this.moderationModel = moderationModel;
    }
}
