package dev.langchain4j.store.embedding.chroma.spring;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = ChromaEmbeddingStoreProperties.PREFIX)
@Getter
@Setter
public class ChromaEmbeddingStoreProperties {

    static final String PREFIX = "langchain4j.chroma";

    static final String DEFAULT_BASE_URL = "http://localhost:8080/";
    static final String DEFAULT_COLLECTION_NAME = "langchain4j_collection";

    private String baseUrl;
    private String collectionName;
    private Boolean logRequests = false;
    private Boolean logResponses = false;
    private Integer timeoutInSeconds;
}
