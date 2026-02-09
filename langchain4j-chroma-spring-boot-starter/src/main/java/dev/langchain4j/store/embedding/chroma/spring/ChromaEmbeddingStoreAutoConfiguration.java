package dev.langchain4j.store.embedding.chroma.spring;

import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import java.util.Optional;
import java.time.Duration;
import static dev.langchain4j.store.embedding.chroma.spring.ChromaEmbeddingStoreProperties.*;

@AutoConfiguration
@ConditionalOnProperty(prefix = "langchain4j.chroma", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ChromaEmbeddingStoreAutoConfiguration {

    public ChromaEmbeddingStore chromaEmbeddingStore(ChromaEmbeddingStoreProperties properties) {
        String baseUrl = Optional.ofNullable(properties.getBaseUrl()).orElse(DEFAULT_BASE_URL);
        String collectionName = Optional.ofNullable(properties.getCollectionName()).orElse(DEFAULT_COLLECTION_NAME);
        Duration timeout = Optional.ofNullable(properties.getTimeout()).orElse(DEFAULT_TIMEOUT);
        
        return ChromaEmbeddingStore.builder()
                .baseUrl(baseUrl)
                .collectionName(collectionName)
                .timeout(timeout)
                .logRequests(properties.getLogRequests())
                .logResponses(properties.getLogResponses())
                .build();
    }
}