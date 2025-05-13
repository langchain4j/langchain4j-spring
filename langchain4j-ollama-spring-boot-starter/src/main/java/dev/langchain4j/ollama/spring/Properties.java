package dev.langchain4j.ollama.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(prefix = Properties.PREFIX)
public record Properties(
    @NestedConfigurationProperty
    ChatModelProperties chatModel,
    @NestedConfigurationProperty
    ChatModelProperties streamingChatModel,
    @NestedConfigurationProperty
    LanguageModelProperties languageModel,
    @NestedConfigurationProperty
    LanguageModelProperties streamingLanguageModel,
    @NestedConfigurationProperty
    EmbeddingModelProperties embeddingModel) {

    static final String PREFIX = "langchain4j.ollama";

}
