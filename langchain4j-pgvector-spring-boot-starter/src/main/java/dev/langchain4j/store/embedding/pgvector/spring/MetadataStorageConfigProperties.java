package dev.langchain4j.store.embedding.pgvector.spring;

import dev.langchain4j.store.embedding.pgvector.DefaultMetadataStorageConfig;
import dev.langchain4j.store.embedding.pgvector.MetadataStorageConfig;
import dev.langchain4j.store.embedding.pgvector.MetadataStorageMode;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.List;

@Getter
@Setter
@Builder
public class MetadataStorageConfigProperties {

    private MetadataStorageMode storageMode;

    private List<String> columnDefinitions;

    public static MetadataStorageConfigProperties defaultConfig() {
        return MetadataStorageConfigProperties.builder()
                .storageMode(MetadataStorageMode.COMBINED_JSON)
                .columnDefinitions(Collections.singletonList("metadata JSON NULL"))
                .build();
    }
}
