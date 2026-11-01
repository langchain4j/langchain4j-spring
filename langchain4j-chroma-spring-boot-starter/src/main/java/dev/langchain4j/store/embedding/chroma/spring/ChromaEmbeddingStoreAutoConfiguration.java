package dev.langchain4j.store.embedding.chroma.spring;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.chroma.ChromaApiVersion;
import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.Map;
import java.util.function.Supplier;


@AutoConfiguration
@EnableConfigurationProperties(ChromaEmbeddingStoreProperties.class)
@ConditionalOnProperty(prefix = ChromaEmbeddingStoreProperties.PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
public class ChromaEmbeddingStoreAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ChromaEmbeddingStoreAutoConfiguration.class);

    @Bean(name = "chromaEmbeddingStore")
    @ConditionalOnMissingBean
    public ChromaEmbeddingStore chromaEmbeddingStore(ChromaEmbeddingStoreProperties properties,
                                                     ObjectProvider<EmbeddingModel> embeddingModelProvider) {

        log.debug("Creating ChromaEmbeddingStore for collection [{}] at [{}]",
                properties.getCollectionName(), properties.getUrl());

        ChromaEmbeddingStore.Builder builder = ChromaEmbeddingStore.builder();

        
        if (properties.getApiVersion() != null && !properties.getApiVersion().isBlank()) {
            try {
                String normalized = properties.getApiVersion().trim().toUpperCase();
                
                builder.apiVersion(ChromaApiVersion.valueOf(normalized));
            } catch (IllegalArgumentException ex) {
                log.warn("Invalid Chroma apiVersion '{}', falling back to default", properties.getApiVersion());
            }
        }

        
        if (properties.getUrl() != null && !properties.getUrl().isBlank()) {
            builder.baseUrl(properties.getUrl());
        }

        
        if (properties.getTenantName() != null && !properties.getTenantName().isBlank()) {
            builder.tenantName(properties.getTenantName());
        }
        if (properties.getDatabaseName() != null && !properties.getDatabaseName().isBlank()) {
            builder.databaseName(properties.getDatabaseName());
        }

        
        if (properties.getCollectionName() != null && !properties.getCollectionName().isBlank()) {
            builder.collectionName(properties.getCollectionName());
        }

       
        if (properties.getLogRequests() != null) {
            builder.logRequests(properties.getLogRequests());
        }
        if (properties.getLogResponses() != null) {
            builder.logResponses(properties.getLogResponses());
        }

        
        if (properties.getApiKey() != null && !properties.getApiKey().isBlank()) {
            final String key = properties.getApiKey();
            Supplier<Map<String, String>> headersSupplier = () -> Map.of("Authorization", "Bearer " + key);
            builder.customHeaders(headersSupplier);
        }

        

        return builder.build();
    }
}