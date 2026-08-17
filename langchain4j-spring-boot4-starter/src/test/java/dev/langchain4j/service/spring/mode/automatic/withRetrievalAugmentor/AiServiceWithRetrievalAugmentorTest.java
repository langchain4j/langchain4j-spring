package dev.langchain4j.service.spring.mode.automatic.withRetrievalAugmentor;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.mock.ChatModelMock;
import dev.langchain4j.service.spring.AiServicesAutoConfiguration;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class AiServiceWithRetrievalAugmentorTest {

    ChatModelMock chatModel = ChatModelMock.thatAlwaysResponds("Klaus");

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AiServicesAutoConfiguration.class))
            .withBean(ChatModel.class, () -> chatModel);


    @Test
    void should_create_AI_service_with_retrieval_augmentor() {
        contextRunner
                .withUserConfiguration(AiServiceWithRetrievalAugmentorApplication.class)
                .run(context -> {

                    // given
                    AiServiceWithRetrievalAugmentor aiService = context.getBean(AiServiceWithRetrievalAugmentor.class);

                    // when
                    String answer = aiService.chat("What is my name?");

                    // then
                    assertThat(answer).containsIgnoringCase("Klaus");

                    // and the retrieved content was injected into the message sent to the model
                    assertThat(chatModel.requests().get(0).messages())
                            .last(InstanceOfAssertFactories.type(UserMessage.class))
                            .satisfies(message -> assertThat(message.singleText()).contains("My name is Klaus"));
                });
    }
}