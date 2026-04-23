package dev.langchain4j.store.embedding.pgvector.spring;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import dev.langchain4j.store.embedding.spring.EmbeddingStoreAutoConfigurationIT;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

import static dev.langchain4j.internal.Utils.randomUUID;

class PgVectorEmbeddingStoreAutoConfigurationIT extends EmbeddingStoreAutoConfigurationIT {

    static final PostgreSQLContainer<?> pgVector = new PostgreSQLContainer<>(
            DockerImageName.parse("pgvector/pgvector:pg16")
    ).withStartupTimeout(Duration.ofMinutes(2));

    String tableName;

    @BeforeAll
    static void startContainer() {
        pgVector.start();
    }

    @AfterAll
    static void stopContainer() {
        pgVector.stop();
    }

    @BeforeEach
    void setTableName() {
        tableName = "embeddings_" + randomUUID().replace("-", "_");
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
                "spring.langchain4j.pgvector.host=" + pgVector.getHost(),
                "spring.langchain4j.pgvector.port=" + pgVector.getMappedPort(5432),
                "spring.langchain4j.pgvector.database=" + pgVector.getDatabaseName(),
                "spring.langchain4j.pgvector.user=" + pgVector.getUsername(),
                "spring.langchain4j.pgvector.password=" + pgVector.getPassword(),
                "spring.langchain4j.pgvector.tableName=" + tableName,
                "spring.langchain4j.pgvector.dimension=384"
        };
    }

    @Override
    protected String dimensionPropertyKey() {
        return "spring.langchain4j.pgvector.dimension";
    }
}
