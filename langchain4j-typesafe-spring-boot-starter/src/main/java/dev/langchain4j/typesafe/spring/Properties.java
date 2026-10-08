package dev.langchain4j.typesafe.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(prefix = Properties.PREFIX)
public class Properties {

    static final String PREFIX = "langchain4j.typesafe";

    @NestedConfigurationProperty
    DecisionModelProperties decisionModel;

    public DecisionModelProperties getDecisionModel() {
        return decisionModel;
    }

    public void setDecisionModel(DecisionModelProperties decisionModel) {
        this.decisionModel = decisionModel;
    }
}
