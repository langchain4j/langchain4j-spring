package dev.langchain4j.store.embedding.redis.spring;

import com.redis.testcontainers.RedisStackContainer;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.redis.RedisEmbeddingStore;
import dev.langchain4j.store.embedding.spring.EmbeddingStoreAutoConfigurationIT;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import static com.redis.testcontainers.RedisStackContainer.DEFAULT_IMAGE_NAME;
import static com.redis.testcontainers.RedisStackContainer.DEFAULT_TAG;
import static dev.langchain4j.internal.Utils.randomUUID;

class RedisEmbeddingStoreAutoConfigurationIT extends EmbeddingStoreAutoConfigurationIT {

    static RedisStackContainer redis = new RedisStackContainer(DEFAULT_IMAGE_NAME.withTag(DEFAULT_TAG));

    String indexName;

    @BeforeAll
    static void beforeAll() {
        redis.start();
    }

    @AfterAll
    static void afterAll() {
        redis.stop();
    }

    @BeforeEach
    void setIndexName() {
        indexName = randomUUID();
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
                "langchain4j.redis.port=" + redis.getFirstMappedPort(),
                "langchain4j.redis.prefix=" + indexName + ":",
                "langchain4j.redis.index-name=" + indexName
        };
    }

    @Override
    protected String dimensionPropertyKey() {
        return "langchain4j.redis.dimension";
    }
}
