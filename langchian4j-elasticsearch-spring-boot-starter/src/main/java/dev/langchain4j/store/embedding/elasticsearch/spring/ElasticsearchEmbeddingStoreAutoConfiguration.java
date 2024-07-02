package dev.langchain4j.store.embedding.elasticsearch.spring;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.elasticsearch.ElasticsearchEmbeddingStore;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.lang.Nullable;

import java.util.Optional;

import static dev.langchain4j.store.embedding.elasticsearch.spring.ElasticsearchEmbeddingStoreProperties.*;

@AutoConfiguration
@EnableConfigurationProperties(ElasticsearchEmbeddingStoreProperties.class)
@ConditionalOnProperty(prefix = PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
public class ElasticsearchEmbeddingStoreAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ElasticsearchEmbeddingStore elasticsearchEmbeddingStore(ElasticsearchEmbeddingStoreProperties properties,
                                                                   @Nullable EmbeddingModel embeddingModel) {
        String serverUrl = Optional.ofNullable(properties.getServerUrl()).orElse(DEFAULT_SERVER_URL);
        String indexName = Optional.ofNullable(properties.getIndexName()).orElse(DEFAULT_INDEX_NAME);
        Integer dimension = Optional.ofNullable(properties.getDimension()).orElseGet(() -> embeddingModel == null ? null : embeddingModel.dimension());

        return ElasticsearchEmbeddingStore.builder()
                .serverUrl(serverUrl)
                .apiKey(properties.getApiKey())
                .userName(properties.getUserName())
                .password(properties.getPassword())
                .indexName(indexName)
                .dimension(dimension)
                .build();
    }
}
