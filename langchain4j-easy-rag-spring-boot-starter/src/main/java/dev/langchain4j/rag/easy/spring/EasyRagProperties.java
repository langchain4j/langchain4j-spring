package dev.langchain4j.rag.easy.spring;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import static dev.langchain4j.rag.easy.spring.EasyRagProperties.PREFIX;

@Getter
@Setter
@ConfigurationProperties(prefix = PREFIX)
public class EasyRagProperties {

    static final String PREFIX = "langchain4j.easy-rag";

    @NestedConfigurationProperty
    IngestionProperties ingestion;
}
