package dev.langchain4j.store.embedding.pgvector.spring;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.lang.Nullable;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Optional;

import static dev.langchain4j.internal.ValidationUtils.*;
import static dev.langchain4j.store.embedding.pgvector.spring.PgVectorEmbeddingStoreProperties.*;
import static org.springframework.util.StringUtils.startsWithIgnoreCase;

@AutoConfiguration
@EnableConfigurationProperties({PgVectorEmbeddingStoreProperties.class, PgVectorDataSourceProperties.class})
@ConditionalOnProperty(prefix = PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
public class PgVectorEmbeddingStoreAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(PgVectorEmbeddingStoreAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(DataSource.class)
    @ConditionalOnProperty(prefix = PgVectorDataSourceProperties.PREFIX, name = "enabled", havingValue = "false")
    public PgVectorEmbeddingStore pgVectorEmbeddingStoreWithExistingDataSource(DataSource dataSource, PgVectorEmbeddingStoreProperties properties,
                                                         @Nullable EmbeddingModel embeddingModel) {
        // Check if the context's data source is a Postgres datasource
        ensureTrue(isPostgresqlDataSource(dataSource), "The DataSource in Spring Context is not a Postgres datasource, you need to manually specify the Postgres datasource configuration via 'langchain4j.pgvector.datasource'.");

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

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = PgVectorDataSourceProperties.PREFIX, name = "enabled", havingValue = "true")
    public PgVectorEmbeddingStore pgVectorEmbeddingStoreWithCustomDataSource(PgVectorEmbeddingStoreProperties properties, PgVectorDataSourceProperties dataSourceProperties,
                                                         @Nullable EmbeddingModel embeddingModel) {
        Integer dimension = Optional.ofNullable(properties.getDimension()).orElseGet(() -> embeddingModel == null ? null : embeddingModel.dimension());

        return PgVectorEmbeddingStore.builder()
                .host(dataSourceProperties.getHost())
                .port(dataSourceProperties.getPort())
                .user(dataSourceProperties.getUser())
                .password(dataSourceProperties.getPassword())
                .database(dataSourceProperties.getDatabase())
                .table(properties.getTable())
                .createTable(properties.getCreateTable())
                .dimension(dimension)
                .useIndex(properties.getUseIndex())
                .indexListSize(properties.getIndexListSize())
                .build();
    }

    /**
     * Check if the datasource is <code>postgresql</code>`.
     * @param dataSource instance of {@link DataSource}.
     * @return true means it is a postgresql data source, otherwise it is not.
     */
    private boolean isPostgresqlDataSource(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            return startsWithIgnoreCase(metaData.getURL(), "jdbc:postgresql");
        } catch (SQLException e) {
            log.warn("Exception checking datasource driver type during PgVector auto-configuration .");
            return false;
        }
    }
}
