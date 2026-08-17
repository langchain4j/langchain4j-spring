package dev.langchain4j.service.spring.mode.automatic.withChatMemory;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.mock.ChatModelMock;
import dev.langchain4j.service.spring.AiServicesAutoConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class AiServiceWithChatMemoryTest {

    ChatModelMock chatModel = ChatModelMock.thatAlwaysResponds("Your name is Klaus.");

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AiServicesAutoConfig.class))
            .withBean(ChatModel.class, () -> chatModel);

    @Test
    void should_create_AI_service_with_chat_memory() {
        contextRunner
                .withUserConfiguration(AiServiceWithChatMemoryApplication.class)
                .run(context -> {

                    // given
                    AiServiceWithChatMemory aiService = context.getBean(AiServiceWithChatMemory.class);
                    aiService.chat("My name is Klaus");

                    // when
                    String answer = aiService.chat("What is my name?");

                    // then
                    assertThat(answer).containsIgnoringCase("Klaus");

                    // and the memory was wired, so the second request replays the whole conversation
                    assertThat(chatModel.requests().get(1).messages())
                            .containsExactly(
                                    UserMessage.from("My name is Klaus"),
                                    AiMessage.from("Your name is Klaus."),
                                    UserMessage.from("What is my name?"));
                });
    }
}
