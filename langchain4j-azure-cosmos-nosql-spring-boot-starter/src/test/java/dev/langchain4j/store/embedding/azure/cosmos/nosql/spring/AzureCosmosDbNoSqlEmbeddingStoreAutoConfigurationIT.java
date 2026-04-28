package dev.langchain4j.store.embedding.azure.cosmos.nosql.spring;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.azure.cosmos.nosql.AzureCosmosDbNoSqlEmbeddingStore;
import dev.langchain4j.store.embedding.spring.EmbeddingStoreAutoConfigurationIT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static dev.langchain4j.internal.Utils.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

@EnabledIfEnvironmentVariable(named = "AZURE_COSMOS_NOSQL_ENDPOINT", matches = ".+")
class AzureCosmosDbNoSqlEmbeddingStoreAutoConfigurationIT extends EmbeddingStoreAutoConfigurationIT {

    // Support both naming conventions for environment variables
    private static final String ENDPOINT = getEnvWithFallback("AZURE_COSMOS_NOSQL_ENDPOINT", "AZURE_COSMOS_HOST");
    private static final String KEY = getEnvWithFallback("AZURE_COSMOS_NOSQL_KEY", "AZURE_COSMOS_MASTER_KEY");

    private static String getEnvWithFallback(String primary, String fallback) {
        String value = System.getenv(primary);
        return value != null ? value : System.getenv(fallback);
    }

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
        return "langchain4j.azure.cosmos-nosql.dimensions";
    }

    @Test
    void should_not_create_embedding_store_when_disabled() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(AzureCosmosDbNoSqlEmbeddingStoreAutoConfiguration.class))
                .withPropertyValues(
                        "langchain4j.azure.cosmos-nosql.enabled=false",
                        "langchain4j.azure.cosmos-nosql.endpoint=" + ENDPOINT,
                        "langchain4j.azure.cosmos-nosql.key=" + KEY,
                        "langchain4j.azure.cosmos-nosql.database-name=testdb",
                        "langchain4j.azure.cosmos-nosql.container-name=" + containerName,
                        "langchain4j.azure.cosmos-nosql.dimensions=384"
                )
                .run(context -> {
                    assertThat(context).doesNotHaveBean(AzureCosmosDbNoSqlEmbeddingStore.class);
                });
    }
}
