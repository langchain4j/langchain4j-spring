package dev.langchain4j.rag.easy.spring;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@Getter
@Setter
class IngestionProperties {

    @NestedConfigurationProperty
    DocumentsProperties documents;
}
