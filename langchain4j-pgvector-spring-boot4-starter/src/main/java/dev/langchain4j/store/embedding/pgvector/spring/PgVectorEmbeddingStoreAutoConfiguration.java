package dev.langchain4j.store.embedding.pgvector.spring;

import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import static dev.langchain4j.store.embedding.pgvector.spring.PgVectorEmbeddingStoreProperties.PREFIX;

/**
 * Auto-configuration for {@link PgVectorEmbeddingStore} in Spring Boot 4 applications.
 */
@AutoConfiguration
@ConditionalOnClass(PgVectorEmbeddingStore.class)
@EnableConfigurationProperties(PgVectorEmbeddingStoreProperties.class)
@ConditionalOnProperty(prefix = PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
public class PgVectorEmbeddingStoreAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(PgVectorEmbeddingStoreAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    public PgVectorEmbeddingStore pgVectorEmbeddingStore(PgVectorEmbeddingStoreProperties properties) {
        log.debug("Creating PgVectorEmbeddingStore with properties: host={}, port={}, database={}, tableName={}",
                properties.getHost(), properties.getPort(), properties.getDatabase(), properties.getTableName());

        PgVectorEmbeddingStore.PgVectorEmbeddingStoreBuilder builder = PgVectorEmbeddingStore.builder()
                .host(properties.getHost())
                .port(properties.getPort())
                .user(properties.getUser())
                .password(properties.getPassword())
                .database(properties.getDatabase())
                .table(properties.getTableName())
                .dimension(properties.getDimension())
                .useIndex(properties.getUseIndex())
                .indexListSize(properties.getIndexListSize())
                .createTable(properties.getCreateTable())
                .dropTableFirst(properties.getDropTableFirst())
                .skipCreateVectorExtension(properties.getSkipCreateVectorExtension());

        return builder.build();
    }
}
