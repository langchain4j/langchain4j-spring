package dev.langchain4j.voyageai.spring;

import dev.langchain4j.model.voyageai.VoyageAiEmbeddingModel;
import dev.langchain4j.model.voyageai.VoyageAiScoringModel;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import static dev.langchain4j.voyageai.spring.Properties.PREFIX;

@AutoConfiguration
@EnableConfigurationProperties(Properties.class)
public class AutoConfig {

    @Bean
    @ConditionalOnProperty(PREFIX + ".embedding-model.api-key")
    VoyageAiEmbeddingModel voyageAiEmbeddingModel(Properties properties) {
        EmbeddingModelProperties embeddingModelProperties = properties.getEmbeddingModel();
        return VoyageAiEmbeddingModel.builder()
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
    VoyageAiScoringModel voyageAiScoringModel(Properties properties) {
        ScoringModelProperties scoringModelProperties = properties.getScoringModel();
        return VoyageAiScoringModel.builder()
                .baseUrl(scoringModelProperties.getBaseUrl())
                .timeout(scoringModelProperties.getTimeout())
                .maxRetries(scoringModelProperties.getMaxRetries())
                .apiKey(scoringModelProperties.getApiKey())
                .modelName(scoringModelProperties.getModelName())
                .topK(scoringModelProperties.getTopK())
                .truncation(scoringModelProperties.getTruncation())
                .logRequests(scoringModelProperties.getLogRequests())
                .logResponses(scoringModelProperties.getLogResponses())
                .build();
    }
}
