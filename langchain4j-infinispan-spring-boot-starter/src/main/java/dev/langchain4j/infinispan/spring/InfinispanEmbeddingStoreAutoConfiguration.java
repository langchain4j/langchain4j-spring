package dev.langchain4j.infinispan.spring;

import static dev.langchain4j.infinispan.spring.InfinispanEmbeddingStoreProperties.PREFIX;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.infinispan.InfinispanEmbeddingStore;
import java.util.Optional;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.impl.ConfigurationProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.lang.Nullable;

@AutoConfiguration
@EnableConfigurationProperties(InfinispanEmbeddingStoreProperties.class)
@ConditionalOnProperty(prefix = PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
public class InfinispanEmbeddingStoreAutoConfiguration {

    static final String DEFAULT_HOST = "localhost";
    static final String DEFAULT_CACHE_NAME = "langchain4j-cache";

    @Bean
    @ConditionalOnMissingBean
    public InfinispanEmbeddingStore infinispanEmbeddingStore(
            InfinispanEmbeddingStoreProperties properties, @Nullable EmbeddingModel embeddingModel) {

        String host = Optional.ofNullable(properties.getHost()).orElse(DEFAULT_HOST);
        int port = Optional.ofNullable(properties.getPort()).orElse(ConfigurationProperties.DEFAULT_HOTROD_PORT);
        String cacheName = Optional.ofNullable(properties.getCacheName()).orElse(DEFAULT_CACHE_NAME);
        Integer dimension = Optional.ofNullable(properties.getDimension())
                .orElseGet(() -> embeddingModel == null ? null : embeddingModel.dimension());

        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.addServer().host(host).port(port);
        if (properties.getUsername() != null && properties.getPassword() != null) {
            builder.security().authentication().username(properties.getUsername()).password(properties.getPassword());
        }

        InfinispanEmbeddingStore.Builder storeBuilder = InfinispanEmbeddingStore.builder()
                .cacheName(cacheName)
                .dimension(dimension)
                .infinispanConfigBuilder(builder);

        if (properties.getDistance() != null) {
            storeBuilder.distance(properties.getDistance());
        }
        if (properties.getSimilarity() != null) {
            storeBuilder.similarity(properties.getSimilarity());
        }
        if (properties.getCacheConfig() != null) {
            storeBuilder.cacheConfig(properties.getCacheConfig());
        }

        return storeBuilder.build();
    }
}
