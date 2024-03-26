package dev.langchain4j.azure.aisearch.spring;

import dev.langchain4j.model.embedding.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.azure.search.AzureAiSearchContentRetriever;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class AutoConfigIT {

    private static final String AZURE_AISEARCH_API_KEY = System.getenv("AZURE_AISEARCH_API_KEY");
    private static final String AZURE_AISEARCH_ENDPOINT = System.getenv("AZURE_AISEARCH_ENDPOINT");
    private static final String AZURE_AISEARCH_DIMENSIONS = System.getenv("AZURE_AISEARCH_DIMENSIONS");
    private static final String AZURE_AISEARCH_SETUP_INDEX = System.getenv("AZURE_AISEARCH_SETUP_INDEX");
    private static final String AZURE_AISEARCH_MAX_RESULTS = System.getenv("AZURE_AISEARCH_MAX_RESULTS");
    private static final String AZURE_AISEARCH_MIN_SCORE = System.getenv("AZURE_AISEARCH_MIN_SCORE");
    private static final String AZURE_AISEARCH_QUERY_TYPE = System.getenv("AZURE_AISEARCH_QUERY_TYPE");


    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AutoConfig.class));

//    @Test
//    void should_provide_ai_search_store() {
//        contextRunner
//                .withPropertyValues(
//                        "langchain4j.azure.ai-search.api-key=" + AZURE_AISEARCH_API_KEY,
//                        "langchain4j.azure.ai-search.endpoint=" + AZURE_AISEARCH_ENDPOINT,
//                        "langchain4j.azure.ai-search.dimensions=" + AZURE_AISEARCH_DIMENSIONS,
//                        "langchain4j.azure.ai-search.setup-index=" + AZURE_AISEARCH_SETUP_INDEX
//                )
//                .run(context -> {
//
//                    EmbeddingStore<TextSegment> azureAiSearchEmbeddingStore = context.getBean(AzureAiSearchEmbeddingStore.class);
//                    assertThat(azureAiSearchEmbeddingStore).isInstanceOf(AzureAiSearchEmbeddingStore.class);
//
//                    assertThat(context.getBean(AzureAiSearchEmbeddingStore.class)).isSameAs(azureAiSearchEmbeddingStore);
//                });
//    }

    @Test
    void should_provide_ai_search_retriver_only_search() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.azure.ai-search.api-key=" + AZURE_AISEARCH_API_KEY,
                        "langchain4j.azure.ai-search.endpoint=" + AZURE_AISEARCH_ENDPOINT,
                        "langchain4j.azure.ai-search.dimensions=" + "0",
                        "langchain4j.azure.ai-search.create-or-update-index=" + "false",
                        "langchain4j.azure.ai-search.max-results=" + AZURE_AISEARCH_MAX_RESULTS,
                        "langchain4j.azure.ai-search.min-score=" + AZURE_AISEARCH_MIN_SCORE,
                        "langchain4j.azure.ai-search.query-type=" + "FULL_TEXT"
                ).withBean(EmbeddingModel.class, AllMiniLmL6V2EmbeddingModel::new)
                .run(context -> {
                    ContentRetriever contentRetriever = context.getBean(AzureAiSearchContentRetriever.class);
                    assertThat(contentRetriever).isInstanceOf(AzureAiSearchContentRetriever.class);

                    assertThat(context.getBean(ContentRetriever.class)).isSameAs(contentRetriever);
                });
    }

    @Test
    void should_provide_ai_search_retrive_create_or_uodate_indexr() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.azure.ai-search.api-key=" + AZURE_AISEARCH_API_KEY,
                        "langchain4j.azure.ai-search.endpoint=" + AZURE_AISEARCH_ENDPOINT,
                        "langchain4j.azure.ai-search.dimensions=" + AZURE_AISEARCH_DIMENSIONS,
                        "langchain4j.azure.ai-search.create-or-update-index=" + "true",
                        "langchain4j.azure.ai-search.max-results=" + AZURE_AISEARCH_MAX_RESULTS,
                        "langchain4j.azure.ai-search.min-score=" + AZURE_AISEARCH_MIN_SCORE,
                        "langchain4j.azure.ai-search.query-type=" + "VECTOR"
                ).withBean(EmbeddingModel.class, AllMiniLmL6V2EmbeddingModel::new)
                .run(context -> {
                    ContentRetriever contentRetriever = context.getBean(AzureAiSearchContentRetriever.class);
                    assertThat(contentRetriever).isInstanceOf(AzureAiSearchContentRetriever.class);

                    assertThat(context.getBean(ContentRetriever.class)).isSameAs(contentRetriever);
                });
    }

}