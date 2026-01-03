package dev.langchain4j.mistralai.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(prefix = Properties.PREFIX)
public class Properties {

    public static final String PREFIX = "langchain4j.mistral-ai";

    @NestedConfigurationProperty
    private ChatModelProperties chatModel;

    @NestedConfigurationProperty
    private ChatModelProperties streamingChatModel;

    @NestedConfigurationProperty
    private EmbeddingModelProperties embeddingModel;

    @NestedConfigurationProperty
    private FimModelProperties fimModel;

    @NestedConfigurationProperty
    private FimModelProperties streamingFimModel;

    @NestedConfigurationProperty
    private ModerationModelProperties moderationModel;

    public ChatModelProperties getChatModel() {
        return chatModel;
    }

    public void setChatModel(ChatModelProperties chatModel) {
        this.chatModel = chatModel;
    }

    public ChatModelProperties getStreamingChatModel() {
        return streamingChatModel;
    }

    public void setStreamingChatModel(ChatModelProperties streamingChatModel) {
        this.streamingChatModel = streamingChatModel;
    }

    public EmbeddingModelProperties getEmbeddingModel() {
        return embeddingModel;
    }

    public void setEmbeddingModel(EmbeddingModelProperties embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    public FimModelProperties getFimModel() {
        return fimModel;
    }

    public void setFimModel(FimModelProperties fimModel) {
        this.fimModel = fimModel;
    }

    public FimModelProperties getStreamingFimModel() {
        return streamingFimModel;
    }

    public void setStreamingFimModel(FimModelProperties streamingFimModel) {
        this.streamingFimModel = streamingFimModel;
    }

    public ModerationModelProperties getModerationModel() {
        return moderationModel;
    }

    public void setModerationModel(ModerationModelProperties moderationModel) {
        this.moderationModel = moderationModel;
    }

}
