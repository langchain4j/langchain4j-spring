package dev.langchain4j.store.embedding.azure.documentdb.spring;

import com.mongodb.client.MongoClient;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.azure.documentdb.AzureDocumentDbEmbeddingStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.NoneNestedConditions;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;

import static dev.langchain4j.store.embedding.azure.documentdb.spring.AzureDocumentDbEmbeddingStoreProperties.PREFIX;

/**
 * Auto-configuration for {@link AzureDocumentDbEmbeddingStore}.
 * <p>
 * When {@code langchain4j.azure.documentdb.connection-string} is set, the store creates and owns its own client.
 * Otherwise, a user-provided {@link MongoClient} bean is used.
 * This configuration runs before Spring Boot's MongoDB auto-configuration so that
 * its default client does not override the DocumentDB connection properties.
 * </p>
 *
 * @see AzureDocumentDbEmbeddingStoreProperties
 */
@AutoConfiguration(beforeName = {
        "org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration",
        "org.springframework.boot.mongodb.autoconfigure.MongoAutoConfiguration"
})
@ConditionalOnClass({AzureDocumentDbEmbeddingStore.class, MongoClient.class})
@EnableConfigurationProperties(AzureDocumentDbEmbeddingStoreProperties.class)
@ConditionalOnProperty(prefix = PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
public class AzureDocumentDbEmbeddingStoreAutoConfiguration {

    @Bean(destroyMethod = "close")
    @Conditional(ConnectionStringConfiguredOrNoMongoClient.class)
    @ConditionalOnMissingBean
    public AzureDocumentDbEmbeddingStore azureDocumentDbEmbeddingStore(
            AzureDocumentDbEmbeddingStoreProperties properties,
            ObjectProvider<EmbeddingModel> embeddingModelProvider) {
        return createEmbeddingStore(properties, null, embeddingModelProvider);
    }

    @Bean(destroyMethod = "close")
    @ConditionalOnBean(MongoClient.class)
    @Conditional(ConnectionStringNotConfigured.class)
    @ConditionalOnMissingBean
    public AzureDocumentDbEmbeddingStore azureDocumentDbEmbeddingStoreWithMongoClient(
            AzureDocumentDbEmbeddingStoreProperties properties,
            MongoClient mongoClient,
            ObjectProvider<EmbeddingModel> embeddingModelProvider) {
        return createEmbeddingStore(properties, mongoClient, embeddingModelProvider);
    }

    private AzureDocumentDbEmbeddingStore createEmbeddingStore(
            AzureDocumentDbEmbeddingStoreProperties properties,
            MongoClient mongoClient,
            ObjectProvider<EmbeddingModel> embeddingModelProvider) {
        AzureDocumentDbEmbeddingStore.Builder builder = AzureDocumentDbEmbeddingStore.builder()
                .mongoClient(mongoClient)
                .connectionString(properties.getConnectionString())
                .databaseName(properties.getDatabaseName())
                .collectionName(properties.getCollectionName())
                .indexName(properties.getIndexName())
                .applicationName(properties.getApplicationName())
                .createIndex(properties.getCreateIndex())
                .kind(properties.getKind())
                .numLists(properties.getNumLists())
                .m(properties.getM())
                .efConstruction(properties.getEfConstruction())
                .efSearch(properties.getEfSearch());

        Integer dimensions = properties.getDimensions();
        if (Boolean.TRUE.equals(properties.getCreateIndex()) && dimensions == null) {
            EmbeddingModel embeddingModel = embeddingModelProvider.getIfAvailable();
            if (embeddingModel != null) {
                dimensions = embeddingModel.dimension();
            }
            if (dimensions == null) {
                throw new IllegalArgumentException(
                        "dimensions must be configured via 'langchain4j.azure.documentdb.dimensions' "
                                + "or an EmbeddingModel bean must be present when create-index is true");
            }
        }

        return builder.dimensions(dimensions).build();
    }

    static class ConnectionStringConfiguredOrNoMongoClient extends AnyNestedCondition {

        ConnectionStringConfiguredOrNoMongoClient() {
            super(ConfigurationPhase.REGISTER_BEAN);
        }

        @ConditionalOnProperty(prefix = PREFIX, name = "connection-string")
        static class ConnectionStringConfigured {
        }

        @ConditionalOnMissingBean(MongoClient.class)
        static class NoMongoClient {
        }
    }

    static class ConnectionStringNotConfigured extends NoneNestedConditions {

        ConnectionStringNotConfigured() {
            super(ConfigurationPhase.REGISTER_BEAN);
        }

        @ConditionalOnProperty(prefix = PREFIX, name = "connection-string")
        static class ConnectionStringConfigured {
        }
    }
}
