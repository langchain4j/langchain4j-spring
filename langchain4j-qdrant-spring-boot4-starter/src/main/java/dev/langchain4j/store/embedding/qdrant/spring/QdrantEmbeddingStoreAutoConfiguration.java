package dev.langchain4j.store.embedding.qdrant.spring;

import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore;
import io.qdrant.client.QdrantClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.lang.Nullable;

import java.util.Optional;

import static dev.langchain4j.store.embedding.qdrant.spring.QdrantEmbeddingStoreProperties.*;

@AutoConfiguration
@EnableConfigurationProperties(QdrantEmbeddingStoreProperties.class)
@ConditionalOnProperty(prefix = PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
public class QdrantEmbeddingStoreAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public QdrantEmbeddingStore qdrantEmbeddingStore(QdrantEmbeddingStoreProperties properties,
                                                     @Nullable QdrantClient qdrantClient) {

        QdrantEmbeddingStore.Builder builder = QdrantEmbeddingStore.builder()
                .collectionName(properties.getCollectionName());

        if (qdrantClient != null) {
            return builder.client(qdrantClient).build();
        }

        String host = Optional.ofNullable(properties.getHost()).orElse(DEFAULT_HOST);
        int port = Optional.ofNullable(properties.getPort()).orElse(DEFAULT_PORT);
        boolean useTls = Optional.ofNullable(properties.getUseTls()).orElse(DEFAULT_USE_TLS);
        String payloadTextKey = Optional.ofNullable(properties.getPayloadTextKey()).orElse(DEFAULT_PAYLOAD_TEXT_KEY);
        String apiKey = Optional.ofNullable(properties.getApiKey())
                .orElse(System.getenv("QDRANT_API_KEY"));

        return builder
                .host(host)
                .port(port)
                .useTls(useTls)
                .payloadTextKey(payloadTextKey)
                .apiKey(apiKey)
                .build();
    }
}
