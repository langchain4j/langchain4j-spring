package dev.langchain4j.openai.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(prefix = OpenAiProperties.PREFIX)
public record OpenAiProperties(

    @NestedConfigurationProperty
    OpenAiChatModelProperties chatModel,

    @NestedConfigurationProperty
    OpenAiChatModelProperties streamingChatModel,

    @NestedConfigurationProperty
    OpenAiLanguageModelProperties languageModel,

    @NestedConfigurationProperty
    OpenAiLanguageModelProperties streamingLanguageModel,

    @NestedConfigurationProperty
    OpenAiEmbeddingModelProperties embeddingModel,

    @NestedConfigurationProperty
    OpenAiModerationModelProperties moderationModel,

    @NestedConfigurationProperty
    OpenAiImageModelProperties imageModel
) {
    static final String PREFIX = "langchain4j.open-ai";

}
