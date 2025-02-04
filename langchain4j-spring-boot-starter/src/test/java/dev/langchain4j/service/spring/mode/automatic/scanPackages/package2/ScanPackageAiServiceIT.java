package dev.langchain4j.service.spring.mode.automatic.scanPackages.package2;

import dev.langchain4j.service.spring.AiServicesAutoConfig;
import dev.langchain4j.service.spring.mode.automatic.scanPackages.aiService.ScanPackageAiService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static dev.langchain4j.service.spring.mode.ApiKeys.OPENAI_API_KEY;
import static org.assertj.core.api.Assertions.assertThat;

class ScanPackageAiServiceIT {

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AiServicesAutoConfig.class));

    @Test
    void should_create_AI_service_that_use_scanPackageClass_value() {

        contextRunner
                .withPropertyValues(
                        "langchain4j.open-ai.chat-model.api-key=" + OPENAI_API_KEY,
                        "langchain4j.open-ai.chat-model.max-tokens=20",
                        "langchain4j.open-ai.chat-model.temperature=0.0"
                )
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