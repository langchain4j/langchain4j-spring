import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import dev.langchain4j.store.embedding.chroma.spring.ChromaEmbeddingStoreAutoConfiguration;
import dev.langchain4j.store.embedding.spring.EmbeddingStoreAutoConfigurationIT;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.chromadb.ChromaDBContainer;

import static dev.langchain4j.internal.Utils.randomUUID;

class ChromaEmbeddingStoreAutoConfigurationIT extends EmbeddingStoreAutoConfigurationIT {

    static ChromaDBContainer chroma = new ChromaDBContainer("chromadb/chroma:1.3.0");

    String collectionName;

    @BeforeEach
    void setCollectionName() {
        collectionName = "langchain4j-" + randomUUID().replace("_", "-");
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
                "langchain4j.chroma.baseUrl=" + "http://" + chroma.getHost() + ":" + chroma.getMappedPort(8000),
                "langchain4j.chroma.collectionName=" + collectionName
        };
    }

    @Override
    protected String dimensionPropertyKey() {
        return "langchain4j.chroma.dimension";
    }
}
