package dev.langchain4j.ollama.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(prefix = OllamaProperties.PREFIX)
public class OllamaProperties {

    static final String PREFIX = "langchain4j.ollama";

    @NestedConfigurationProperty
    OllamaChatModelProperties chatModel;

    @NestedConfigurationProperty
    OllamaChatModelProperties streamingChatModel;

    @NestedConfigurationProperty
    OllamaLanguageModelProperties languageModel;

    @NestedConfigurationProperty
    OllamaLanguageModelProperties streamingLanguageModel;

    @NestedConfigurationProperty
    OllamaEmbeddingModelProperties embeddingModel;

    public OllamaChatModelProperties getChatModel() {
        return chatModel;
    }

    public void setChatModel(OllamaChatModelProperties chatModel) {
        this.chatModel = chatModel;
    }

    public OllamaChatModelProperties getStreamingChatModel() {
        return streamingChatModel;
    }

    public void setStreamingChatModel(OllamaChatModelProperties streamingChatModel) {
        this.streamingChatModel = streamingChatModel;
    }

    public OllamaLanguageModelProperties getLanguageModel() {
        return languageModel;
    }

    public void setLanguageModel(OllamaLanguageModelProperties languageModel) {
        this.languageModel = languageModel;
    }

    public OllamaLanguageModelProperties getStreamingLanguageModel() {
        return streamingLanguageModel;
    }

    public void setStreamingLanguageModel(OllamaLanguageModelProperties streamingLanguageModel) {
        this.streamingLanguageModel = streamingLanguageModel;
    }

    public OllamaEmbeddingModelProperties getEmbeddingModel() {
        return embeddingModel;
    }

    public void setEmbeddingModel(OllamaEmbeddingModelProperties embeddingModel) {
        this.embeddingModel = embeddingModel;
    }
}
