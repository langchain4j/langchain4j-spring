package dev.langchain4j.store.embedding.chroma.spring;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import dev.langchain4j.store.embedding.spring.EmbeddingStoreAutoConfigurationIT;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.chromadb.ChromaDBContainer;

class ChromaEmbeddingStoreAutoConfigurationIT extends EmbeddingStoreAutoConfigurationIT {

    static ChromaDBContainer chroma = new ChromaDBContainer("chromadb/chroma:0.5.0");

    @BeforeAll
    static void beforeAll() {
        chroma.start();
    }

    @AfterAll
    static void afterAll() {
        chroma.stop();
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
                "langchain4j.chroma.baseUrl=" + chroma.getEndpoint()
        };
    }

    @Override
    protected String dimensionPropertyKey() {
        return "langchain4j.chroma.dimension";
    }
}
