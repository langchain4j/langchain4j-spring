package dev.langchain4j.store.embedding.chroma.spring;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = ChromaEmbeddingStoreProperties.PREFIX)
public class ChromaEmbeddingStoreProperties {

    static final String PREFIX = "langchain4j.chroma";
    static final String DEFAULT_BASE_URL = "http://localhost:8000";
    static final String DEFAULT_COLLECTION_NAME = "default";
    static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(5);

    private String baseUrl;
    private String collectionName;
    private Duration timeout;
    private Boolean logRequests;
    private Boolean logResponses;
}