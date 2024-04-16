package dev.langchain4j.azure.aisearch.spring;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.indexes.models.SearchIndex;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.azure.search.AzureAiSearchContentRetriever;
import dev.langchain4j.rag.content.retriever.azure.search.AzureAiSearchQueryType;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.azure.search.AzureAiSearchEmbeddingStore;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.List;

import static dev.langchain4j.store.embedding.azure.search.AbstractAzureAiSearchEmbeddingStore.INDEX_NAME;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

class AutoConfigIT {

    private static final String AZURE_SEARCH_KEY = System.getenv("AZURE_SEARCH_KEY");
    private static final String AZURE_SEARCH_ENDPOINT = System.getenv("AZURE_SEARCH_ENDPOINT");
    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AutoConfig.class));

    private static final Logger log = LoggerFactory.getLogger(AutoConfigIT.class);

    private final EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();
    private final int dimensions = embeddingModel.embed("test").content().vector().length;

    private final SearchIndexClient searchIndexClient = new SearchIndexClientBuilder()
            .endpoint(System.getenv("AZURE_SEARCH_ENDPOINT"))
            .credential(new AzureKeyCredential(System.getenv("AZURE_SEARCH_KEY")))
            .buildClient();

    private SearchIndex index = new SearchIndex(INDEX_NAME);

    @Test
    void should_provide_ai_search_retriever() {

        searchIndexClient.deleteIndex(INDEX_NAME);

        contextRunner
                .withPropertyValues(
                        Properties.PREFIX + ".content-retriever.api-key=" + AZURE_SEARCH_KEY,
                        Properties.PREFIX + ".content-retriever.endpoint=" + AZURE_SEARCH_ENDPOINT,
                        Properties.PREFIX + ".content-retriever.dimensions=" + dimensions,
                        Properties.PREFIX + ".content-retriever.create-or-update-index=" + "true",
                        Properties.PREFIX + ".content-retriever.query-type=" + "VECTOR"
                ).withBean(EmbeddingModel.class, () -> embeddingModel)
                .run(context -> {
                    ContentRetriever contentRetriever = context.getBean(ContentRetriever.class);
                    assertThat(contentRetriever).isInstanceOf(AzureAiSearchContentRetriever.class);
                    AzureAiSearchContentRetriever azureAiSearchContentRetriever = (AzureAiSearchContentRetriever) contentRetriever;

                    String content1 = "This book is about politics";
                    String content2 = "Cats sleeps a lot.";
                    String content3 = "Sandwiches taste good.";
                    String content4 = "The house is open";
                    List<String> contents = asList(content1, content2, content3, content4);

                    for (String content : contents) {
                        TextSegment textSegment = TextSegment.from(content);
                        Embedding embedding = embeddingModel.embed(content).content();
                        azureAiSearchContentRetriever.add(embedding, textSegment);
                    }

                    awaitUntilPersisted();
                });

        String content = "house";
        Query query = Query.from(content);

        contextRunner
                .withPropertyValues(
                        Properties.PREFIX + ".content-retriever.api-key=" + AZURE_SEARCH_KEY,
                        Properties.PREFIX + ".content-retriever.endpoint=" + AZURE_SEARCH_ENDPOINT,
                        Properties.PREFIX + ".content-retriever.create-or-update-index=" + "false",
                        Properties.PREFIX + ".content-retriever.max-results=" + "3",
                        Properties.PREFIX + ".content-retriever.min-score=" + "0.6",
                        Properties.PREFIX + ".content-retriever.query-type=" + AzureAiSearchQueryType.VECTOR
                ).withBean(SearchIndex.class, () -> index)
                .withBean(EmbeddingModel.class, () -> embeddingModel)
                .run(context -> {
                    ContentRetriever contentRetriever = context.getBean(ContentRetriever.class);
                    assertThat(contentRetriever).isInstanceOf(AzureAiSearchContentRetriever.class);
                    AzureAiSearchContentRetriever contentRetrieverWithVector = (AzureAiSearchContentRetriever) contentRetriever;
                    log.info("Testing Vector Search");
                    List<Content> relevant = contentRetrieverWithVector.retrieve(query);
                    assertThat(relevant).hasSizeGreaterThan(0);
                    assertThat(relevant.get(0).textSegment().text()).isEqualTo("The house is open");
                    log.info("#1 relevant item: {}", relevant.get(0).textSegment().text());
                });

        contextRunner
                .withPropertyValues(
                        Properties.PREFIX + ".content-retriever.api-key=" + AZURE_SEARCH_KEY,
                        Properties.PREFIX + ".content-retriever.endpoint=" + AZURE_SEARCH_ENDPOINT,
                        Properties.PREFIX + ".content-retriever.create-or-update-index=" + "false",
                        Properties.PREFIX + ".content-retriever.query-type=" + AzureAiSearchQueryType.FULL_TEXT
                )
                .run(context -> {
                    ContentRetriever contentRetriever = context.getBean(ContentRetriever.class);
                    assertThat(contentRetriever).isInstanceOf(AzureAiSearchContentRetriever.class);
                    AzureAiSearchContentRetriever contentRetrieverWithFullText = (AzureAiSearchContentRetriever) contentRetriever;
                    log.info("Testing Full Text Search");
                    // This uses the same storage as the vector search, so we don't need to add the content again
                    List<Content> relevant2 = contentRetrieverWithFullText.retrieve(query);
                    assertThat(relevant2).hasSizeGreaterThan(0);
                    assertThat(relevant2.get(0).textSegment().text()).isEqualTo("The house is open");
                    log.info("#1 relevant item: {}", relevant2.get(0).textSegment().text());
                });

        contextRunner
                .withPropertyValues(
                        Properties.PREFIX + ".content-retriever.api-key=" + AZURE_SEARCH_KEY,
                        Properties.PREFIX + ".content-retriever.endpoint=" + AZURE_SEARCH_ENDPOINT,
                        Properties.PREFIX + ".content-retriever.create-or-update-index=" + "false",
                        Properties.PREFIX + ".content-retriever.query-type=" + AzureAiSearchQueryType.HYBRID
                ).withBean(SearchIndex.class, () -> index)
                .withBean(EmbeddingModel.class, () -> embeddingModel)
                .run(context -> {
                    ContentRetriever contentRetriever = context.getBean(ContentRetriever.class);
                    assertThat(contentRetriever).isInstanceOf(AzureAiSearchContentRetriever.class);
                    AzureAiSearchContentRetriever contentRetrieverWithHybrid = (AzureAiSearchContentRetriever) contentRetriever;
                    log.info("Testing Hybrid Search");
                    List<Content> relevant3 = contentRetrieverWithHybrid.retrieve(query);
                    assertThat(relevant3).hasSizeGreaterThan(0);
                    assertThat(relevant3.get(0).textSegment().text()).isEqualTo("The house is open");
                    log.info("#1 relevant item: {}", relevant3.get(0).textSegment().text());
                });

        contextRunner
                .withPropertyValues(
                        Properties.PREFIX + ".content-retriever.api-key=" + AZURE_SEARCH_KEY,
                        Properties.PREFIX + ".content-retriever.endpoint=" + AZURE_SEARCH_ENDPOINT,
                        Properties.PREFIX + ".content-retriever.create-or-update-index=" + "false",
                        Properties.PREFIX + ".content-retriever.max-results=" + "3",
                        Properties.PREFIX + ".content-retriever.min-score=" + "0.4",
                        Properties.PREFIX + ".content-retriever.query-type=" + AzureAiSearchQueryType.HYBRID_WITH_RERANKING
                ).withBean(SearchIndex.class, () -> index)
                .withBean(EmbeddingModel.class, () -> embeddingModel)
                .run(context -> {
                    ContentRetriever contentRetriever = context.getBean(ContentRetriever.class);
                    assertThat(contentRetriever).isInstanceOf(AzureAiSearchContentRetriever.class);
                    AzureAiSearchContentRetriever contentRetrieverWithHybridAndReranking = (AzureAiSearchContentRetriever) contentRetriever;
                    log.info("Testing Hybrid Search with Reranking");
                    List<Content> relevant4 = contentRetrieverWithHybridAndReranking.retrieve(query);
                    assertThat(relevant4).hasSizeGreaterThan(0);
                    assertThat(relevant4.get(0).textSegment().text()).isEqualTo("The house is open");
                    log.info("#1 relevant item: {}", relevant4.get(0).textSegment().text());
                });
    }

    protected void awaitUntilPersisted() {
        try {
            Thread.sleep(1_000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    void should_provide_ai_search_embedding_store() {

        searchIndexClient.deleteIndex(INDEX_NAME);

        contextRunner
                .withPropertyValues(
                        Properties.PREFIX + ".embedding-store.api-key=" + AZURE_SEARCH_KEY,
                        Properties.PREFIX + ".embedding-store.endpoint=" + AZURE_SEARCH_ENDPOINT,
                        Properties.PREFIX + ".embedding-store.dimensions=" + 384,
                        Properties.PREFIX + ".embedding-store.create-or-update-index=" + "true"
                ).withBean(EmbeddingModel.class, () -> embeddingModel)
                .run(context -> {
                    EmbeddingStore embeddingStore = context.getBean(EmbeddingStore.class);
                    assertThat(embeddingStore).isInstanceOf(AzureAiSearchEmbeddingStore.class);
                    assertThat(context.getBean(AzureAiSearchEmbeddingStore.class)).isSameAs(embeddingStore);


                    String content1 = "banana";
                    String content2 = "computer";
                    String content3 = "apple";
                    String content4 = "pizza";
                    String content5 = "strawberry";
                    String content6 = "chess";
                    List<String> contents = asList(content1, content2, content3, content4, content5, content6);

                    for (String content : contents) {
                        TextSegment textSegment = TextSegment.from(content);
                        Embedding embedding = embeddingModel.embed(content).content();
                        embeddingStore.add(embedding, textSegment);
                    }
                    Embedding relevantEmbedding = embeddingModel.embed("fruit").content();
                    List<EmbeddingMatch<TextSegment>> relevant = embeddingStore.findRelevant(relevantEmbedding, 3);
                    assertThat(relevant).hasSize(3);
                    assertThat(relevant.get(0).embedding()).isNotNull();
                    assertThat(relevant.get(0).embedded().text()).isIn(content1, content3, content5);
                });
    }

}