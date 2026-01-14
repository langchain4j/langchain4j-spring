package dev.langchain4j.store.embedding.azure.cosmos.mongo.vcore.spring;

import dev.langchain4j.store.embedding.azure.cosmos.mongo.vcore.AzureCosmosDbMongoVCoreEmbeddingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import static dev.langchain4j.store.embedding.azure.cosmos.mongo.vcore.spring.AzureCosmosDbMongoVCoreEmbeddingStoreProperties.PREFIX;

/**
 * Auto-configuration for {@link AzureCosmosDbMongoVCoreEmbeddingStore}.
 * <p>
 * This auto-configuration provides a {@link AzureCosmosDbMongoVCoreEmbeddingStore} bean
 * when the required properties are configured under {@code langchain4j.azure.cosmos-mongo-vcore}.
 * </p>
 * <p>
 * The auto-configuration can be disabled by setting {@code langchain4j.azure.cosmos-mongo-vcore.enabled=false}.
 * </p>
 *
 * @see AzureCosmosDbMongoVCoreEmbeddingStoreProperties
 */
@AutoConfiguration
@EnableConfigurationProperties(AzureCosmosDbMongoVCoreEmbeddingStoreProperties.class)
@ConditionalOnProperty(prefix = PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
public class AzureCosmosDbMongoVCoreEmbeddingStoreAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(AzureCosmosDbMongoVCoreEmbeddingStoreAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    public AzureCosmosDbMongoVCoreEmbeddingStore azureCosmosDbMongoVCoreEmbeddingStore(
            AzureCosmosDbMongoVCoreEmbeddingStoreProperties properties) {

        log.debug("Creating AzureCosmosDbMongoVCoreEmbeddingStore with database [{}], collection [{}].",
                properties.getDatabaseName(), properties.getCollectionName());

        return AzureCosmosDbMongoVCoreEmbeddingStore.builder()
                .connectionString(properties.getConnectionString())
                .databaseName(properties.getDatabaseName())
                .collectionName(properties.getCollectionName())
                .indexName(properties.getIndexName())
                .applicationName(properties.getApplicationName())
                .createIndex(properties.getCreateIndex())
                .kind(properties.getKind())
                .numLists(properties.getNumLists())
                .dimensions(properties.getDimensions())
                .m(properties.getM())
                .efConstruction(properties.getEfConstruction())
                .efSearch(properties.getEfSearch())
                .build();
    }
}
