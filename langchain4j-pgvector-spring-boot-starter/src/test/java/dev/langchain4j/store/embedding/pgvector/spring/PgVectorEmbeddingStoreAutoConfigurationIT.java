package dev.langchain4j.store.embedding.pgvector.spring;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import dev.langchain4j.store.embedding.spring.EmbeddingStoreAutoConfigurationIT;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static dev.langchain4j.internal.Utils.randomUUID;

@Testcontainers
class PgVectorEmbeddingStoreAutoConfigurationIT extends EmbeddingStoreAutoConfigurationIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    private String tableName;

    @BeforeEach
    void setTableName() {
        tableName = "langchain4j_" + randomUUID().replace("-", "_");
    }

    @BeforeAll
    static void beforeAll() {
        postgres.start();
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
    }

    @Override
    protected Class<?> autoConfigurationClass() {
        return PgVectorEmbeddingStoreAutoConfiguration.class;
    }

    @Override
    protected Class<? extends EmbeddingStore<TextSegment>> embeddingStoreClass() {
        return PgVectorEmbeddingStore.class;
    }

    @Override
    protected String[] properties() {
        return new String[]{
                "langchain4j.pgvector.host=" + postgres.getHost(),
                "langchain4j.pgvector.port=" + postgres.getMappedPort(5432),
                "langchain4j.pgvector.user=" + postgres.getUsername(),
                "langchain4j.pgvector.password=" + postgres.getPassword(),
                "langchain4j.pgvector.database=" + postgres.getDatabaseName(),
                "langchain4j.pgvector.table=" + tableName
        };
    }

    @Override
    protected String dimensionPropertyKey() {
        return "langchain4j.pgvector.dimension";
    }
}
