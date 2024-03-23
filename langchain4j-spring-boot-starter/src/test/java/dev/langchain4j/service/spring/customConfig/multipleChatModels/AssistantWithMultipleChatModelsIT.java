package dev.langchain4j.service.spring.customConfig.multipleChatModels;

import dev.langchain4j.exception.IllegalConfigurationException;
import dev.langchain4j.service.spring.AiServicesAutoConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AssistantWithMultipleChatModelsIT {

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AiServicesAutoConfig.class));

    @Test
    void should_fail_to_create_AI_service_when_multiple_chat_models_are_found() {
        contextRunner
                .withUserConfiguration(TestApplicationWithMultipleChatModels.class)
                .run(context -> {

                    assertThatThrownBy(() -> context.getBean(AssistantWithMultipleChatModels.class))
                            .isExactlyInstanceOf(IllegalStateException.class)
                            .hasCauseExactlyInstanceOf(IllegalConfigurationException.class)
                            .hasRootCauseMessage("Conflict: multiple beans of type " +
                                    "dev.langchain4j.model.chat.ChatLanguageModel are found: " +
                                    "[chatLanguageModel, chatLanguageModel2]. " +
                                    "Please specify which one you wish to use in the @AiService annotation " +
                                    "like this: @AiService(chatModel = \"<beanName>\").");
                });
    }
}