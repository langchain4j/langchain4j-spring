package dev.langchain4j.infinispan.spring;

import static dev.langchain4j.internal.Utils.randomUUID;
import static org.infinispan.testcontainers.InfinispanContainer.IMAGE_BASENAME;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.infinispan.InfinispanEmbeddingStore;
import dev.langchain4j.store.embedding.spring.EmbeddingStoreAutoConfigurationIT;
import org.infinispan.commons.util.Version;
import org.infinispan.testcontainers.InfinispanContainer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.context.ApplicationContext;

class InfinispanEmbeddingStoreAutoConfigurationIT extends EmbeddingStoreAutoConfigurationIT {

    static InfinispanContainer infinispan = new InfinispanContainer(IMAGE_BASENAME + ":" + Version.getVersion());

    String cacheName;

    @BeforeAll
    static void beforeAll() {
        infinispan.start();
    }

    @AfterAll
    static void afterAll() {
        infinispan.stop();
    }

    @BeforeEach
    void setCacheName() {
        cacheName = "cache_" + randomUUID().replace("-", "");
    }

    @Override
    protected Class<?> autoConfigurationClass() {
        return InfinispanEmbeddingStoreAutoConfiguration.class;
    }

    @Override
    protected Class<? extends EmbeddingStore<TextSegment>> embeddingStoreClass() {
        return InfinispanEmbeddingStore.class;
    }

    @Override
    protected String[] properties() {
        return new String[]{
                "langchain4j.infinispan.host=" + infinispan.getHost(),
                "langchain4j.infinispan.port=" + infinispan.getMappedPort(11222),
                "langchain4j.infinispan.username=admin",
                "langchain4j.infinispan.password=secret",
                "langchain4j.infinispan.cache-name=" + cacheName
        };
    }

    @Override
    protected String dimensionPropertyKey() {
        return "langchain4j.infinispan.dimension";
    }

    @Override
    protected void awaitUntilPersisted(ApplicationContext context) {
    }
}
