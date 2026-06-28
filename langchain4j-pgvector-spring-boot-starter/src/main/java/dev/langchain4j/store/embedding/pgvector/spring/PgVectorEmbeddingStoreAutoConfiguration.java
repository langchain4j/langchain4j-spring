package dev.langchain4j.store.embedding.pgvector.spring;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.lang.Nullable;

import java.util.Optional;

import static dev.langchain4j.store.embedding.pgvector.spring.PgVectorEmbeddingStoreProperties.*;

@AutoConfiguration
@EnableConfigurationProperties(PgVectorEmbeddingStoreProperties.class)
@ConditionalOnProperty(prefix = PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
public class PgVectorEmbeddingStoreAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public PgVectorEmbeddingStore pgvectorEmbeddingStore(PgVectorEmbeddingStoreProperties properties,
                                                         @Nullable EmbeddingModel embeddingModel) {
        String host = Optional.ofNullable(properties.getHost()).orElse(DEFAULT_HOST);
        int port = Optional.ofNullable(properties.getPort()).orElse(DEFAULT_PORT);
        String database = Optional.ofNullable(properties.getDatabase()).orElse(DEFAULT_DATABASE);
        String table = Optional.ofNullable(properties.getTable()).orElse(DEFAULT_TABLE);
        Integer dimension = properties.getDimension();
        if (dimension == null && embeddingModel != null) {
            dimension = embeddingModel.dimension();
        }

        // get password from env variable if not set
        String password = Optional.ofNullable(properties.getPassword()).orElse(System.getenv("PGVECTOR_PASSWORD"));

        PgVectorEmbeddingStore.PgVectorEmbeddingStoreBuilder builder = PgVectorEmbeddingStore.builder()
                .host(host)
                .port(port)
                .database(database)
                .table(table)
                .dimension(dimension)
                .useIndex(properties.getUseIndex())
                .indexListSize(properties.getIndexListSize())
                .createTable(properties.getCreateTable())
                .dropTableFirst(properties.getDropTableFirst())
                .skipCreateVectorExtension(properties.getSkipCreateVectorExtension())
                .searchMode(properties.getSearchMode())
                .textSearchConfig(properties.getTextSearchConfig())
                .rrfK(properties.getRrfK());

        String user = properties.getUser();
        if (user != null) {
            builder.user(user);
        }
        if (password != null) {
            builder.password(password);
        }

        return builder.build();
    }
}
