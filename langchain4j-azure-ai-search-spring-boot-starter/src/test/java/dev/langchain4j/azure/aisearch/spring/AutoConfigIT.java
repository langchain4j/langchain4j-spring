package dev.langchain4j.azure.aisearch.spring;

import dev.langchain4j.model.embedding.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.azure.search.AzureAiSearchContentRetriever;
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
    private static final String AZURE_AISEARCH_MAX_RESULTS = System.getenv("AZURE_AISEARCH_MAX_RESULTS");
    private static final String AZURE_AISEARCH_MIN_SCORE = System.getenv("AZURE_AISEARCH_MIN_SCORE");

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AutoConfig.class));

    @Test
    void should_provide_ai_search_retriver_only_search() {
        contextRunner
                .withPropertyValues(
                        Properties.PREFIX + ".content-retriver.api-key=" + AZURE_AISEARCH_API_KEY,
                        Properties.PREFIX + ".content-retriver.endpoint=" + AZURE_AISEARCH_ENDPOINT,
                        Properties.PREFIX + ".content-retriver.create-or-update-index=" + "false",
                        Properties.PREFIX + ".content-retriver.query-type=" + "FULL_TEXT"
                ).run(context -> {
                    ContentRetriever contentRetriever = context.getBean(ContentRetriever.class);
                    assertThat(contentRetriever).isInstanceOf(AzureAiSearchContentRetriever.class);

                    assertThat(context.getBean(AzureAiSearchContentRetriever.class)).isSameAs(contentRetriever);
                });
    }

    @Test
    void should_provide_ai_search_retrive_create_or_update_indexr() {
        contextRunner
                .withPropertyValues(
                        Properties.PREFIX + ".content-retriver.api-key=" + AZURE_AISEARCH_API_KEY,
                        Properties.PREFIX + ".content-retriver.endpoint=" + AZURE_AISEARCH_ENDPOINT,
                        Properties.PREFIX + ".content-retriver.dimensions=" + AZURE_AISEARCH_DIMENSIONS,
                        Properties.PREFIX + ".content-retriver.create-or-update-index=" + "true",
                        Properties.PREFIX + ".content-retriver.max-results=" + AZURE_AISEARCH_MAX_RESULTS,
                        Properties.PREFIX + ".content-retriver.min-score=" + AZURE_AISEARCH_MIN_SCORE,
                        Properties.PREFIX + ".content-retriver.query-type=" + "VECTOR"
                ).withBean(EmbeddingModel.class, AllMiniLmL6V2EmbeddingModel::new)
                .run(context -> {
                    ContentRetriever contentRetriever = context.getBean(ContentRetriever.class);
                    assertThat(contentRetriever).isInstanceOf(AzureAiSearchContentRetriever.class);

                    assertThat(context.getBean(AzureAiSearchContentRetriever.class)).isSameAs(contentRetriever);
                });
    }

    @Test
    void should_provide_ai_search_embedding_store() {
        contextRunner
                .withPropertyValues(
                        Properties.PREFIX + ".embedding-store.api-key=" + AZURE_AISEARCH_API_KEY,
                        Properties.PREFIX + ".embedding-store.endpoint=" + AZURE_AISEARCH_ENDPOINT,
                        Properties.PREFIX + ".embedding-store.dimensions=" + AZURE_AISEARCH_DIMENSIONS,
                        Properties.PREFIX + ".embedding-store.create-or-update-index=" + "true"
                ).withBean(EmbeddingModel.class, AllMiniLmL6V2EmbeddingModel::new)
                .run(context -> {
                    EmbeddingStore embeddingStore = context.getBean(EmbeddingStore.class);
                    assertThat(embeddingStore).isInstanceOf(AzureAiSearchEmbeddingStore.class);

                    assertThat(context.getBean(AzureAiSearchEmbeddingStore.class)).isSameAs(embeddingStore);
                });
    }

}