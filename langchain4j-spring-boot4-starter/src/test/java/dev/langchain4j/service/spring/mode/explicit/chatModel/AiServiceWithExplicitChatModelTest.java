package dev.langchain4j.service.spring.mode.explicit.chatModel;

import dev.langchain4j.service.spring.AiServicesAutoConfiguration;
import dev.langchain4j.service.spring.LogCapture;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class AiServiceWithExplicitChatModelTest {

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AiServicesAutoConfiguration.class));


    @Test
    void should_create_AI_service_with_explicit_chat_model() {
        contextRunner
                .withUserConfiguration(AiServiceWithExplicitChatModelApplication.class)
                .run(context -> {

                    // given
                    AiServiceWithExplicitChatModel aiService = context.getBean(AiServiceWithExplicitChatModel.class);

                    // when
                    String answer = aiService.chat("What is the capital of Germany?");

                    // then
                    assertThat(answer).containsIgnoringCase("Berlin");
                });
    }

    @Test
    void should_log_explicitly_wired_chat_model() {
        try (LogCapture logs = LogCapture.debug(AiServicesAutoConfiguration.class)) {
            contextRunner
                    .withUserConfiguration(AiServiceWithExplicitChatModelApplication.class)
                    .run(context -> assertThat(logs.messages())
                            .contains("Registered @AiService bean 'aiServiceWithExplicitChatModel' for "
                                    + AiServiceWithExplicitChatModel.class.getName() + " using EXPLICIT wiring mode")
                            .contains("chatModel=myChatModel,")
                            .contains("tools=[]"));
        }
    }
}
