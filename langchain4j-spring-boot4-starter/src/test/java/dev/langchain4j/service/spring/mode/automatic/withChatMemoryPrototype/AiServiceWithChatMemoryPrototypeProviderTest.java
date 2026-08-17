package dev.langchain4j.service.spring.mode.automatic.withChatMemoryPrototype;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.mock.ChatModelMock;
import dev.langchain4j.service.spring.AiServicesAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class AiServiceWithChatMemoryPrototypeProviderTest {

    ChatModelMock chatModel = ChatModelMock.thatAlwaysResponds("Your name is Klaus.");

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AiServicesAutoConfiguration.class))
            .withBean(ChatModel.class, () -> chatModel);

    @Test
    void should_create_AI_services_with_separate_chat_memories() {
        contextRunner
                .withUserConfiguration(AiServiceWithChatMemoryPrototypeApplication.class)
                .run(context -> {

                    // given
                    AiServiceWithChatMemoryPrototype1 aiService1 = context.getBean(AiServiceWithChatMemoryPrototype1.class);
                    aiService1.chat("My name is Klaus");
                    // when
                    aiService1.chat("What is my name?");
                    // then it has its own memory, so the earlier message is replayed
                    assertThat(chatModel.requests().get(1).messages())
                            .contains(UserMessage.from("My name is Klaus"));


                    // given
                    AiServiceWithChatMemoryPrototype2 aiService2 = context.getBean(AiServiceWithChatMemoryPrototype2.class);
                    // when
                    aiService2.chat("What is my name?");
                    // then it got a separate memory, so it knows nothing about the other conversation
                    assertThat(chatModel.requests().get(2).messages())
                            .containsExactly(UserMessage.from("What is my name?"));
                });
    }
}
