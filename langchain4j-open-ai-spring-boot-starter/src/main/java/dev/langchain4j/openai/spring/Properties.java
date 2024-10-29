package dev.langchain4j.openai.spring;

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
    EmbeddingModelProperties embeddingModel,

    @NestedConfigurationProperty
    ModerationModelProperties moderationModel,

    @NestedConfigurationProperty
    ImageModelProperties imageModel
) {
    static final String PREFIX = "langchain4j.open-ai";

}
