package dev.langchain4j.store.embedding.azure.cosmos.mongo.vcore.spring;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.azure.cosmos.mongo.vcore.AzureCosmosDbMongoVCoreEmbeddingStore;
import dev.langchain4j.store.embedding.spring.EmbeddingStoreAutoConfigurationIT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import static dev.langchain4j.internal.Utils.randomUUID;

@EnabledIfEnvironmentVariable(named = "AZURE_COSMOS_MONGO_VCORE_CONNECTION_STRING", matches = ".+")
class AzureCosmosDbMongoVCoreEmbeddingStoreAutoConfigurationIT extends EmbeddingStoreAutoConfigurationIT {

    private static final String CONNECTION_STRING = System.getenv("AZURE_COSMOS_MONGO_VCORE_CONNECTION_STRING");

    String collectionName;

    @BeforeEach
    void setCollectionName() {
        collectionName = "test_" + randomUUID().replace("-", "_");
    }

    @Override
    protected Class<?> autoConfigurationClass() {
        return AzureCosmosDbMongoVCoreEmbeddingStoreAutoConfiguration.class;
    }

    @Override
    protected Class<? extends EmbeddingStore<TextSegment>> embeddingStoreClass() {
        return AzureCosmosDbMongoVCoreEmbeddingStore.class;
    }

    @Override
    protected String[] properties() {
        return new String[]{
                "langchain4j.azure.cosmos-mongo-vcore.connection-string=" + CONNECTION_STRING,
                "langchain4j.azure.cosmos-mongo-vcore.database-name=testdb",
                "langchain4j.azure.cosmos-mongo-vcore.collection-name=" + collectionName,
                "langchain4j.azure.cosmos-mongo-vcore.index-name=testindex",
                "langchain4j.azure.cosmos-mongo-vcore.create-index=true"
        };
    }

    @Override
    protected String dimensionPropertyKey() {
        return "langchain4j.azure.cosmos-mongo-vcore.dimensions";
    }
}
