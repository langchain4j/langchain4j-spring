package dev.langchain4j.service.spring.mode.explicit.multiple;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.mock.ChatModelMock;
import dev.langchain4j.service.spring.AiServicesAutoConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class MultipleAiServicesTest {

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AiServicesAutoConfig.class));


    @Test
    void should_create_AI_service_with_explicit_chat_model() {
        contextRunner
                .withUserConfiguration(MultipleAiServicesApplication.class)
                .run(context -> {

                    ChatModelMock chatModel = context.getBean(
                            MultipleAiServicesApplication.CHAT_MODEL_BEAN_NAME, ChatModelMock.class);

                    // MultipleAiServicesApplication.chatMemory() is wired automatically because wiringMode = AUTOMATIC
                    testWithMemory(chatModel, context.getBean(FirstAiServiceWithAutomaticWiring.class));
                    testWithMemory(chatModel, context.getBean(SecondAiServiceWithAutomaticWiring.class));

                    // MultipleAiServicesApplication.chatMemory() is NOT wired because wiringMode = EXPLICIT
                    testWithoutMemory(chatModel, context.getBean(FirstAiServiceWithExplicitWiring.class));
                    testWithoutMemory(chatModel, context.getBean(SecondAiServiceWithExplicitWiring.class));
                });
    }

    private static void testWithMemory(ChatModelMock chatModel, BaseAiService aiService) {

        // given
        int requestsBefore = chatModel.requests().size();
        aiService.chat("My name is Klaus");

        // when
        aiService.chat("What is my name?");

        // then the memory bean was wired, so the earlier message is replayed
        assertThat(chatModel.requests().get(requestsBefore + 1).messages())
                .contains(UserMessage.from("My name is Klaus"));
    }

    private static void testWithoutMemory(ChatModelMock chatModel, BaseAiService aiService) {

        // given
        int requestsBefore = chatModel.requests().size();
        aiService.chat("My name is Klaus");

        // when
        aiService.chat("What is my name?");

        // then no memory bean was wired, so only the current message is sent
        assertThat(chatModel.requests().get(requestsBefore + 1).messages())
                .containsExactly(UserMessage.from("What is my name?"));
    }
}