package dev.langchain4j.store.embedding.redis.spring;

import com.redis.testcontainers.RedisContainer;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.AllMiniLmL6V2QuantizedEmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.redis.RedisEmbeddingStore;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.testcontainers.containers.wait.strategy.Wait;

import java.util.List;

import static com.redis.testcontainers.RedisStackContainer.DEFAULT_IMAGE_NAME;
import static com.redis.testcontainers.RedisStackContainer.DEFAULT_TAG;
import static dev.langchain4j.internal.Utils.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Percentage.withPercentage;

class RedisEmbeddingStoreAutoConfigurationIT {

    static RedisContainer redis = new RedisContainer(DEFAULT_IMAGE_NAME.withTag(DEFAULT_TAG))
            .waitingFor(Wait.defaultWaitStrategy());

    EmbeddingModel embeddingModel = new AllMiniLmL6V2QuantizedEmbeddingModel();

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(RedisEmbeddingStoreAutoConfiguration.class));

    @BeforeAll
    static void beforeAll() {
        redis.start();
    }

    @AfterAll
    static void afterAll() {
        redis.stop();
    }

    @Test
    void should_provide_redis_vector_store() {
        TextSegment segment = TextSegment.from("hello");
        Embedding embedding = embeddingModel.embed(segment.text()).content();
        contextRunner
                .withPropertyValues(
                        "langchain4j.redis.enabled=true",
                        "langchain4j.redis.host=" + redis.getHost(),
                        "langchain4j.redis.port=" + redis.getFirstMappedPort(),
                        "langchain4j.redis.indexName=" + randomUUID(),
                        "langchain4j.redis.dimension=" + 384,
                        "langchain4j.redis.metadataFieldsName=test-key"
                )
                .run(context -> {
                    EmbeddingStore<TextSegment> embeddingStore = context.getBean(RedisEmbeddingStore.class);
                    assertThat(embeddingStore).isInstanceOf(RedisEmbeddingStore.class);

                    String id = embeddingStore.add(embedding, segment);
                    assertThat(id).isNotBlank();

                    List<EmbeddingMatch<TextSegment>> relevant = embeddingStore.findRelevant(embedding, 10);
                    assertThat(relevant).hasSize(1);

                    EmbeddingMatch<TextSegment> match = relevant.get(0);
                    assertThat(match.score()).isCloseTo(1, withPercentage(1));
                    assertThat(match.embeddingId()).isEqualTo(id);
                    assertThat(match.embedding()).isEqualTo(embedding);
                    assertThat(match.embedded()).isEqualTo(segment);
                });
    }
}
