package dev.langchain4j.azure.aisearch.spring;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.azure.search.AzureAiSearchEmbeddingStore;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class AutoConfigIT {

    private static final String AZURE_AISEARCH_API_KEY = System.getenv("AZURE_AISEARCH_API_KEY");
    private static final String AZURE_AISEARCH_ENDPOINT = System.getenv("AZURE_AISEARCH_ENDPOINT");
    private static final String AZURE_AISEARCH_DIMENSIONS = System.getenv("AZURE_AISEARCH_DIMENSIONS");
    private static final String AZURE_AISEARCH_SETUP_INDEX = System.getenv("AZURE_AISEARCH_SETUP_INDEX");


    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AutoConfig.class));

    @Test
    void should_provide_ai_search_store() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.azure.ai-search.api-key=" + AZURE_AISEARCH_API_KEY,
                        "langchain4j.azure.ai-search.endpoint=" + AZURE_AISEARCH_ENDPOINT,
                        "langchain4j.azure.ai-search.dimensions=" + AZURE_AISEARCH_DIMENSIONS,
                        "langchain4j.azure.ai-search.setup-index=" + AZURE_AISEARCH_SETUP_INDEX
                )
                .run(context -> {

                    EmbeddingStore<TextSegment> azureAiSearchEmbeddingStore = context.getBean(AzureAiSearchEmbeddingStore.class);
                    assertThat(azureAiSearchEmbeddingStore).isInstanceOf(AzureAiSearchEmbeddingStore.class);

                    assertThat(context.getBean(AzureAiSearchEmbeddingStore.class)).isSameAs(azureAiSearchEmbeddingStore);
                });
    }

}