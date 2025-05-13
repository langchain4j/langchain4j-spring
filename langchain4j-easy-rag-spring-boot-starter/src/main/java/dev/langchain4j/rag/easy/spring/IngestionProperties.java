package dev.langchain4j.rag.easy.spring;

import org.springframework.boot.context.properties.NestedConfigurationProperty;

record IngestionProperties(
    @NestedConfigurationProperty
    DocumentsProperties documents
) {
}
