package dev.langchain4j.typesafe.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(prefix = TypeSafeProperties.PREFIX)
public class TypeSafeProperties {

    static final String PREFIX = "langchain4j.typesafe";

    @NestedConfigurationProperty
    TypeSafeDecisionModelProperties decisionModel;

    public TypeSafeDecisionModelProperties getDecisionModel() {
        return decisionModel;
    }

    public void setDecisionModel(TypeSafeDecisionModelProperties decisionModel) {
        this.decisionModel = decisionModel;
    }
}
