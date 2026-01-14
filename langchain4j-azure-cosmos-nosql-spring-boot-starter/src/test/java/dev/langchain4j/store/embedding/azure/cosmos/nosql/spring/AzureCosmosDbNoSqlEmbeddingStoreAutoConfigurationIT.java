package dev.langchain4j.store.embedding.azure.cosmos.nosql.spring;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.azure.cosmos.nosql.AzureCosmosDbNoSqlEmbeddingStore;
import dev.langchain4j.store.embedding.spring.EmbeddingStoreAutoConfigurationIT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import static dev.langchain4j.internal.Utils.randomUUID;

@EnabledIfEnvironmentVariable(named = "AZURE_COSMOS_NOSQL_ENDPOINT", matches = ".+")
class AzureCosmosDbNoSqlEmbeddingStoreAutoConfigurationIT extends EmbeddingStoreAutoConfigurationIT {

    private static final String ENDPOINT = System.getenv("AZURE_COSMOS_NOSQL_ENDPOINT");
    private static final String KEY = System.getenv("AZURE_COSMOS_NOSQL_KEY");

    String containerName;

    @BeforeEach
    void setContainerName() {
        containerName = "test_" + randomUUID().replace("-", "_");
    }

    @Override
    protected Class<?> autoConfigurationClass() {
        return AzureCosmosDbNoSqlEmbeddingStoreAutoConfiguration.class;
    }

    @Override
    protected Class<? extends EmbeddingStore<TextSegment>> embeddingStoreClass() {
        return AzureCosmosDbNoSqlEmbeddingStore.class;
    }

    @Override
    protected String[] properties() {
        return new String[]{
                "langchain4j.azure.cosmos-nosql.endpoint=" + ENDPOINT,
                "langchain4j.azure.cosmos-nosql.key=" + KEY,
                "langchain4j.azure.cosmos-nosql.database-name=testdb",
                "langchain4j.azure.cosmos-nosql.container-name=" + containerName
        };
    }

    @Override
    protected String dimensionPropertyKey() {
        return "langchain4j.azure.cosmos-nosql.dimension";
    }
}
