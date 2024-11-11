package dev.langchain4j.service.spring.mode.automatic.withModerationModel;

import dev.langchain4j.service.ModerationException;
import dev.langchain4j.service.spring.AiServicesAutoConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static dev.langchain4j.service.spring.mode.ApiKeys.OPENAI_API_KEY;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AiServiceWithModerationModelIT {

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AiServicesAutoConfig.class));

    @Test
    void should_create_AI_service_with_moderation_model() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.open-ai.chat-model.api-key=" + OPENAI_API_KEY,
                        "langchain4j.open-ai.chat-model.max-tokens=20",
                        "langchain4j.open-ai.chat-model.temperature=0.0"
                )
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
