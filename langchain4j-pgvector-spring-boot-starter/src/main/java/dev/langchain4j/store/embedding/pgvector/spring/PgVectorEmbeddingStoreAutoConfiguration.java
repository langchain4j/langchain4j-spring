package dev.langchain4j.store.embedding.pgvector.spring;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.lang.Nullable;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

import static dev.langchain4j.internal.ValidationUtils.*;
import static dev.langchain4j.store.embedding.pgvector.spring.PgVectorEmbeddingStoreProperties.*;
import static org.springframework.util.StringUtils.startsWithIgnoreCase;

@AutoConfiguration
@EnableConfigurationProperties({PgVectorEmbeddingStoreProperties.class, PgVectorDataSourceProperties.class})
@ConditionalOnProperty(prefix = PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
public class PgVectorEmbeddingStoreAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(PgVectorEmbeddingStoreAutoConfiguration.class);

    private final ApplicationContext applicationContext;

    public PgVectorEmbeddingStoreAutoConfiguration(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(DataSource.class)
    @ConditionalOnProperty(prefix = PgVectorDataSourceProperties.PREFIX, name = "enabled", havingValue = "false")
    public PgVectorEmbeddingStore pgVectorEmbeddingStoreWithExistingDataSource(ObjectProvider<DataSource> dataSources, PgVectorEmbeddingStoreProperties properties,
                                                                               @Nullable EmbeddingModel embeddingModel) {

        // The PostgreSQL data source is selected based on the configured dataSourceBeanName or automatically.
        DataSource dataSource = dataSources.stream()
                .filter(ds -> {
                    // Preferentially matches the configured dataSourceBeanName.
                    String beanName = properties.getDataSourceBeanName();
                    if (beanName != null && !beanName.isEmpty()) {
                        String actualBeanName = getBeanNameForDataSource(ds);
                        return beanName.equals(actualBeanName);
                    }
                    return false;
                })
                .findFirst()
                // If no dataSourceBeanName is specified, the first PostgreSQL data source is selected.
                .orElseGet(() -> dataSources.stream()
                        .filter(this::isPostgresqlDataSource)
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("No suitable PostgreSQL DataSource found in the application context. "
                                + "Please configure a valid PostgreSQL DataSource.")));

        log.info("Using DataSource bean: {}", dataSource.getClass().getSimpleName());

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
                .host(dataSourceProperties.host())
                .port(dataSourceProperties.port())
                .user(dataSourceProperties.user())
                .password(dataSourceProperties.password())
                .database(dataSourceProperties.database())
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

    /**
     * Get the BeanName of the DataSource instance from the ApplicationContext.
     * @param dataSource Target DataSource instance.
     * @return bean name of target DataSource .
     */
    private String getBeanNameForDataSource(DataSource dataSource) {
        // Iterate through all DataSource beans to find the bean name that matches the current instance
        return applicationContext.getBeansOfType(DataSource.class).entrySet().stream()
                .filter(entry -> entry.getValue().equals(dataSource))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }
}
