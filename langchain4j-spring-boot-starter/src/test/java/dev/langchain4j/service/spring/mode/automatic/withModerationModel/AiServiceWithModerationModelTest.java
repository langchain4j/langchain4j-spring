package dev.langchain4j.service.spring.mode.automatic.withModerationModel;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.test.mock.MockChatModel;
import dev.langchain4j.service.ModerationException;
import dev.langchain4j.service.spring.AiServicesAutoConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AiServiceWithModerationModelTest {

    MockChatModel chatModel = MockChatModel.thatResponds("Answer");

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AiServicesAutoConfig.class))
            .withBean(ChatModel.class, () -> chatModel);


    @Test
    void should_create_AI_service_with_moderation_model() {
        contextRunner
                .withUserConfiguration(AiServiceWithModerationModelApplication.class)
                .run(context -> {

                    // given
                    AiServiceWithModerationModel aiService = context.getBean(AiServiceWithModerationModel.class);

                    // when & then
                    assertThatThrownBy(() -> aiService.chat("I'm violating content policy"))
                            .isInstanceOf(ModerationException.class)
                            .hasMessageContaining("Flagged");

                });
    }
}
