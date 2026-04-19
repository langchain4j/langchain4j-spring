package dev.langchain4j.store.embedding.pgvector.spring;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2q.AllMiniLmL6V2QuantizedEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.testcontainers.containers.PostgreSQLContainer;

import static dev.langchain4j.internal.Utils.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

class PgVectorEmbeddingStoreDataSourceAutoConfigurationIT {

    static PostgreSQLContainer<?> pgVector = new PostgreSQLContainer<>("pgvector/pgvector:pg16");

    @BeforeAll
    static void beforeAll() {
        pgVector.start();
    }

    @AfterAll
    static void afterAll() {
        pgVector.stop();
    }

    private String randomTable() {
        return "langchain4j_" + randomUUID().replace("-", "_");
    }

    @Test
    void should_create_embedding_store_using_existing_datasource() {
        String tableName = randomTable();

        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        DataSourceAutoConfiguration.class,
                        PgVectorEmbeddingStoreAutoConfiguration.class))
                .withPropertyValues(
                        "spring.datasource.url=" + pgVector.getJdbcUrl(),
                        "spring.datasource.username=" + pgVector.getUsername(),
                        "spring.datasource.password=" + pgVector.getPassword(),
                        "langchain4j.pgvector.table=" + tableName,
                        "langchain4j.pgvector.dimension=384",
                        "langchain4j.pgvector.create-table=true"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(PgVectorEmbeddingStore.class);

                    PgVectorEmbeddingStore store = context.getBean(PgVectorEmbeddingStore.class);
                    EmbeddingModel embeddingModel = new AllMiniLmL6V2QuantizedEmbeddingModel();
                    TextSegment segment = TextSegment.from("test with existing datasource");
                    Embedding embedding = embeddingModel.embed(segment.text()).content();

                    String id = store.add(embedding, segment);
                    assertThat(id).isNotBlank();

                    EmbeddingSearchResult<TextSegment> result = store.search(
                            EmbeddingSearchRequest.builder()
                                    .queryEmbedding(embedding)
                                    .maxResults(1)
                                    .build());
                    assertThat(result.matches()).hasSize(1);
                    assertThat(result.matches().get(0).embedded()).isEqualTo(segment);
                });
    }

    @Test
    void should_fail_when_no_postgres_datasource_and_no_explicit_properties() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        PgVectorEmbeddingStoreAutoConfiguration.class))
                .withPropertyValues(
                        "langchain4j.pgvector.table=test_table",
                        "langchain4j.pgvector.dimension=384",
                        "langchain4j.pgvector.create-table=true"
                )
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .rootCause()
                            .isInstanceOf(IllegalStateException.class)
                            .hasMessageContaining("No suitable PostgreSQL DataSource found");
                });
    }

    @Test
    void should_fail_when_non_postgres_datasource_present_and_no_explicit_properties() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        DataSourceAutoConfiguration.class,
                        PgVectorEmbeddingStoreAutoConfiguration.class))
                .withPropertyValues(
                        "spring.datasource.url=jdbc:h2:mem:testdb",
                        "spring.datasource.driver-class-name=org.h2.Driver",
                        "langchain4j.pgvector.table=test_table",
                        "langchain4j.pgvector.dimension=384",
                        "langchain4j.pgvector.create-table=true"
                )
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .rootCause()
                            .isInstanceOf(IllegalStateException.class)
                            .hasMessageContaining("No suitable PostgreSQL DataSource found");
                });
    }

    @Test
    void should_not_create_bean_when_disabled() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        PgVectorEmbeddingStoreAutoConfiguration.class))
                .withPropertyValues("langchain4j.pgvector.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(PgVectorEmbeddingStore.class);
                });
    }

    @Test
    void should_prefer_explicit_properties_over_datasource() {
        String tableName = randomTable();

        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        DataSourceAutoConfiguration.class,
                        PgVectorEmbeddingStoreAutoConfiguration.class))
                .withPropertyValues(
                        // Spring DataSource pointing to the same PG for simplicity
                        "spring.datasource.url=" + pgVector.getJdbcUrl(),
                        "spring.datasource.username=" + pgVector.getUsername(),
                        "spring.datasource.password=" + pgVector.getPassword(),
                        // Explicit pgvector properties — these should take priority
                        "langchain4j.pgvector.host=" + pgVector.getHost(),
                        "langchain4j.pgvector.port=" + pgVector.getMappedPort(5432),
                        "langchain4j.pgvector.user=" + pgVector.getUsername(),
                        "langchain4j.pgvector.password=" + pgVector.getPassword(),
                        "langchain4j.pgvector.database=" + pgVector.getDatabaseName(),
                        "langchain4j.pgvector.table=" + tableName,
                        "langchain4j.pgvector.dimension=384",
                        "langchain4j.pgvector.create-table=true"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(PgVectorEmbeddingStore.class);

                    PgVectorEmbeddingStore store = context.getBean(PgVectorEmbeddingStore.class);
                    EmbeddingModel embeddingModel = new AllMiniLmL6V2QuantizedEmbeddingModel();
                    Embedding embedding = embeddingModel.embed("test").content();

                    String id = store.add(embedding, TextSegment.from("test"));
                    assertThat(id).isNotBlank();
                });
    }

    @Test
    void should_use_embedding_model_dimension_when_not_specified() {
        String tableName = randomTable();

        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        DataSourceAutoConfiguration.class,
                        PgVectorEmbeddingStoreAutoConfiguration.class))
                .withBean(AllMiniLmL6V2QuantizedEmbeddingModel.class)
                .withPropertyValues(
                        "spring.datasource.url=" + pgVector.getJdbcUrl(),
                        "spring.datasource.username=" + pgVector.getUsername(),
                        "spring.datasource.password=" + pgVector.getPassword(),
                        "langchain4j.pgvector.table=" + tableName,
                        "langchain4j.pgvector.create-table=true"
                        // Note: no dimension property — should fall back to EmbeddingModel
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(PgVectorEmbeddingStore.class);

                    PgVectorEmbeddingStore store = context.getBean(PgVectorEmbeddingStore.class);
                    EmbeddingModel embeddingModel = context.getBean(AllMiniLmL6V2QuantizedEmbeddingModel.class);
                    Embedding embedding = embeddingModel.embed("dimension fallback test").content();

                    String id = store.add(embedding, TextSegment.from("dimension fallback test"));
                    assertThat(id).isNotBlank();
                });
    }
}
