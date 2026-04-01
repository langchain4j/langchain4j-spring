package dev.langchain4j.service.spring.mode.automatic.withChatMemoryPrototype;

import dev.langchain4j.service.spring.AiServicesAutoConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static dev.langchain4j.service.spring.mode.ApiKeys.OPENAI_API_KEY;
import static org.assertj.core.api.Assertions.assertThat;

class AiServiceWithChatMemoryPrototypeProviderIT {

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AiServicesAutoConfig.class));

    @Test
    void should_create_AI_services_with_separate_chat_memories() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.open-ai.chat-model.api-key=" + OPENAI_API_KEY,
                        "langchain4j.open-ai.chat-model.model-name=gpt-4o-mini",
                        "langchain4j.open-ai.chat-model.temperature=0.0",
                        "langchain4j.open-ai.chat-model.max-tokens=20"
                )
                .withUserConfiguration(AiServiceWithChatMemoryPrototypeApplication.class)
                .run(context -> {

                    // given
                    AiServiceWithChatMemoryPrototype1 aiService1 = context.getBean(AiServiceWithChatMemoryPrototype1.class);
                    aiService1.chat("My name is Klaus");
                    // when
                    String answer = aiService1.chat("What is my name?");
                    // then
                    assertThat(answer).containsIgnoringCase("Klaus");


                    // given
                    AiServiceWithChatMemoryPrototype2 aiService2 = context.getBean(AiServiceWithChatMemoryPrototype2.class);
                    // when
                    String answer2 = aiService2.chat("What is my name?");
                    // then
                    assertThat(answer2).doesNotContainIgnoringCase("Klaus");
                });
    }
}