package dev.langchain4j.service.spring.mode.automatic.scanPackages.package2;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.mock.ChatModelMock;
import dev.langchain4j.service.spring.AiServicesAutoConfiguration;
import dev.langchain4j.service.spring.mode.automatic.scanPackages.aiService.ScanPackageAiService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class ScanPackageAiServiceTest {

    ChatModelMock chatModel = ChatModelMock.thatAlwaysResponds("Berlin");

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AiServicesAutoConfiguration.class))
            .withBean(ChatModel.class, () -> chatModel);


    @Test
    void should_create_AI_service_that_use_scanPackageClass_value() {

        contextRunner
                .withUserConfiguration(ScanPackageAiServiceApplication.class)
                .run(context -> {

                    // given
                    ScanPackageAiService aiService = context.getBean(ScanPackageAiService.class);

                    // when
                    String answer = aiService.chat("What is the capital of Germany?");

                    // then
                    assertThat(answer).containsIgnoringCase("Berlin");
                });
    }

}