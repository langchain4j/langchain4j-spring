package dev.langchain4j.service.spring.mode.automatic.withStructuredOutput;

import dev.langchain4j.service.spring.AiServicesAutoConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static dev.langchain4j.service.spring.mode.ApiKeys.OPENAI_API_KEY;
import static org.assertj.core.api.Assertions.assertThat;

class AiServiceWithStructuredOutputIT {

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AiServicesAutoConfig.class));

    @Test
    void should_create_AI_service_with_structured_output() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.open-ai.chat-model.api-key=" + OPENAI_API_KEY,
                        "langchain4j.open-ai.chat-model.model-name=gpt-4o-mini",
                        "langchain4j.open-ai.chat-model.supported-capabilities=[RESPONSE_FORMAT_JSON_SCHEMA]",
                        "langchain4j.open-ai.chat-model.strict-schema=true",
                        "langchain4j.open-ai.chat-model.temperature=0.0"
                )
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