package dev.langchain4j.azure.openai.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(prefix = Properties.PREFIX)
public record Properties(

    @NestedConfigurationProperty
    ChatModelProperties chatModel,

    @NestedConfigurationProperty
    ChatModelProperties streamingChatModel,

    @NestedConfigurationProperty
    EmbeddingModelProperties embeddingModel,

    @NestedConfigurationProperty
    ImageModelProperties imageModel
) {
    static final String PREFIX = "langchain4j.azure-open-ai";
}
