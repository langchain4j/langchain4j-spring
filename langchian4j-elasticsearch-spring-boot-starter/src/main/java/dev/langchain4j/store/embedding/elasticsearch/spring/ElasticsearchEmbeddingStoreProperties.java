package dev.langchain4j.store.embedding.elasticsearch.spring;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = ElasticsearchEmbeddingStoreProperties.PREFIX)
public class ElasticsearchEmbeddingStoreProperties {

    static final String PREFIX = "langchain4j.elasticsearch";

    private String serverUrl;
    private String apiKey;
    private String userName;
    private String password;
    private String indexName = "default";
    private Integer dimension;
}
