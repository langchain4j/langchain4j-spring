package dev.langchain4j.store.embedding.elasticsearch.spring;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.elasticsearch.ElasticsearchEmbeddingStore;
import dev.langchain4j.store.embedding.spring.EmbeddingStoreAutoConfigurationIT;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

import static dev.langchain4j.internal.Utils.randomUUID;

class ElasticsearchEmbeddingStoreAutoConfigurationIT extends EmbeddingStoreAutoConfigurationIT {

    static ElasticsearchContainer elasticsearch = new ElasticsearchContainer("elasticsearch:8.9.0")
            .withEnv("xpack.security.enabled", "false")
            .waitingFor(Wait.defaultWaitStrategy());

    @BeforeAll
    static void beforeAll() {
        elasticsearch.start();
    }

    @AfterAll
    static void afterAll() {
        elasticsearch.stop();
    }

    @Override
    protected Class<?> autoConfigurationClass() {
        return ElasticsearchEmbeddingStoreAutoConfiguration.class;
    }

    @Override
    protected Class<? extends EmbeddingStore<TextSegment>> embeddingStoreClass() {
        return ElasticsearchEmbeddingStore.class;
    }

    @Override
    protected String[] properties() {
        return new String[]{
                "langchain4j.elasticsearch.serverUrl=" + elasticsearch.getHttpHostAddress(),
                "langchain4j.elasticsearch.indexName=" + randomUUID()
        };
    }

    @Override
    @SneakyThrows
    protected void awaitUntilPersisted() {
        Thread.sleep(1000);
    }
}
