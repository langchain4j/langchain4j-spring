package dev.langchain4j.model.githubmodels.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(prefix = GitHubModelsProperties.PREFIX)
public class GitHubModelsProperties {

    static final String PREFIX = "langchain4j.github-models";

    @NestedConfigurationProperty
    private GitHubModelsChatModelProperties chatModel;

    @NestedConfigurationProperty
    private GitHubModelsChatModelProperties streamingChatModel;

    @NestedConfigurationProperty
    private GitHubModelsEmbeddingModelProperties embeddingModel;

    public GitHubModelsChatModelProperties getChatModel() {
        return chatModel;
    }

    public void setChatModel(GitHubModelsChatModelProperties chatModel) {
        this.chatModel = chatModel;
    }

    public GitHubModelsChatModelProperties getStreamingChatModel() {
        return streamingChatModel;
    }

    public void setStreamingChatModel(GitHubModelsChatModelProperties streamingChatModel) {
        this.streamingChatModel = streamingChatModel;
    }

    public GitHubModelsEmbeddingModelProperties getEmbeddingModel() {
        return embeddingModel;
    }

    public void setEmbeddingModel(GitHubModelsEmbeddingModelProperties embeddingModel) {
        this.embeddingModel = embeddingModel;
    }
}
