package dev.langchain4j.service.spring.mode.automatic.withTools;

import dev.langchain4j.service.spring.AiServicesAutoConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.time.LocalDateTime;

import static dev.langchain4j.service.spring.mode.ApiKeys.OPENAI_API_KEY;
import static dev.langchain4j.service.spring.mode.automatic.withTools.PublicTools.CURRENT_TEMPERATURE;
import static org.assertj.core.api.Assertions.assertThat;

class AiServicesAutoConfigIT {

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AiServicesAutoConfig.class));

    @Test
    void should_create_AI_service_with_tool_which_is_public_method_in_public_class() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.open-ai.chat-model.api-key=" + OPENAI_API_KEY,
                        "langchain4j.open-ai.chat-model.max-tokens=20",
                        "langchain4j.open-ai.chat-model.temperature=0.0",
                        "langchain4j.open-ai.chat-model.log-requests=true",
                        "langchain4j.open-ai.chat-model.log-responses=true"
                )
                .withUserConfiguration(AiServiceWithToolsApplication.class)
                .run(context -> {

                    // given
                    AiServiceWithTools aiService = context.getBean(AiServiceWithTools.class);

                    // when
                    String answer = aiService.chat("What is the current temperature?");

                    // then should use PublicTools.getCurrentTemperature()
                    assertThat(answer).contains(String.valueOf(CURRENT_TEMPERATURE));
                });
    }

    @Test
    void should_create_AI_service_with_tool_that_is_package_private_method_in_package_private_class() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.open-ai.chat-model.api-key=" + OPENAI_API_KEY,
                        "langchain4j.open-ai.chat-model.max-tokens=20",
                        "langchain4j.open-ai.chat-model.temperature=0.0"
                )
                .withUserConfiguration(AiServiceWithToolsApplication.class)
                .run(context -> {

                    // given
                    AiServiceWithTools aiService = context.getBean(AiServiceWithTools.class);

                    // when
                    String answer = aiService.chat("What is the current minute?");

                    // then should use PackagePrivateTools.getCurrentMinute()
                    assertThat(answer).contains(String.valueOf(LocalDateTime.now().getMinute()));
                });
    }

    // TODO tools which are not @Beans?
    // TODO negative cases
    // TODO no @AiServices in app, just models
    // TODO @AiServices as inner class?
    // TODO streaming, memory, tools, etc
}