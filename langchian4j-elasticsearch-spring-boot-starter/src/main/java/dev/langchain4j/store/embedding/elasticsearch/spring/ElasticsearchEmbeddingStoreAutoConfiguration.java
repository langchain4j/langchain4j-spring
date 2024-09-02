package dev.langchain4j.store.embedding.elasticsearch.spring;

import dev.langchain4j.store.embedding.elasticsearch.ElasticsearchEmbeddingStore;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.Base64;
import java.util.Optional;

import static dev.langchain4j.store.embedding.elasticsearch.spring.ElasticsearchEmbeddingStoreProperties.*;

@AutoConfiguration
@EnableConfigurationProperties(ElasticsearchEmbeddingStoreProperties.class)
@ConditionalOnProperty(prefix = PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
public class ElasticsearchEmbeddingStoreAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ElasticsearchEmbeddingStoreAutoConfiguration.class);

    /**
     * Create a bean for the Elasticsearch Rest Client if it does not exist yet
     * but ideally this should be created before
     * @param properties the properties
     * @return a RestClient instance
     */
    @Bean
    @ConditionalOnMissingBean
    public RestClient elasticsearchRestClient(ElasticsearchEmbeddingStoreProperties properties) {
        String serverUrl = Optional.ofNullable(properties.getServerUrl()).orElse(DEFAULT_SERVER_URL);
        String username = Optional.ofNullable(properties.getUsername()).orElse(DEFAULT_USERNAME);
        boolean checkSslCertificates = Optional.ofNullable(properties.getCheckSslCertificates()).orElse(true);

        if (!checkSslCertificates) {
            log.warn("disabling ssl checks is a bad practice in general and should be done ONLY in the context of tests.");
        }

        // If we have a self-signed certificate, we can provide it
        byte[] caCertificate = null;
        String caCertificateAsBase64String = properties.getCaCertificateAsBase64String();
        if (caCertificateAsBase64String != null) {
            caCertificate = Base64.getDecoder().decode(caCertificateAsBase64String);
        }

        log.debug("create RestClient running at [{}] with api key [{}], username [{}].",
                serverUrl, properties.getApiKey(), username);

        return ElasticsearchClientHelper.getClient(
                serverUrl,
                properties.getApiKey(),
                username,
                properties.getPassword(),
                checkSslCertificates,
                caCertificate);
    }

    @Bean
    @ConditionalOnMissingBean
    public ElasticsearchEmbeddingStore elasticsearchEmbeddingStore(ElasticsearchEmbeddingStoreProperties properties,
                                                                   RestClient elasticsearchRestClient) {
        String indexName = Optional.ofNullable(properties.getIndexName()).orElse(DEFAULT_INDEX_NAME);

        log.debug("create ElasticsearchEmbeddingStore on index [{}].", indexName);
        return ElasticsearchEmbeddingStore.builder()
                .restClient(elasticsearchRestClient)
                .indexName(indexName)
                .build();
    }
}
