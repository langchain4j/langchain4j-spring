package dev.langchain4j.service.spring.mode.explicit.tools;

import dev.langchain4j.service.spring.AiServicesAutoConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static dev.langchain4j.service.spring.mode.explicit.tools.Tools1.TOOL_1_TEMPERATURE;
import static dev.langchain4j.service.spring.mode.explicit.tools.Tools2.TOOL_2_TEMPERATURE;
import static org.assertj.core.api.Assertions.assertThat;

class AiServiceWithExplicitToolsIT {

    ApplicationContextRunner contextRunner = new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(AiServicesAutoConfig.class));

    @Test
    void should_create_AI_service_with_explicit_tools() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.open-ai.chat-model.api-key=" + System.getenv("OPENAI_API_KEY"),
                        "langchain4j.open-ai.chat-model.max-tokens=100",
                        "langchain4j.open-ai.chat-model.temperature=0.0")
                .withUserConfiguration(AiServiceWithExplicitToolsApplication.class)
                .run(context -> {

                    // given
                    AiServiceWithExplicitTools aiService = context.getBean(AiServiceWithExplicitTools.class);

                    // when
                    String answer = aiService.chat("What is the temperature?");

                    // then
                    assertThat(answer).contains(TOOL_1_TEMPERATURE).doesNotContain(TOOL_2_TEMPERATURE);
                });
    }
}