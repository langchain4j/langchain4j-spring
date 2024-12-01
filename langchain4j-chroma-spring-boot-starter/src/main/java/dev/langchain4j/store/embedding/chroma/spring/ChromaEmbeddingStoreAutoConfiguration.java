package dev.langchain4j.store.embedding.chroma.spring;

import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.time.Duration;
import java.util.Optional;

import static dev.langchain4j.store.embedding.chroma.spring.ChromaEmbeddingStoreProperties.*;

@AutoConfiguration
@EnableConfigurationProperties(ChromaEmbeddingStoreProperties.class)
@ConditionalOnProperty(prefix = PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
public class ChromaEmbeddingStoreAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ChromaEmbeddingStore chromaEmbeddingStore(ChromaEmbeddingStoreProperties properties) {
        String baseUrl = Optional.ofNullable(properties.getBaseUrl()).orElse(DEFAULT_BASE_URL);
        String collectionName = Optional.ofNullable(properties.getCollectionName()).orElse(DEFAULT_COLLECTION_NAME);
        Boolean logRequests = Optional.ofNullable(properties.getLogRequests()).orElse(false);
        Boolean logResponses = Optional.ofNullable(properties.getLogResponses()).orElse(false);

        ChromaEmbeddingStore.Builder builder = ChromaEmbeddingStore.builder()
                .baseUrl(baseUrl)
                .collectionName(collectionName)
                .logRequests(logRequests)
                .logResponses(logResponses);

        if(properties.getTimeoutInSeconds() != null) {
            Duration timeoutInSeconds = Duration.ofSeconds(properties.getTimeoutInSeconds());
            builder.timeout(timeoutInSeconds);
        }

        return builder.build();
    }
}
