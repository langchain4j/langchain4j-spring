package dev.langchain4j.store.embedding.pgvector.spring;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import dev.langchain4j.store.embedding.spring.EmbeddingStoreAutoConfigurationIT;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.PostgreSQLContainer;

import static dev.langchain4j.internal.Utils.randomUUID;

class PgVectorEmbeddingStoreAutoConfigurationIT extends EmbeddingStoreAutoConfigurationIT {

    static PostgreSQLContainer<?> pgVector = new PostgreSQLContainer<>("pgvector/pgvector:pg16");

    private String tableName;

    @BeforeAll
    static void beforeAll() {
        pgVector.start();
    }

    @AfterAll
    static void afterAll() {
        pgVector.stop();
    }

    @BeforeEach
    void setTableName() {
        tableName = "langchain4j_" + randomUUID().replace("-", "_");
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
                "langchain4j.pgvector.host=" + pgVector.getHost(),
                "langchain4j.pgvector.port=" + pgVector.getMappedPort(5432),
                "langchain4j.pgvector.user=" + pgVector.getUsername(),
                "langchain4j.pgvector.password=" + pgVector.getPassword(),
                "langchain4j.pgvector.database=" + pgVector.getDatabaseName(),
                "langchain4j.pgvector.table=" + tableName,
                "langchain4j.pgvector.create-table=true"
        };
    }

    @Override
    protected String dimensionPropertyKey() {
        return "langchain4j.pgvector.dimension";
    }
}
