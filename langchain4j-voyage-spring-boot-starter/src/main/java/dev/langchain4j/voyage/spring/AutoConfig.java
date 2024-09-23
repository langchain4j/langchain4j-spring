package dev.langchain4j.voyage.spring;

import dev.langchain4j.model.voyage.VoyageEmbeddingModel;
import dev.langchain4j.model.voyage.VoyageScoringModel;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import static dev.langchain4j.voyage.spring.Properties.PREFIX;

@AutoConfiguration
@EnableConfigurationProperties(Properties.class)
public class AutoConfig {

    @Bean
    @ConditionalOnProperty(PREFIX + ".embedding-model.api-key")
    VoyageEmbeddingModel voyageEmbeddingModel(Properties properties) {
        EmbeddingModelProperties embeddingModelProperties = properties.getEmbeddingModel();
        return VoyageEmbeddingModel.builder()
                .baseUrl(embeddingModelProperties.getBaseUrl())
                .timeout(embeddingModelProperties.getTimeout())
                .maxRetries(embeddingModelProperties.getMaxRetries())
                .apiKey(embeddingModelProperties.getApiKey())
                .modelName(embeddingModelProperties.getModelName())
                .inputType(embeddingModelProperties.getInputType())
                .truncation(embeddingModelProperties.getTruncation())
                .encodingFormat(embeddingModelProperties.getEncodingFormat())
                .logRequests(embeddingModelProperties.getLogRequests())
                .logResponses(embeddingModelProperties.getLogResponses())
                .maxSegmentsPerBatch(embeddingModelProperties.getMaxSegmentsPerBatch())
                .build();
    }

    @Bean
    @ConditionalOnProperty(PREFIX + ".scoring-model.api-key")
    VoyageScoringModel voyageScoringModel(Properties properties) {
        ScoringModelProperties scoringModelProperties = properties.getScoringModel();
        return VoyageScoringModel.builder()
                .baseUrl(scoringModelProperties.getBaseUrl())
                .timeout(scoringModelProperties.getTimeout())
                .maxRetries(scoringModelProperties.getMaxRetries())
                .apiKey(scoringModelProperties.getApiKey())
                .modelName(scoringModelProperties.getModelName())
                .topK(scoringModelProperties.getTopK())
                .returnDocuments(scoringModelProperties.getReturnDocuments())
                .truncation(scoringModelProperties.getTruncation())
                .logRequests(scoringModelProperties.getLogRequests())
                .logResponses(scoringModelProperties.getLogResponses())
                .build();
    }
}
