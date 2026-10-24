package dev.langchain4j.typesafe.spring;

import dev.langchain4j.http.client.HttpClientBuilder;
import dev.langchain4j.model.decision.DecisionModel;
import dev.langchain4j.model.decision.listener.DecisionModelListener;
import dev.langchain4j.model.typesafe.TypeSafeDecisionModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;

import static dev.langchain4j.typesafe.spring.TypeSafeProperties.PREFIX;

@AutoConfiguration
@ConditionalOnClass(TypeSafeDecisionModel.class)
@EnableConfigurationProperties(TypeSafeProperties.class)
public class TypeSafeAutoConfiguration {

    static class TypeSafeDecisionModelCondition extends AnyNestedCondition {

        public TypeSafeDecisionModelCondition() {
            super(ConfigurationPhase.REGISTER_BEAN);
        }

        @ConditionalOnProperty(PREFIX + ".decision-model.api-key")
        static class ApiKeyCondition {}

        @ConditionalOnProperty(PREFIX + ".decision-model.base-url")
        static class BaseUrlCondition {}

        @ConditionalOnProperty(PREFIX + ".decision-model.model-name")
        static class ModelNameCondition {}
    }

    @Bean
    @Conditional(TypeSafeDecisionModelCondition.class)
    @ConditionalOnMissingBean(DecisionModel.class)
    TypeSafeDecisionModel typeSafeDecisionModel(
            TypeSafeProperties properties,
            ObjectProvider<HttpClientBuilder> httpClientBuilder,
            ObjectProvider<DecisionModelListener> listeners) {
        TypeSafeDecisionModelProperties modelProperties = properties.getDecisionModel();

        TypeSafeDecisionModel.TypeSafeDecisionModelBuilder builder = TypeSafeDecisionModel.builder()
                .baseUrl(modelProperties.getBaseUrl())
                .apiKey(modelProperties.getApiKey())
                .modelName(modelProperties.getModelName())
                .timeout(modelProperties.getTimeout())
                .maxRetries(modelProperties.getMaxRetries())
                .logRequests(modelProperties.getLogRequests())
                .logResponses(modelProperties.getLogResponses())
                .customHeaders(modelProperties.getCustomHeaders())
                .listeners(listeners.orderedStream().toList());

        httpClientBuilder.ifAvailable(builder::httpClientBuilder);

        return builder.build();
    }
}
