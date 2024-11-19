package dev.langchain4j.store.embedding.pgvector.spring;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;

import javax.sql.DataSource;
import java.util.Optional;

import static dev.langchain4j.store.embedding.pgvector.spring.PgVectorEmbeddingStoreProperties.*;

@AutoConfiguration
@EnableConfigurationProperties(PgVectorEmbeddingStoreProperties.class)
@ConditionalOnProperty(prefix = PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnClass({PgVectorEmbeddingStore.class, DataSource.class})
public class PgVectorEmbeddingStoreAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(DataSource.class)
    public PgVectorEmbeddingStore pgVectorEmbeddingStore(DataSource dataSource, PgVectorEmbeddingStoreProperties properties,
                                                         @Nullable EmbeddingModel embeddingModel) {
        Integer dimension = Optional.ofNullable(properties.getDimension()).orElseGet(() -> embeddingModel == null ? null : embeddingModel.dimension());

        return PgVectorEmbeddingStore.datasourceBuilder()
                .datasource(dataSource)
                .table(properties.getTable())
                .createTable(properties.getCreateTable())
                .dimension(dimension)
                .useIndex(properties.getUseIndex())
                .indexListSize(properties.getIndexListSize())
                .build();
    }

    @Configuration
    @ConditionalOnMissingBean(DataSource.class)
    protected static class DataSourceNotFoundConfiguration {
        @PostConstruct
        public void logWarning() {
            throw new IllegalStateException(
                    "No DataSource found. Please configure a DataSource (e.g., by including spring-boot-starter-jdbc) to use PgVector."
            );
        }
    }
}
