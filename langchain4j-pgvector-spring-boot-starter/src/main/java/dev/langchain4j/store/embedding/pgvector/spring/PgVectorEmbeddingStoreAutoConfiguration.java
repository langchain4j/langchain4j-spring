package dev.langchain4j.store.embedding.pgvector.spring;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.pgvector.DefaultMetadataStorageConfig;
import dev.langchain4j.store.embedding.pgvector.MetadataStorageConfig;
import dev.langchain4j.store.embedding.pgvector.MetadataStorageMode;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.lang.Nullable;

import java.util.Optional;

import static dev.langchain4j.internal.Utils.getOrDefault;
import static dev.langchain4j.store.embedding.pgvector.spring.PgVectorEmbeddingStoreProperties.*;

@AutoConfiguration
@EnableConfigurationProperties(PgVectorEmbeddingStoreProperties.class)
@ConditionalOnProperty(prefix = PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
public class PgVectorEmbeddingStoreAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public PgVectorEmbeddingStore pgVectorEmbeddingStore(PgVectorEmbeddingStoreProperties properties,
                                                         @Nullable EmbeddingModel embeddingModel) {
        String host = Optional.ofNullable(properties.getHost()).orElse(DEFAULT_HOST);
        int port = Optional.ofNullable(properties.getPort()).orElse(DEFAULT_PORT);
        String database = Optional.ofNullable(properties.getDatabase()).orElse(DEFAULT_DATABASE);
        String table = Optional.ofNullable(properties.getDatabase()).orElse(DEFAULT_TABLE);
        Integer dimension = Optional.ofNullable(properties.getDimension()).orElseGet(() -> embeddingModel == null ? null : embeddingModel.dimension());

        // get user and password from env variable
        String user = Optional.ofNullable(properties.getUser()).orElse(System.getenv("PGVECTOR_USER"));
        String password = Optional.ofNullable(properties.getPassword()).orElse(System.getenv("PGVECTOR_PASSWORD"));

        // get metadata storage config from spring properties
        MetadataStorageConfigProperties metadataStorageConfigProperties = Optional.ofNullable(properties.getMetadataStorageConfig()).orElse(MetadataStorageConfigProperties.defaultConfig());
        MetadataStorageMode storageMode = Optional.ofNullable(metadataStorageConfigProperties.getStorageMode()).orElse(MetadataStorageMode.COMBINED_JSON);
        MetadataStorageConfig metadataStorageConfig = DefaultMetadataStorageConfig.builder()
                .storageMode(storageMode)
                .columnDefinitions(metadataStorageConfigProperties.getColumnDefinitions()).build();

        return PgVectorEmbeddingStore.builder()
                .host(host)
                .port(port)
                .database(database)
                .user(user)
                .password(password)
                .table(table)
                .dimension(dimension)
                .useIndex(getOrDefault(properties.getUseIndex(), false))
                .indexListSize(properties.getIndexListSize())
                .createTable(getOrDefault(properties.getCreateTable(), true))
                .dropTableFirst(getOrDefault(properties.getDropTableFirst(), false))
                .metadataStorageConfig(metadataStorageConfig)
                .build();
    }
}
