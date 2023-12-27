package dev.langchain4j.store.embedding.elasticsearch.spring;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.AllMiniLmL6V2QuantizedEmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.elasticsearch.ElasticsearchEmbeddingStore;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

import java.util.List;

import static dev.langchain4j.internal.Utils.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Percentage.withPercentage;

class ElasticsearchEmbeddingStoreAutoConfigurationIT {

    static ElasticsearchContainer elasticsearch = new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:8.9.0")
            .withEnv("xpack.security.enabled", "false");

    EmbeddingModel embeddingModel = new AllMiniLmL6V2QuantizedEmbeddingModel();

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ElasticsearchEmbeddingStoreAutoConfiguration.class));

    @BeforeAll
    static void beforeAll() {
        elasticsearch.start();
    }

    @AfterAll
    static void afterAll() {
        elasticsearch.stop();
    }

    @Test
    void should_provide_redis_vector_store() {
        TextSegment segment = TextSegment.from("hello");
        Embedding embedding = embeddingModel.embed(segment.text()).content();
        contextRunner
                .withPropertyValues(
                        "langchain4j.redis.enabled=true",
                        "langchain4j.elasticsearch.serverUrl=" + elasticsearch.getHttpHostAddress(),
                        "langchain4j.redis.indexName=" + randomUUID(),
                        "langchain4j.redis.dimension=" + 384
                )
                .run(context -> {
                    EmbeddingStore<TextSegment> embeddingStore = context.getBean(ElasticsearchEmbeddingStore.class);
                    assertThat(embeddingStore).isInstanceOf(ElasticsearchEmbeddingStore.class);

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
