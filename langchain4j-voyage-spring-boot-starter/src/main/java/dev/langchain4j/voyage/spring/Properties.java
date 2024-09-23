package dev.langchain4j.voyage.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(prefix = Properties.PREFIX)
public class Properties {

    static final String PREFIX = "langchain4j.voyage";

    @NestedConfigurationProperty
    EmbeddingModelProperties embeddingModel;

    @NestedConfigurationProperty
    ScoringModelProperties scoringModel;

    public EmbeddingModelProperties getEmbeddingModel() {
        return embeddingModel;
    }

    public void setEmbeddingModel(EmbeddingModelProperties embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    public ScoringModelProperties getScoringModel() {
        return scoringModel;
    }

    public void setScoringModel(ScoringModelProperties scoringModel) {
        this.scoringModel = scoringModel;
    }
}
