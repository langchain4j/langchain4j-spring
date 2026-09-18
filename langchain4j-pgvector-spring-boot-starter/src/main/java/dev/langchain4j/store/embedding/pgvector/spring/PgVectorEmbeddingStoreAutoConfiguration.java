package dev.langchain4j.store.embedding.pgvector.spring;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.lang.Nullable;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Optional;

import static dev.langchain4j.store.embedding.pgvector.spring.PgVectorEmbeddingStoreProperties.PREFIX;

@AutoConfiguration
@EnableConfigurationProperties(PgVectorEmbeddingStoreProperties.class)
@ConditionalOnProperty(prefix = PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
public class PgVectorEmbeddingStoreAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(PgVectorEmbeddingStoreAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    public PgVectorEmbeddingStore pgVectorEmbeddingStore(PgVectorEmbeddingStoreProperties properties,
                                                         ObjectProvider<DataSource> dataSources,
                                                         ApplicationContext applicationContext,
                                                         @Nullable EmbeddingModel embeddingModel) {
        Integer dimension = Optional.ofNullable(properties.getDimension())
                .orElseGet(() -> embeddingModel == null ? null : embeddingModel.dimension());

        // If explicit host is specified, use the host/port/user/password builder
        if (properties.getHost() != null) {
            return PgVectorEmbeddingStore.builder()
                    .host(properties.getHost())
                    .port(properties.getPort())
                    .user(properties.getUser())
                    .password(properties.getPassword())
                    .database(properties.getDatabase())
                    .table(properties.getTable())
                    .dimension(dimension)
                    .useIndex(properties.getUseIndex())
                    .indexListSize(properties.getIndexListSize())
                    .createTable(properties.getCreateTable())
                    .dropTableFirst(properties.getDropTableFirst())
                    .skipCreateVectorExtension(properties.getSkipCreateVectorExtension())
                    .searchMode(properties.getSearchMode())
                    .textSearchConfig(properties.getTextSearchConfig())
                    .rrfK(properties.getRrfK())
                    .build();
        }

        // Otherwise, use an existing DataSource from the Spring context
        DataSource dataSource = resolveDataSource(properties, dataSources, applicationContext);

        // Wrap for @Transactional support (Issue #2614)
        DataSource transactionalDataSource = new TransactionAwareDataSourceProxy(dataSource);

        return PgVectorEmbeddingStore.datasourceBuilder()
                .datasource(transactionalDataSource)
                .table(properties.getTable())
                .dimension(dimension)
                .useIndex(properties.getUseIndex())
                .indexListSize(properties.getIndexListSize())
                .createTable(properties.getCreateTable())
                .dropTableFirst(properties.getDropTableFirst())
                .skipCreateVectorExtension(properties.getSkipCreateVectorExtension())
                .searchMode(properties.getSearchMode())
                .textSearchConfig(properties.getTextSearchConfig())
                .rrfK(properties.getRrfK())
                .build();
    }

    private DataSource resolveDataSource(PgVectorEmbeddingStoreProperties properties,
                                         ObjectProvider<DataSource> dataSources,
                                         ApplicationContext applicationContext) {
        String beanName = properties.getDataSourceBeanName();

        // If a specific DataSource bean name is configured, use that
        if (beanName != null && !beanName.isEmpty()) {
            DataSource dataSource = applicationContext.getBean(beanName, DataSource.class);
            log.info("Using configured DataSource bean: {}", beanName);
            return dataSource;
        }

        // Otherwise, find the first PostgreSQL DataSource
        DataSource dataSource = dataSources.stream()
                .filter(this::isPostgresqlDataSource)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No suitable PostgreSQL DataSource found in the application context. "
                                + "Please configure a PostgreSQL DataSource or specify explicit connection properties "
                                + "via 'langchain4j.pgvector.host', 'langchain4j.pgvector.port', etc."));
        log.info("Using auto-detected PostgreSQL DataSource: {}", dataSource.getClass().getSimpleName());
        return dataSource;
    }

    private boolean isPostgresqlDataSource(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String url = metaData.getURL();
            return url != null && url.toLowerCase().startsWith("jdbc:postgresql");
        } catch (SQLException e) {
            log.warn("Could not determine DataSource type during PgVector auto-configuration", e);
            return false;
        }
    }
}
