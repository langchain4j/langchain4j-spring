package dev.langchain4j.store.embedding.spring;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2q.AllMiniLmL6V2QuantizedEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.ApplicationContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Percentage.withPercentage;

public abstract class EmbeddingStoreAutoConfigurationIT {

    protected abstract Class<?> autoConfigurationClass();

    protected abstract Class<? extends EmbeddingStore<TextSegment>> embeddingStoreClass();

    protected abstract String[] properties();

    /**
     * Property Key to configure {@link EmbeddingStore} dimension (if needed)
     *
     * @return {@link EmbeddingStore} dimension property key
     */
    protected abstract String dimensionPropertyKey();

    /**
     * Inherited IT can reuse this contextRunner for additional test.
     */
    protected ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(autoConfigurationClass()));

    @Test
    void should_provide_embedding_store_without_embedding_model() {
        // copy dimension property
        EmbeddingModel embeddingModel = new AllMiniLmL6V2QuantizedEmbeddingModel();
        String[] properties = new String[properties().length + 1];
        System.arraycopy(properties(), 0, properties, 0, properties().length);
        properties[properties.length - 1] = dimensionPropertyKey() + "=" + embeddingModel.dimension();

        contextRunner
                .withPropertyValues(properties)
                .run(context -> {
                    TextSegment segment = TextSegment.from("hello");
                    Embedding embedding = embeddingModel.embed(segment.text()).content();

                    assertThat(context.getBean(embeddingStoreClass())).isExactlyInstanceOf(embeddingStoreClass());
                    EmbeddingStore<TextSegment> embeddingStore = context.getBean(embeddingStoreClass());

                    String id = embeddingStore.add(embedding, segment);
                    assertThat(id).isNotBlank();

                    awaitUntilPersisted(context);

                    EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                            .queryEmbedding(embedding)
                            .maxResults(10)
                            .build();
                    List<EmbeddingMatch<TextSegment>> relevant = embeddingStore.search(searchRequest).matches();
                    assertThat(relevant).hasSize(1);

                    EmbeddingMatch<TextSegment> match = relevant.get(0);
                    assertThat(match.score()).isCloseTo(1, withPercentage(1));
                    assertThat(match.embeddingId()).isEqualTo(id);
                    assertThat(match.embedding()).isEqualTo(embedding);
                    assertThat(match.embedded()).isEqualTo(segment);
                });
    }

    @Test
    void should_provide_embedding_store_with_embedding_model() {
        contextRunner
                .withBean(AllMiniLmL6V2QuantizedEmbeddingModel.class)
                .withPropertyValues(properties())
                .run(context -> {
                    TextSegment segment = TextSegment.from("hello");
                    EmbeddingModel embeddingModel = context.getBean(AllMiniLmL6V2QuantizedEmbeddingModel.class);
                    Embedding embedding = embeddingModel.embed(segment.text()).content();

                    assertThat(context.getBean(embeddingStoreClass())).isExactlyInstanceOf(embeddingStoreClass());
                    EmbeddingStore<TextSegment> embeddingStore = context.getBean(embeddingStoreClass());

                    String id = embeddingStore.add(embedding, segment);
                    assertThat(id).isNotBlank();

                    awaitUntilPersisted(context);

                    EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                            .queryEmbedding(embedding)
                            .maxResults(10)
                            .build();

                    List<EmbeddingMatch<TextSegment>> matches = embeddingStore.search(searchRequest).matches();
                    assertThat(matches).hasSize(1);

                    EmbeddingMatch<TextSegment> match = matches.get(0);
                    assertThat(match.score()).isCloseTo(1, withPercentage(1));
                    assertThat(match.embeddingId()).isEqualTo(id);
                    assertThat(match.embedding()).isEqualTo(embedding);
                    assertThat(match.embedded()).isEqualTo(segment);
                });
    }

    protected void awaitUntilPersisted(ApplicationContext context) {

    }
}
