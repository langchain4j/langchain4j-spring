package dev.langchain4j.service.spring.customConfig.chatModel;

import dev.langchain4j.service.spring.AiServicesAutoConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class AssistantWithCustomChatModelIT {

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AiServicesAutoConfig.class));

    @Test
    void should_create_AI_service_with_custom_chat_model() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.open-ai.chat-model.api-key=banana" // to make sure that this model is not used
                )
                .withUserConfiguration(TestApplicationWithCustomChatModel.class)
                .run(context -> {

                    // given
                    AssistantWithCustomChatModel assistant = context.getBean(AssistantWithCustomChatModel.class);

                    // when
                    String answer = assistant.chat("What is the capital of Germany?");

                    // then
                    assertThat(answer).containsIgnoringCase("Berlin");
                });
    }
}