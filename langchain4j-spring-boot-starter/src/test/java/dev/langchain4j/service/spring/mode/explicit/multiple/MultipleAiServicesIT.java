package dev.langchain4j.service.spring.mode.explicit.multiple;

import dev.langchain4j.service.spring.AiServicesAutoConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class MultipleAiServicesIT {

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AiServicesAutoConfig.class));

    @Test
    void should_create_AI_service_with_explicit_chat_model() {
        contextRunner
                .withUserConfiguration(MultipleAiServicesApplication.class)
                .run(context -> {

                    // MultipleAiServicesApplication.chatMemory() is wired automatically because wiringMode = AUTOMATIC
                    testWithMemory(context.getBean(FirstAiServiceWithAutomaticWiring.class));
                    testWithMemory(context.getBean(SecondAiServiceWithAutomaticWiring.class));

                    // MultipleAiServicesApplication.chatMemory() is NOT wired because wiringMode = EXPLICIT
                    testWithoutMemory(context.getBean(FirstAiServiceWithExplicitWiring.class));
                    testWithoutMemory(context.getBean(SecondAiServiceWithExplicitWiring.class));
                });
    }

    private static void testWithMemory(BaseAiService aiService) {

        // given
        aiService.chat("My name is Klaus");

        // when
        String answer = aiService.chat("What is my name?");

        // then
        assertThat(answer).containsIgnoringCase("Klaus");
    }

    private static void testWithoutMemory(BaseAiService aiService) {

        // given
        aiService.chat("My name is Klaus");

        // when
        String answer = aiService.chat("What is my name?");

        // then
        assertThat(answer).doesNotContainIgnoringCase("Klaus");
    }
}