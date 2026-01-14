package dev.langchain4j.store.embedding.azure.cosmos.nosql.spring;

import dev.langchain4j.store.embedding.azure.cosmos.nosql.AzureCosmosDbNoSqlEmbeddingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import static dev.langchain4j.store.embedding.azure.cosmos.nosql.spring.AzureCosmosDbNoSqlEmbeddingStoreProperties.PREFIX;

/**
 * Auto-configuration for {@link AzureCosmosDbNoSqlEmbeddingStore}.
 * <p>
 * This auto-configuration provides a {@link AzureCosmosDbNoSqlEmbeddingStore} bean
 * when the required properties are configured under {@code langchain4j.azure.cosmos-nosql}.
 * </p>
 * <p>
 * The auto-configuration can be disabled by setting {@code langchain4j.azure.cosmos-nosql.enabled=false}.
 * </p>
 *
 * @see AzureCosmosDbNoSqlEmbeddingStoreProperties
 */
@AutoConfiguration
@EnableConfigurationProperties(AzureCosmosDbNoSqlEmbeddingStoreProperties.class)
@ConditionalOnProperty(prefix = PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
public class AzureCosmosDbNoSqlEmbeddingStoreAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(AzureCosmosDbNoSqlEmbeddingStoreAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    public AzureCosmosDbNoSqlEmbeddingStore azureCosmosDbNoSqlEmbeddingStore(
            AzureCosmosDbNoSqlEmbeddingStoreProperties properties) {

        log.debug("Creating AzureCosmosDbNoSqlEmbeddingStore with endpoint [{}], database [{}], container [{}].",
                properties.getEndpoint(), properties.getDatabaseName(), properties.getContainerName());

        AzureCosmosDbNoSqlEmbeddingStore.Builder builder = AzureCosmosDbNoSqlEmbeddingStore.builder()
                .endpoint(properties.getEndpoint())
                .apiKey(properties.getKey())
                .databaseName(properties.getDatabaseName())
                .containerName(properties.getContainerName());

        if (properties.getPartitionKeyPath() != null) {
            builder.partitionKeyPath(properties.getPartitionKeyPath());
        }

        if (properties.getVectorStoreThroughput() != null) {
            builder.vectorStoreThroughput(properties.getVectorStoreThroughput());
        }

        return builder.build();
    }
}
