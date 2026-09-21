package dev.langchain4j.store.embedding.chroma.spring;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import dev.langchain4j.store.embedding.spring.EmbeddingStoreAutoConfigurationIT;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

class ChromaEmbeddingStoreAutoConfigurationIT extends EmbeddingStoreAutoConfigurationIT {

    // Define a Testcontainers instance for Chroma
    static GenericContainer<?> chromaContainer = new GenericContainer<>("chroma-core:latest")
            .withExposedPorts(8000) // Chroma typically runs on port 8000
            .waitingFor(Wait.forHttp("/").forStatusCode(200)); // Wait until Chroma service is ready

    @BeforeAll
    static void beforeAll() {
        chromaContainer.start();
    }

    @AfterAll
    static void afterAll() {
        chromaContainer.stop();
    }

    @Override
    protected Class<?> autoConfigurationClass() {
        return ChromaEmbeddingStoreAutoConfiguration.class;
    }

    @Override
    protected Class<? extends EmbeddingStore<TextSegment>> embeddingStoreClass() {
        return ChromaEmbeddingStore.class;
    }

    @Override
    protected String[] properties() {
        return new String[]{
                "langchain4j.chroma.base-url=http://" + chromaContainer.getHost() + ":" + chromaContainer.getMappedPort(8000),
                "langchain4j.chroma.collection-name=test-collection",
                "langchain4j.chroma.timeout=5s",
                "langchain4j.chroma.log-requests=true",
                "langchain4j.chroma.log-responses=false"
        };
    }

    @Override
    protected String dimensionPropertyKey() {
        // Chroma doesn't require a dimension property; return null or a placeholder key if needed
        return "langchain4j.chroma.dimension";
    }
}