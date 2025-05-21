package dev.langchain4j.service.spring.mode.automatic.issue3074;

import dev.langchain4j.service.spring.AiServicesAutoConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

public class TestAutowrieToolProvider {

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AiServicesAutoConfig.class));

    @Test
    void should_fail_to_create_AI_service_when_conflicting_chat_models_are_found() {
        contextRunner
                .withUserConfiguration(TestAutowireAiServiceToolProviderApplication.class)
                .run(context -> {
                    Assertions.assertDoesNotThrow(() -> context.getBean(TestMcpToolProvider.class));
                });
    }
}
