package dev.langchain4j.rag.easy.spring;

import org.springframework.boot.context.properties.NestedConfigurationProperty;

class IngestionProperties {

    @NestedConfigurationProperty
    DocumentsProperties documents;

    public DocumentsProperties getDocuments() {
        return documents;
    }

    public void setDocuments(DocumentsProperties documents) {
        this.documents = documents;
    }
}
