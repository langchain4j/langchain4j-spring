package dev.langchain4j.service.spring.mode.automatic.packagePrivateClass;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.mock.ChatModelMock;
import dev.langchain4j.service.spring.AiServicesAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class PackagePrivateAiServiceTest {

    ChatModelMock chatModel = ChatModelMock.thatAlwaysResponds("Berlin");

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AiServicesAutoConfiguration.class))
            .withBean(ChatModel.class, () -> chatModel);


    @Test
    void should_create_AI_service_that_is_package_private_interface() {
        contextRunner
                .withUserConfiguration(PackagePrivateAiServiceApplication.class)
                .run(context -> {

                    // given
                    PackagePrivateAiService aiService = context.getBean(PackagePrivateAiService.class);

                    // when
                    String answer = aiService.chat("What is the capital of Germany?");

                    // then
                    assertThat(answer).containsIgnoringCase("Berlin");
                });
    }
}