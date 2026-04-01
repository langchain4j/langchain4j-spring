package dev.langchain4j.service.spring.mode.explicit.chatModel;

import dev.langchain4j.service.spring.AiServicesAutoConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class AiServiceWithExplicitChatModelIT {

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AiServicesAutoConfig.class));

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
}