package dev.langchain4j.service.spring.mode.automatic.differentPackage.package1;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.mock.ChatModelMock;
import dev.langchain4j.service.spring.AiServicesAutoConfig;
import dev.langchain4j.service.spring.mode.automatic.differentPackage.package2.DifferentPackageAiService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.ComponentScan;

import static org.assertj.core.api.Assertions.assertThat;

class DifferentPackageAiServiceTest {

    ChatModelMock chatModel = ChatModelMock.thatAlwaysResponds("Berlin");

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AiServicesAutoConfig.class))
            .withBean(ChatModel.class, () -> chatModel);


    @ComponentScan(value = "dev.langchain4j.service.spring.mode.automatic.differentPackage.package2")
    static class ComponentScanWithValue {
    }

    @ComponentScan(basePackages = "dev.langchain4j.service.spring.mode.automatic.differentPackage.package2")
    static class ComponentScanWithBasePackages {
    }

    @ComponentScan(basePackageClasses = dev.langchain4j.service.spring.mode.automatic.differentPackage.package2.DifferentPackageAiService.class)
    static class ComponentScanWithBasePackageClasses {
    }

    @ComponentScan(basePackages = "${test.basePackages}")
    static class ComponentScanWithPlaceholder {
    }

    @Test
    void should_create_AI_service_that_use_componentScan_value() {

        contextRunner
                .withUserConfiguration(DifferentPackageAiServiceApplication.class)
                .withUserConfiguration(ComponentScanWithValue.class)
                .run(context -> {

                    // given
                    DifferentPackageAiService aiService = context.getBean(DifferentPackageAiService.class);

                    // when
                    String answer = aiService.chat("What is the capital of Germany?");

                    // then
                    assertThat(answer).containsIgnoringCase("Berlin");
                });
    }

    @Test
    void should_create_AI_service_that_use_componentScan_basePackages() {

        contextRunner
                .withUserConfiguration(DifferentPackageAiServiceApplication.class)
                .withUserConfiguration(ComponentScanWithBasePackages.class)
                .run(context -> {

                    // given
                    DifferentPackageAiService aiService = context.getBean(DifferentPackageAiService.class);

                    // when
                    String answer = aiService.chat("What is the capital of Germany?");

                    // then
                    assertThat(answer).containsIgnoringCase("Berlin");
                });
    }

    @Test
    void should_create_AI_service_that_use_componentScan_basePackageClasses() {

        contextRunner
                .withUserConfiguration(DifferentPackageAiServiceApplication.class)
                .withUserConfiguration(ComponentScanWithBasePackageClasses.class)
                .run(context -> {

                    // given
                    DifferentPackageAiService aiService = context.getBean(DifferentPackageAiService.class);

                    // when
                    String answer = aiService.chat("What is the capital of Germany?");

                    // then
                    assertThat(answer).containsIgnoringCase("Berlin");
                });
    }

    @Test
    void should_create_AI_service_that_use_componentScan_with_placeholder() {

        contextRunner
                .withPropertyValues(
                        "test.basePackages=dev.langchain4j.service.spring.mode.automatic.differentPackage.package2")
                .withUserConfiguration(DifferentPackageAiServiceApplication.class)
                .withUserConfiguration(ComponentScanWithPlaceholder.class)
                .run(context -> {

                    // given
                    DifferentPackageAiService aiService = context.getBean(DifferentPackageAiService.class);

                    // when
                    String answer = aiService.chat("What is the capital of Germany?");

                    // then
                    assertThat(answer).containsIgnoringCase("Berlin");
                });
    }
}