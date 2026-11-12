package dev.langchain4j.store.embedding.chroma.spring;

import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ChromaAutoConfigWithMockTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(ChromaEmbeddingStoreAutoConfiguration.class);

    @Test
    void autoConfigBacksOffWhenBeanPresent_and_contextStarts() {
        contextRunner
            // register a mock so auto-config doesn't instantiate the real store (no network)
            .withBean(ChromaEmbeddingStore.class, () -> mock(ChromaEmbeddingStore.class))
            .withPropertyValues(
                "langchain4j.chroma.url=http://localhost:8000",
                "langchain4j.chroma.collection-name=test-collection"
            )
            .run(context -> {
                assertThat(context).hasNotFailed();
                // We provided the mock; bean of this type should be present
                assertThat(context).hasSingleBean(ChromaEmbeddingStore.class);
            });
    }
}
