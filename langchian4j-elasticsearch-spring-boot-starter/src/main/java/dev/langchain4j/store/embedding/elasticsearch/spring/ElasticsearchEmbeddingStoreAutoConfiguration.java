package dev.langchain4j.store.embedding.elasticsearch.spring;

import dev.langchain4j.store.embedding.elasticsearch.ElasticsearchEmbeddingStore;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import static dev.langchain4j.store.embedding.elasticsearch.spring.ElasticsearchProperties.PREFIX;

@AutoConfiguration
@EnableConfigurationProperties(ElasticsearchProperties.class)
public class ElasticsearchEmbeddingStoreAutoConfiguration {

    @Bean
    @ConditionalOnProperty(PREFIX + ".enabled")
    public ElasticsearchEmbeddingStore elasticsearchEmbeddingStore(ElasticsearchProperties properties) {
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
