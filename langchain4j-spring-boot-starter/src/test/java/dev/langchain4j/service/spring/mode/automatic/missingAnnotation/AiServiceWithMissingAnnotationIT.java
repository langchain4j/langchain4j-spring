package dev.langchain4j.service.spring.mode.automatic.missingAnnotation;

import dev.langchain4j.service.spring.AiServicesAutoConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static dev.langchain4j.service.spring.mode.ApiKeys.OPENAI_API_KEY;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AiServiceWithMissingAnnotationIT {

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AiServicesAutoConfig.class));

    @Test
    void should_fail_to_create_AI_service_with_missing_annotation() {
        contextRunner
                .withPropertyValues("langchain4j.open-ai.chat-model.api-key=" + OPENAI_API_KEY)
                .withUserConfiguration(AiServiceWithMissingAnnotationApplication.class)
                .run(context -> {

                    // when-then
                    assertThatThrownBy(() -> context.getBean(AssistantWithMissingAnnotation.class))
                            .isExactlyInstanceOf(NoSuchBeanDefinitionException.class);
                });
    }
}