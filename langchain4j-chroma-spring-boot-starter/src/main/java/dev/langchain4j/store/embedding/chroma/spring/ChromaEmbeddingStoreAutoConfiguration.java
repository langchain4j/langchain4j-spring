package dev.langchain4j.store.embedding.chroma.spring;

import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import static dev.langchain4j.store.embedding.chroma.spring.ChromaEmbeddingStoreProperties.PREFIX;

@AutoConfiguration
@EnableConfigurationProperties(ChromaEmbeddingStoreProperties.class)
@ConditionalOnProperty(prefix = PREFIX, name = "base-url")
public class ChromaEmbeddingStoreAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ChromaEmbeddingStore chromaEmbeddingStore(ChromaEmbeddingStoreProperties properties) {
        ChromaEmbeddingStore.Builder builder = ChromaEmbeddingStore.builder()
                .baseUrl(properties.getBaseUrl());

        if (properties.getCollectionName() != null) {
            builder.collectionName(properties.getCollectionName());
        }
        if (properties.getLogRequests() != null) {
            builder.logRequests(properties.getLogRequests());
        }
        if (properties.getLogResponses() != null) {
            builder.logResponses(properties.getLogResponses());
        }

        return builder.build();
    }
}
