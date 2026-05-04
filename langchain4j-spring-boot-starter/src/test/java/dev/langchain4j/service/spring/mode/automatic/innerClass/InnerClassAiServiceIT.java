package dev.langchain4j.service.spring.mode.automatic.innerClass;

import dev.langchain4j.service.spring.AiServicesAutoConfig;
import dev.langchain4j.service.spring.mode.automatic.innerClass.OuterClass.InnerAiService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class InnerClassAiServiceIT {

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AiServicesAutoConfig.class));

    @Test
    void should_create_AI_service_that_is_inner_class() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.open-ai.chat-model.api-key=" + System.getenv("OPENAI_API_KEY"),
                        "langchain4j.open-ai.chat-model.model-name=gpt-4o-mini",
                        "langchain4j.open-ai.chat-model.max-tokens=20",
                        "langchain4j.open-ai.chat-model.temperature=0.0"
                )
                .withUserConfiguration(InnerClassAiServiceApplication.class)
                .run(context -> {

                    // given
                    InnerAiService aiService = context.getBean(InnerAiService.class);

                    // when
                    String answer = aiService.chat("What is the capital of Germany?");

                    // then
                    assertThat(answer).containsIgnoringCase("Berlin");
                });
    }
}