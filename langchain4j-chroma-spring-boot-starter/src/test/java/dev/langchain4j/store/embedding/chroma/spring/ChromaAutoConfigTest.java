package dev.langchain4j.store.embedding.chroma.spring;

import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ChromaAutoConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(ChromaEmbeddingStoreAutoConfiguration.class);

    @Test
    void contextCreatesChromaEmbeddingStoreBean() {
        contextRunner
                // Building a real ChromaEmbeddingStore performs HTTP requests during initialization.
                // Supply a mock so this auto-configuration test remains independent of a running Chroma server.
                .withBean(ChromaEmbeddingStore.class, () -> mock(ChromaEmbeddingStore.class))
                .withPropertyValues(
                        "langchain4j.chroma.url=http://localhost:8000",
                        "langchain4j.chroma.collection-name=test-collection"
                ).run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(ChromaEmbeddingStore.class);
        });
    }
}