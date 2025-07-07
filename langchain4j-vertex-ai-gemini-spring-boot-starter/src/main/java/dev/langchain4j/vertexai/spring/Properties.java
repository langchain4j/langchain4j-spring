package dev.langchain4j.vertexai.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(prefix = Properties.PREFIX)
public record Properties(
    @NestedConfigurationProperty
    ChatModelProperties chatModel,
    @NestedConfigurationProperty
    ChatModelProperties streamingChatModel) {

    static final String PREFIX = "langchain4j.vertex-ai-gemini";

}
