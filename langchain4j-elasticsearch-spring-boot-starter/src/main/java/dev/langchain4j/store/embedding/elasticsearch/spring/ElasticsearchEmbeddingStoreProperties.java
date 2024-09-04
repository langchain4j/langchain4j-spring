package dev.langchain4j.store.embedding.elasticsearch.spring;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = ElasticsearchEmbeddingStoreProperties.PREFIX)
public class ElasticsearchEmbeddingStoreProperties {

    static final String PREFIX = "langchain4j.elasticsearch";
    static final String DEFAULT_SERVER_URL = "https://localhost:9200";
    static final String DEFAULT_INDEX_NAME = "langchain4j-index";
    static final String DEFAULT_USERNAME = "elastic";

    private String serverUrl;
    private String apiKey;
    private String username;
    private String password;
    private String indexName;
    private Boolean checkSslCertificates;
    private String caCertificateAsBase64String;
}
