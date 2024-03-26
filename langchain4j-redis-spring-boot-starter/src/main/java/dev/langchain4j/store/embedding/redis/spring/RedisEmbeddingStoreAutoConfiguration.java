package dev.langchain4j.store.embedding.redis.spring;

import dev.langchain4j.store.embedding.redis.RedisEmbeddingStore;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import static dev.langchain4j.store.embedding.redis.spring.RedisEmbeddingStoreProperties.PREFIX;

@AutoConfiguration
@EnableConfigurationProperties(RedisEmbeddingStoreProperties.class)
@ConditionalOnProperty(prefix = PREFIX, name = "enabled", havingValue = "true")
public class RedisEmbeddingStoreAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RedisEmbeddingStore redisEmbeddingStore(RedisEmbeddingStoreProperties properties) {
        return RedisEmbeddingStore.builder()
                .host(properties.getHost())
                .port(properties.getPort())
                .user(properties.getUser())
                .password(properties.getPassword())
                .indexName(properties.getIndexName())
                .dimension(properties.getDimension())
                .metadataFieldsName(properties.getMetadataKeys())
                .build();
    }
}
