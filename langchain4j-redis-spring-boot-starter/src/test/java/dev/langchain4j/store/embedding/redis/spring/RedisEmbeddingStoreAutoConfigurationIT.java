package dev.langchain4j.store.embedding.redis.spring;

import com.redis.testcontainers.RedisContainer;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.redis.RedisEmbeddingStore;
import dev.langchain4j.store.embedding.spring.EmbeddingStoreAutoConfigurationIT;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.wait.strategy.Wait;
import redis.clients.jedis.JedisPooled;

import static com.redis.testcontainers.RedisStackContainer.DEFAULT_IMAGE_NAME;
import static com.redis.testcontainers.RedisStackContainer.DEFAULT_TAG;

class RedisEmbeddingStoreAutoConfigurationIT extends EmbeddingStoreAutoConfigurationIT {

    static RedisContainer redis = new RedisContainer(DEFAULT_IMAGE_NAME.withTag(DEFAULT_TAG))
            .waitingFor(Wait.defaultWaitStrategy());

    @BeforeAll
    static void beforeAll() {
        redis.start();
    }

    @AfterAll
    static void afterAll() {
        redis.stop();
    }

    @BeforeEach
    void beforeEach() {
        try (JedisPooled jedis = new JedisPooled(redis.getHost(), redis.getFirstMappedPort())) {
            jedis.flushDB(); // TODO fix: why redis returns embeddings from different indexes?
        }
    }

    @Override
    protected Class<?> autoConfigurationClass() {
        return RedisEmbeddingStoreAutoConfiguration.class;
    }

    @Override
    protected Class<? extends EmbeddingStore<TextSegment>> embeddingStoreClass() {
        return RedisEmbeddingStore.class;
    }

    @Override
    protected String[] properties() {
        return new String[]{
                "langchain4j.redis.host=" + redis.getHost(),
                "langchain4j.redis.port=" + redis.getFirstMappedPort()
        };
    }

    @Override
    protected String dimensionPropertyKey() {
        return "langchain4j.redis.dimension";
    }
}
