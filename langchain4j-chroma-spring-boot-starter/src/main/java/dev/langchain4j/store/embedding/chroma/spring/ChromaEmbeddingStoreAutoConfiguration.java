package dev.langchain4j.store.embedding.chroma.spring;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.lang.Nullable;

import java.time.Duration;
import java.util.Optional;

import static dev.langchain4j.store.embedding.chroma.spring.ChromaEmbeddingStoreProperties.*;

@AutoConfiguration
@EnableConfigurationProperties(ChromaEmbeddingStoreProperties.class)
@ConditionalOnProperty(prefix = PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
public class ChromaEmbeddingStoreAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ChromaEmbeddingStore chromaEmbeddingStore(ChromaEmbeddingStoreProperties properties,
                                                      @Nullable EmbeddingModel embeddingModel) {
        String baseUrl = Optional.ofNullable(properties.getBaseUrl()).orElse(DEFAULT_BASE_URL);
        String collectionName = Optional.ofNullable(properties.getCollectionName()).orElse(DEFAULT_COLLECTION_NAME);

        ChromaEmbeddingStore.Builder builder = ChromaEmbeddingStore.builder()
                .baseUrl(baseUrl)
                .collectionName(collectionName)
                .apiVersion(properties.getApiVersion())
                .timeout(Duration.ofSeconds(5));

        if (properties.getTenantName() != null) {
            builder.tenantName(properties.getTenantName());
        }
        if (properties.getDatabaseName() != null) {
            builder.databaseName(properties.getDatabaseName());
        }

        return builder.build();
    }
}
