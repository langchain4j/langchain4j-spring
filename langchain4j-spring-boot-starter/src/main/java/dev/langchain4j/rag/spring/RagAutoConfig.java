package dev.langchain4j.rag.spring;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@EnableConfigurationProperties(RagProperties.class)
public class RagAutoConfig {

    // TODO make these beans lazy?

    @Bean
    @ConditionalOnMissingBean
    EmbeddingStore<TextSegment> embeddingStore() { // TODO bean name
        return new InMemoryEmbeddingStore<>();
    }

    @Bean
    @ConditionalOnBean({
            EmbeddingModel.class,
            EmbeddingStore.class
    })
    @ConditionalOnMissingBean
    ContentRetriever contentRetriever(EmbeddingModel embeddingModel,
                                      EmbeddingStore<TextSegment> embeddingStore,
                                      RagProperties ragProperties) {  // TODO bean name

        EmbeddingStoreContentRetriever.EmbeddingStoreContentRetrieverBuilder builder = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel);

        // TODO ragProperties can be null?
        RetrievalProperties retrievalProperties = ragProperties.getRetrieval();
        if (retrievalProperties != null) {
            builder
                    .maxResults(retrievalProperties.maxResults)
                    .minScore(retrievalProperties.minScore);
        }

        return builder.build();
    }

    // TODO test
}
