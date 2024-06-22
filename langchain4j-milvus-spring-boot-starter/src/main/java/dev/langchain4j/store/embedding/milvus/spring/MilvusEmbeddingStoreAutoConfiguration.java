package dev.langchain4j.store.embedding.milvus.spring;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.Optional;

import static dev.langchain4j.store.embedding.milvus.spring.MilvusEmbeddingStoreProperties.PREFIX;

@AutoConfiguration
@EnableConfigurationProperties(MilvusEmbeddingStoreProperties.class)
@ConditionalOnProperty(prefix = PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
public class MilvusEmbeddingStoreAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(EmbeddingModel.class)
    public MilvusEmbeddingStore milvusEmbeddingStore(EmbeddingModel embeddingModel,
                                                     MilvusEmbeddingStoreProperties properties) {
        int dimension = Optional.ofNullable(properties.getDimension()).orElse(embeddingModel.dimension());

        return MilvusEmbeddingStore.builder()
                .host(properties.getHost())
                .port(properties.getPort())
                .collectionName(properties.getCollectionName())
                .dimension(dimension)
                .indexType(properties.getIndexType())
                .metricType(properties.getMetricType())
                .uri(properties.getUri())
                .token(properties.getToken())
                .username(properties.getUsername())
                .password(properties.getPassword())
                .consistencyLevel(properties.getConsistencyLevel())
                .retrieveEmbeddingsOnSearch(properties.getRetrieveEmbeddingsOnSearch())
                .autoFlushOnInsert(properties.getAutoFlushOnInsert())
                .databaseName(properties.getDatabaseName())
                .build();
    }
}
