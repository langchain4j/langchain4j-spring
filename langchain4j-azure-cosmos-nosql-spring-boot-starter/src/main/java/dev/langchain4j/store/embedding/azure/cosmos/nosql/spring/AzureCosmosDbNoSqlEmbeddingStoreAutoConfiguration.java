package dev.langchain4j.store.embedding.azure.cosmos.nosql.spring;

import com.azure.cosmos.models.CosmosVectorDataType;
import com.azure.cosmos.models.CosmosVectorDistanceFunction;
import com.azure.cosmos.models.CosmosVectorEmbedding;
import com.azure.cosmos.models.CosmosVectorEmbeddingPolicy;
import com.azure.cosmos.models.CosmosVectorIndexSpec;
import com.azure.cosmos.models.CosmosVectorIndexType;
import com.azure.cosmos.models.IncludedPath;
import com.azure.cosmos.models.IndexingMode;
import com.azure.cosmos.models.IndexingPolicy;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.azure.cosmos.nosql.AzureCosmosDbNoSqlEmbeddingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.Collections;

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
            AzureCosmosDbNoSqlEmbeddingStoreProperties properties,
            ObjectProvider<EmbeddingModel> embeddingModelProvider) {

        // Determine dimensions from properties or embedding model
        Integer dimensions = properties.getDimensions();
        if (dimensions == null) {
            EmbeddingModel embeddingModel = embeddingModelProvider.getIfAvailable();
            if (embeddingModel != null) {
                dimensions = embeddingModel.dimension();
            }
        }

        if (dimensions == null) {
            throw new IllegalArgumentException(
                    "dimensions must be configured via 'langchain4j.azure.cosmos-nosql.dimensions' " +
                    "or an EmbeddingModel bean must be present in the application context");
        }

        log.debug("Creating AzureCosmosDbNoSqlEmbeddingStore with endpoint [{}], database [{}], container [{}], dimensions [{}].",
                properties.getEndpoint(), properties.getDatabaseName(), properties.getContainerName(), dimensions);

        String embeddingPath = properties.getEmbeddingPath();
        String distanceFunction = properties.getDistanceFunction();
        String dataType = properties.getDataType();
        String vectorIndexType = properties.getVectorIndexType();

        // Create vector embedding policy
        CosmosVectorEmbeddingPolicy vectorEmbeddingPolicy = createVectorEmbeddingPolicy(
                embeddingPath, dimensions, distanceFunction, dataType);

        // Create indexing policy
        IndexingPolicy indexingPolicy = createIndexingPolicy(embeddingPath, vectorIndexType);

        AzureCosmosDbNoSqlEmbeddingStore.Builder builder = AzureCosmosDbNoSqlEmbeddingStore.builder()
                .endpoint(properties.getEndpoint())
                .apiKey(properties.getKey())
                .databaseName(properties.getDatabaseName())
                .containerName(properties.getContainerName())
                .cosmosVectorEmbeddingPolicy(vectorEmbeddingPolicy)
                .indexingPolicy(indexingPolicy);

        if (properties.getPartitionKeyPath() != null) {
            builder.partitionKeyPath(properties.getPartitionKeyPath());
        }

        if (properties.getVectorStoreThroughput() != null) {
            builder.vectorStoreThroughput(properties.getVectorStoreThroughput());
        }

        return builder.build();
    }

    private CosmosVectorEmbeddingPolicy createVectorEmbeddingPolicy(
            String embeddingPath,
            Integer dimensions,
            String distanceFunction,
            String dataType) {
        CosmosVectorEmbeddingPolicy vectorEmbeddingPolicy = new CosmosVectorEmbeddingPolicy();
        CosmosVectorEmbedding embedding = new CosmosVectorEmbedding();
        embedding.setPath(embeddingPath);
        embedding.setDataType(CosmosVectorDataType.valueOf(dataType));
        embedding.setEmbeddingDimensions(dimensions);
        embedding.setDistanceFunction(CosmosVectorDistanceFunction.valueOf(distanceFunction));
        vectorEmbeddingPolicy.setCosmosVectorEmbeddings(Collections.singletonList(embedding));
        return vectorEmbeddingPolicy;
    }

    private IndexingPolicy createIndexingPolicy(String embeddingPath, String vectorIndexType) {
        IndexingPolicy indexingPolicy = new IndexingPolicy();
        indexingPolicy.setIndexingMode(IndexingMode.CONSISTENT);
        IncludedPath includedPath = new IncludedPath("/*");
        indexingPolicy.setIncludedPaths(Collections.singletonList(includedPath));

        CosmosVectorIndexSpec vectorIndexSpec = new CosmosVectorIndexSpec();
        vectorIndexSpec.setPath(embeddingPath);
        vectorIndexSpec.setType(CosmosVectorIndexType.valueOf(vectorIndexType).toString());
        indexingPolicy.setVectorIndexes(Collections.singletonList(vectorIndexSpec));

        return indexingPolicy;
    }
}
