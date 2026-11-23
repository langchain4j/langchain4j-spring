
package dev.langchain4j.store.embedding.chroma.spring;

import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ChromaPropertiesBindingTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner()
                    .withUserConfiguration(
                            ChromaEmbeddingStoreAutoConfiguration.class
                    );

    @Test
    void propertiesBindCorrectly() {
        contextRunner
                // Prevent creation of the real Chroma client.
                .withBean(
                        ChromaEmbeddingStore.class,
                        () -> mock(ChromaEmbeddingStore.class)
                )
                .withPropertyValues(
                        "langchain4j.chroma.url=http://localhost:8000",
                        "langchain4j.chroma.collection-name=test-collection",
                        "langchain4j.chroma.api-version=V2"
                )
                .run(context -> {
                    assertThat(context).hasNotFailed();

                    assertThat(context)
                            .hasSingleBean(ChromaEmbeddingStoreProperties.class);

                    ChromaEmbeddingStoreProperties properties =
                            context.getBean(
                                    ChromaEmbeddingStoreProperties.class
                            );

                    assertThat(properties.getUrl())
                            .isEqualTo("http://localhost:8000");

                    assertThat(properties.getCollectionName())
                            .isEqualTo("test-collection");

                    assertThat(properties.getApiVersion())
                            .isEqualTo("V2");
                });
    }
}
