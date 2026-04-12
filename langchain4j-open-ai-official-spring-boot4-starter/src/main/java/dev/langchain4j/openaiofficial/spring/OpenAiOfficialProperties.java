package dev.langchain4j.openaiofficial.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(prefix = OpenAiOfficialProperties.PREFIX)
public record OpenAiOfficialProperties(

    @NestedConfigurationProperty
    OpenAiOfficialChatModelProperties chatModel,

    @NestedConfigurationProperty
    OpenAiOfficialChatModelProperties streamingChatModel,

    @NestedConfigurationProperty
    OpenAiOfficialEmbeddingModelProperties embeddingModel,

    @NestedConfigurationProperty
    OpenAiOfficialImageModelProperties imageModel
) {
    static final String PREFIX = "langchain4j.open-ai-official";

}
