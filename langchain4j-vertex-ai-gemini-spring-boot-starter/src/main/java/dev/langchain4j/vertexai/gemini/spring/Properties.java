package dev.langchain4j.vertexai.gemini.spring;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@Getter
@Setter
@ConfigurationProperties(prefix = Properties.PREFIX)
public class Properties {

    static final String PREFIX = "langchain4j.vertex-ai-gemini";

    @NestedConfigurationProperty
    ChatModelProperties chatModel;

    @NestedConfigurationProperty
    ChatModelProperties streamingChatModel;

}
