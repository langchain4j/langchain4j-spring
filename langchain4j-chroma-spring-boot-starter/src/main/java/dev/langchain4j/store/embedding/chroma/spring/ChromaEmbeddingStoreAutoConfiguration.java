package dev.langchain4j.store.embedding.chroma.spring;

import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.Optional;

import static dev.langchain4j.store.embedding.chroma.spring.ChromaEmbeddingStoreProperties.*;

@AutoConfiguration
@EnableConfigurationProperties(ChromaEmbeddingStoreProperties.class)
@ConditionalOnProperty(prefix = PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
public class ChromaEmbeddingStoreAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ChromaEmbeddingStore chromaEmbeddingStore(ChromaEmbeddingStoreProperties properties) {

        ChromaEmbeddingStore.Builder builder = ChromaEmbeddingStore.builder()
                .baseUrl(Optional.ofNullable(properties.getBaseUrl()).orElse(DEFAULT_BASE_URL))
                .collectionName(Optional.ofNullable(properties.getCollectionName()).orElse(DEFAULT_COLLECTION_NAME))
                .timeout(Optional.ofNullable(properties.getTimeout()).orElse(DEFAULT_TIMEOUT));

        if (properties.getTenantName() != null) {
            builder.tenantName(properties.getTenantName());
        }
        if (properties.getDatabaseName() != null) {
            builder.databaseName(properties.getDatabaseName());
        }
        if (properties.getApiVersion() != null) {
            builder.apiVersion(properties.getApiVersion());
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
