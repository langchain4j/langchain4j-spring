package dev.langchain4j.service.spring.mode.automatic.withStructuredOutput;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.test.mock.MockChatModel;
import dev.langchain4j.service.spring.AiServicesAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class AiServiceWithStructuredOutputTest {

    MockChatModel chatModel = MockChatModel.thatResponds("{\"name\":\"Klaus\",\"age\":42}");

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AiServicesAutoConfiguration.class))
            .withBean(ChatModel.class, () -> chatModel);


    @Test
    void should_create_AI_service_with_structured_output() {
        contextRunner
                .withUserConfiguration(AiServiceWithStructuredOutputApplication.class)
                .run(context -> {

                    // given
                    AiServiceWithStructuredOutput aiService = context.getBean(AiServiceWithStructuredOutput.class);

                    // when
                    Person person = aiService.extractPersonFrom("Klaus is 42 years old");

                    // then
                    assertThat(person).isEqualTo(new Person("Klaus", 42));
                });
    }
}