package dev.langchain4j.service.spring.mode.automatic.publicClass;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.mock.ChatModelMock;
import dev.langchain4j.service.spring.AiServicesAutoConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class PublicAiServiceTest {

    ChatModelMock chatModel = ChatModelMock.thatAlwaysResponds("Berlin");

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AiServicesAutoConfig.class))
            .withBean(ChatModel.class, () -> chatModel);


    @Test
    void should_create_AI_service_that_is_public_interface() {
        contextRunner
                .withUserConfiguration(PublicAiServiceApplication.class)
                .run(context -> {

                    // given
                    PublicAiService aiService = context.getBean(PublicAiService.class);

                    // when
                    String answer = aiService.chat("What is the capital of Germany?");

                    // then
                    assertThat(answer).containsIgnoringCase("Berlin");
                });
    }
}