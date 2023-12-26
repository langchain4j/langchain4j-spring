package dev.langchain4j.store.embedding.redis.spring;

import dev.langchain4j.store.embedding.redis.RedisEmbeddingStore;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import static dev.langchain4j.store.embedding.redis.spring.RedisProperties.PREFIX;

@AutoConfiguration
@EnableConfigurationProperties(RedisProperties.class)
public class RedisEmbeddingStoreAutoConfiguration {

    @Bean
    @ConditionalOnProperty(PREFIX + ".enabled")
    public RedisEmbeddingStore redisEmbeddingStore(RedisProperties properties) {
        return RedisEmbeddingStore.builder()
                .host(properties.getHost())
                .port(properties.getPort())
                .user(properties.getUser())
                .password(properties.getPassword())
                .indexName(properties.getIndexName())
                .dimension(properties.getDimension())
                .metadataFieldsName(properties.getMetadataFieldsName())
                .build();
    }
}
