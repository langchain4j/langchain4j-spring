package dev.langchain4j.mistralai.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(prefix = MistralAiProperties.PREFIX)
public class MistralAiProperties {

    public static final String PREFIX = "langchain4j.mistral-ai";

    @NestedConfigurationProperty
    private MistralAiChatModelProperties chatModel;

    @NestedConfigurationProperty
    private MistralAiChatModelProperties streamingChatModel;

    @NestedConfigurationProperty
    private MistralAiEmbeddingModelProperties embeddingModel;

    @NestedConfigurationProperty
    private FimModelProperties fimModel;

    @NestedConfigurationProperty
    private FimModelProperties streamingFimModel;

    @NestedConfigurationProperty
    private MistralAiModerationModelProperties moderationModel;

    public MistralAiChatModelProperties getChatModel() {
        return chatModel;
    }

    public void setChatModel(MistralAiChatModelProperties chatModel) {
        this.chatModel = chatModel;
    }

    public MistralAiChatModelProperties getStreamingChatModel() {
        return streamingChatModel;
    }

    public void setStreamingChatModel(MistralAiChatModelProperties streamingChatModel) {
        this.streamingChatModel = streamingChatModel;
    }

    public MistralAiEmbeddingModelProperties getEmbeddingModel() {
        return embeddingModel;
    }

    public void setEmbeddingModel(MistralAiEmbeddingModelProperties embeddingModel) {
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

    public MistralAiModerationModelProperties getModerationModel() {
        return moderationModel;
    }

    public void setModerationModel(MistralAiModerationModelProperties moderationModel) {
        this.moderationModel = moderationModel;
    }
}
