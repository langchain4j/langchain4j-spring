package dev.langchain4j.store.embedding.chroma.spring;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import dev.langchain4j.store.embedding.spring.EmbeddingStoreAutoConfigurationIT;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.chromadb.ChromaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static dev.langchain4j.internal.Utils.randomUUID;

@Testcontainers
class ChromaEmbeddingStoreAutoConfigurationIT extends EmbeddingStoreAutoConfigurationIT {

    @Container
    static ChromaDBContainer chroma = new ChromaDBContainer("chromadb/chroma:0.5.4");

    private String collectionName;

    @BeforeEach
    void setCollectionName() {
        collectionName = "langchain4j_" + randomUUID().replace("-", "_");
    }

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
                "langchain4j.chroma.base-url=" + chroma.getEndpoint(),
                "langchain4j.chroma.collection-name=" + collectionName
        };
    }

    @Override
    protected String dimensionPropertyKey() {
        return "langchain4j.chroma.dimension";
    }
}
