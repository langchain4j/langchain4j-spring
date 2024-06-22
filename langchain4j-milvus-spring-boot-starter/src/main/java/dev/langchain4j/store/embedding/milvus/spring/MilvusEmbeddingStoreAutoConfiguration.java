package dev.langchain4j.store.embedding.milvus.spring;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore;
import io.milvus.common.clientenum.ConsistencyLevelEnum;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.Optional;

import static dev.langchain4j.store.embedding.milvus.spring.MilvusEmbeddingStoreProperties.*;

@AutoConfiguration
@EnableConfigurationProperties(MilvusEmbeddingStoreProperties.class)
@ConditionalOnProperty(prefix = PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
public class MilvusEmbeddingStoreAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(EmbeddingModel.class)
    public MilvusEmbeddingStore milvusEmbeddingStore(EmbeddingModel embeddingModel,
                                                     MilvusEmbeddingStoreProperties properties) {
        String host = Optional.ofNullable(properties.getHost()).orElse(DEFAULT_HOST);
        int port = Optional.ofNullable(properties.getPort()).orElse(DEFAULT_PORT);
        String collectionName = Optional.ofNullable(properties.getCollectionName()).orElse(DEFAULT_COLLECTION_NAME);
        ConsistencyLevelEnum consistencyLevel = Optional.ofNullable(properties.getConsistencyLevel()).orElse(DEFAULT_CONSISTENCY_LEVEL);
        int dimension = Optional.ofNullable(properties.getDimension()).orElse(embeddingModel.dimension());

        // get username and password from env variable
        String username = Optional.ofNullable(properties.getUsername()).orElse(System.getenv("MILVUS_USERNAME"));
        String password = Optional.ofNullable(properties.getPassword()).orElse(System.getenv("MILVUS_PASSWORD"));

        return MilvusEmbeddingStore.builder()
                .host(host)
                .port(port)
                .collectionName(collectionName)
                .dimension(dimension)
                .indexType(properties.getIndexType())
                .metricType(properties.getMetricType())
                .uri(properties.getUri())
                .token(properties.getToken())
                .username(username)
                .password(password)
                .consistencyLevel(consistencyLevel)
                .retrieveEmbeddingsOnSearch(properties.getRetrieveEmbeddingsOnSearch())
                .autoFlushOnInsert(properties.getAutoFlushOnInsert())
                .databaseName(properties.getDatabaseName())
                .build();
    }
}
