package dev.langchain4j.service.spring.mode.automatic.Issue2133;

import dev.langchain4j.service.spring.AiServicesAutoConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class TestAutowireClassAiServiceIT {

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AiServicesAutoConfig.class));

    @Test
    void should_get_configuration_class() {
        contextRunner
                .withUserConfiguration(TestAutowireAiServiceApplication.class)
                .withBean(TestAutowireConfiguration.class)
                .run(context -> {
                    // given
                    TestAutowireAiServiceApplication application = context.getBean(TestAutowireAiServiceApplication.class);

                    // should get the configuration class
                    assertNotNull(application.getConfiguration(), "TestConfiguration class should be not null");
                });
    }
}