package dev.langchain4j.store.embedding.pgvector.spring;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.onnx.allminilml6v2q.AllMiniLmL6V2QuantizedEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import dev.langchain4j.store.embedding.spring.EmbeddingStoreAutoConfigurationIT;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.testcontainers.containers.PostgreSQLContainer;

class PgVectorEmbeddingStoreAutoConfigurationIT extends EmbeddingStoreAutoConfigurationIT {

    static PostgreSQLContainer<?> pgVector = new PostgreSQLContainer<>("pgvector/pgvector:pg16");
    static final String DEFAULT_TABLE = "test_langchain4j_table";

    @BeforeAll
    static void beforeAll() {
        pgVector.start();
    }

    @AfterAll
    static void afterAll() {
        pgVector.stop();
    }

    @BeforeEach
    void beforeEach() {
        ApplicationContextRunner contextRunner = new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(autoConfigurationClass()));

        contextRunner
                .withBean(AllMiniLmL6V2QuantizedEmbeddingModel.class)
                .withPropertyValues(properties())
                .run(context -> {
                    PgVectorEmbeddingStore embeddingStore = context.getBean(PgVectorEmbeddingStore.class);
                    embeddingStore.removeAll();
                });
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
                "spring.datasource.url=" + pgVector.getJdbcUrl(),
                "spring.datasource.username=" + pgVector.getUsername(),
                "spring.datasource.password=" + pgVector.getPassword(),
                "spring.datasource.driver-class-name=" + pgVector.getDriverClassName(),
                "langchain4j.pgvector.create-table=true",
                "langchain4j.pgvector.table=" + DEFAULT_TABLE,
                "langchain4j.pgvector.use-index=true",
                "langchain4j.pgvector.index-list-size=100",
                "langchain4j.pgvector.dimension=384"
        };
    }

    @Override
    protected String dimensionPropertyKey() {
        return "langchain4j.pgvector.dimension";
    }
}
