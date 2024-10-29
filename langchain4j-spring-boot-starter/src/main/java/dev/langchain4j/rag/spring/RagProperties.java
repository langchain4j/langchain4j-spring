package dev.langchain4j.rag.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;


@ConfigurationProperties(prefix = RagProperties.PREFIX)
public record RagProperties(@NestedConfigurationProperty RetrievalProperties retrieval) {
    static final String PREFIX = "langchain4j.rag";

}
