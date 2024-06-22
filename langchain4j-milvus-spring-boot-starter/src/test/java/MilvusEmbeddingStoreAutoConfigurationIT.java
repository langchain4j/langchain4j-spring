import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.AllMiniLmL6V2QuantizedEmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore;
import dev.langchain4j.store.embedding.milvus.spring.MilvusEmbeddingStoreAutoConfiguration;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.testcontainers.milvus.MilvusContainer;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Percentage.withPercentage;

class MilvusEmbeddingStoreAutoConfigurationIT {

    static MilvusContainer milvus = new MilvusContainer("milvusdb/milvus:v2.3.16");

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(MilvusEmbeddingStoreAutoConfiguration.class));

    @BeforeAll
    static void beforeAll() {
        milvus.start();
    }

    @AfterAll
    static void afterAll() {
        milvus.stop();
    }

    @Test
    void should_provide_elasticsearch_vector_store() {
        TextSegment segment = TextSegment.from("hello");
        contextRunner
                .withBean(AllMiniLmL6V2QuantizedEmbeddingModel.class)
                .withPropertyValues(
                        "langchain4j.milvus.host=" + milvus.getHost(),
                        "langchain4j.milvus.port=" + milvus.getMappedPort(19530)
                )
                .run(context -> {
                    EmbeddingModel embeddingModel = context.getBean(AllMiniLmL6V2QuantizedEmbeddingModel.class);
                    Embedding embedding = embeddingModel.embed(segment.text()).content();

                    EmbeddingStore<TextSegment> embeddingStore = context.getBean(MilvusEmbeddingStore.class);
                    assertThat(embeddingStore).isInstanceOf(MilvusEmbeddingStore.class);

                    String id = embeddingStore.add(embedding, segment);
                    assertThat(id).isNotBlank();

                    awaitUntilPersisted();

                    List<EmbeddingMatch<TextSegment>> relevant = embeddingStore.findRelevant(embedding, 10);
                    assertThat(relevant).hasSize(1);

                    EmbeddingMatch<TextSegment> match = relevant.get(0);
                    assertThat(match.score()).isCloseTo(1, withPercentage(1));
                    assertThat(match.embeddingId()).isEqualTo(id);
                    assertThat(match.embedding()).isEqualTo(embedding);
                    assertThat(match.embedded()).isEqualTo(segment);
                });
    }

    @SneakyThrows
    private void awaitUntilPersisted() {
        Thread.sleep(1000);
    }
}
