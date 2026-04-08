package dev.langchain4j.voyageai.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(prefix = VoyageAiProperties.PREFIX)
public class VoyageAiProperties {

    static final String PREFIX = "langchain4j.voyage-ai";

    @NestedConfigurationProperty
    VoyageAiEmbeddingModelProperties embeddingModel;

    @NestedConfigurationProperty
    VoyageAiScoringModelProperties scoringModel;

    public VoyageAiEmbeddingModelProperties getEmbeddingModel() {
        return embeddingModel;
    }

    public void setEmbeddingModel(VoyageAiEmbeddingModelProperties embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    public VoyageAiScoringModelProperties getScoringModel() {
        return scoringModel;
    }

    public void setScoringModel(VoyageAiScoringModelProperties scoringModel) {
        this.scoringModel = scoringModel;
    }
}
