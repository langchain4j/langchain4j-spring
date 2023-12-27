package dev.langchain4j.store.embedding.elasticsearch.spring;

import dev.langchain4j.store.embedding.elasticsearch.ElasticsearchEmbeddingStore;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import static dev.langchain4j.store.embedding.elasticsearch.spring.ElasticsearchEmbeddingStoreProperties.PREFIX;

@AutoConfiguration
@EnableConfigurationProperties(ElasticsearchEmbeddingStoreProperties.class)
@ConditionalOnProperty(prefix = PREFIX, name = "enabled", havingValue = "true")
public class ElasticsearchEmbeddingStoreAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ElasticsearchEmbeddingStore elasticsearchEmbeddingStore(ElasticsearchEmbeddingStoreProperties properties) {
        return ElasticsearchEmbeddingStore.builder()
                .serverUrl(properties.getServerUrl())
                .apiKey(properties.getApiKey())
                .userName(properties.getUserName())
                .password(properties.getPassword())
                .indexName(properties.getIndexName())
                .dimension(properties.getDimension())
                .build();
    }
}
