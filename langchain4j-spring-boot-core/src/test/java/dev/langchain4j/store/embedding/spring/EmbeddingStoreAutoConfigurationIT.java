package dev.langchain4j.store.embedding.spring;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.AllMiniLmL6V2QuantizedEmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Percentage.withPercentage;

public abstract class EmbeddingStoreAutoConfigurationIT {

    protected abstract Class<?> autoConfigurationClass();

    protected abstract Class<? extends EmbeddingStore<TextSegment>> embeddingStoreClass();

    protected abstract String[] properties();

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(autoConfigurationClass()));

    @Test
    void should_provide_embedding_store() {
        TextSegment segment = TextSegment.from("hello");
        contextRunner
                .withBean(AllMiniLmL6V2QuantizedEmbeddingModel.class)
                .withPropertyValues(properties())
                .run(context -> {
                    EmbeddingModel embeddingModel = context.getBean(AllMiniLmL6V2QuantizedEmbeddingModel.class);
                    Embedding embedding = embeddingModel.embed(segment.text()).content();

                    EmbeddingStore<TextSegment> embeddingStore = context.getBean(embeddingStoreClass());
                    assertThat(embeddingStore).isInstanceOf(embeddingStoreClass());

                    String id = embeddingStore.add(embedding, segment);
                    assertThat(id).isNotBlank();

                    awaitUntilPersisted();

                    List<EmbeddingMatch<TextSegment>> relevant = embeddingStore.findRelevant(embedding, 10);
                    assertThat(relevant).hasSize(1);

                    EmbeddingMatch<TextSegment> match = relevant.get(0);
                    assertThat(match.score()).isCloseTo(1, withPercentage(1));
                    assertThat(match.embeddingId()).isEqualTo(id);
                    assertThat(match.embedding()).isEqualTo(embedding);
                    assertThat(match.embedded()).isEqualTo(segment);
                });
    }

    protected abstract void awaitUntilPersisted();
}
