package dev.langchain4j.store.embedding.chroma.spring;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.onnx.allminilml6v2q.AllMiniLmL6V2QuantizedEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import dev.langchain4j.store.embedding.spring.EmbeddingStoreAutoConfigurationIT;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.testcontainers.chromadb.ChromaDBContainer;
import org.testcontainers.containers.wait.strategy.Wait;

class ChromaEmbeddingStoreAutoConfigurationIT extends EmbeddingStoreAutoConfigurationIT {

    private static final String DEFAULT_DOCKER_IMAGE = "chromadb/chroma";
    static ChromaDBContainer chromadb = new ChromaDBContainer(DEFAULT_DOCKER_IMAGE)
            .waitingFor(Wait.defaultWaitStrategy());
    static final String COLLECTION_NAME = "test_collection";

    @BeforeAll
    static void beforeAll() {
        chromadb.start();
    }

    @AfterAll
    static void afterAll() {
        chromadb.stop();
    }

    @BeforeEach
    void beforeEach() {
        ApplicationContextRunner contextRunner = new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(autoConfigurationClass()));

        contextRunner
                .withBean(AllMiniLmL6V2QuantizedEmbeddingModel.class)
                .withPropertyValues(properties())
                .run(context -> {
                    ChromaEmbeddingStore embeddingStore = context.getBean(ChromaEmbeddingStore.class);
                    embeddingStore.removeAll();
                });
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
                "langchain4j.chroma.baseUrl=" + chromadb.getEndpoint(),
                "langchain4j.chroma.collectionName=" + COLLECTION_NAME,
        };
    }

    @Override
    protected String dimensionPropertyKey() {
        return "";
    }
}