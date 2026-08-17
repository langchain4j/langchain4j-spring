package dev.langchain4j.service.spring.mode.automatic.missingAnnotation;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.mock.ChatModelMock;
import dev.langchain4j.service.spring.AiServicesAutoConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AiServiceWithMissingAnnotationTest {

    ChatModelMock chatModel = ChatModelMock.thatAlwaysResponds("Berlin");

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AiServicesAutoConfig.class))
            .withBean(ChatModel.class, () -> chatModel);

    @Test
    void should_fail_to_create_AI_service_with_missing_annotation() {
        contextRunner
                .withUserConfiguration(AiServiceWithMissingAnnotationApplication.class)
                .run(context -> {

                    // when-then
                    assertThatThrownBy(() -> context.getBean(AssistantWithMissingAnnotation.class))
                            .isExactlyInstanceOf(NoSuchBeanDefinitionException.class);
                });
    }
}