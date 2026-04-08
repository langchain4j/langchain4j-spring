package dev.langchain4j.azure.openai.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(prefix = AzureOpenAiProperties.PREFIX)
public record AzureOpenAiProperties(

    @NestedConfigurationProperty
    AzureOpenAiChatModelProperties chatModel,

    @NestedConfigurationProperty
    AzureOpenAiChatModelProperties streamingChatModel,

    @NestedConfigurationProperty
    AzureOpenAiEmbeddingModelProperties embeddingModel,

    @NestedConfigurationProperty
    AzureOpenAiImageModelProperties imageModel
) {
    static final String PREFIX = "langchain4j.azure-open-ai";
}
