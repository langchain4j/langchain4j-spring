package dev.langchain4j.rag.easy.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import static dev.langchain4j.rag.easy.spring.EasyRagProperties.PREFIX;

@ConfigurationProperties(prefix = PREFIX)
public class EasyRagProperties {

    static final String PREFIX = "langchain4j.easy-rag";

    @NestedConfigurationProperty
    IngestionProperties ingestion;

    public IngestionProperties getIngestion() {
        return ingestion;
    }

    public void setIngestion(IngestionProperties ingestion) {
        this.ingestion = ingestion;
    }
}
