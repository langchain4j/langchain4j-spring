package dev.langchain4j.service.spring.mode.explicit.chatModel;

import dev.langchain4j.service.spring.AiServicesAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(OutputCaptureExtension.class)
class AiServiceWithExplicitChatModelTest {

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AiServicesAutoConfiguration.class));


    @Test
    void should_create_AI_service_with_explicit_chat_model() {
        contextRunner
                .withUserConfiguration(AiServiceWithExplicitChatModelApplication.class)
                .run(context -> {

                    // given
                    AiServiceWithExplicitChatModel aiService = context.getBean(AiServiceWithExplicitChatModel.class);

                    // when
                    String answer = aiService.chat("What is the capital of Germany?");

                    // then
                    assertThat(answer).containsIgnoringCase("Berlin");
                });
    }

    @Test
    void should_log_explicitly_wired_chat_model(CapturedOutput output) {
        withDebugLogging(() -> contextRunner
                .withUserConfiguration(AiServiceWithExplicitChatModelApplication.class)
                .run(context -> assertThat(output)
                        .contains("Registered @AiService bean 'aiServiceWithExplicitChatModel' for "
                                + AiServiceWithExplicitChatModel.class.getName() + " using EXPLICIT wiring mode")
                        .contains("chatModel=myChatModel,")
                        .contains("tools=[]")));
    }

    private static void withDebugLogging(Runnable runnable) {
        LoggingSystem loggingSystem = LoggingSystem.get(AiServicesAutoConfiguration.class.getClassLoader());
        loggingSystem.setLogLevel(AiServicesAutoConfiguration.class.getName(), LogLevel.DEBUG);
        try {
            runnable.run();
        } finally {
            loggingSystem.setLogLevel(AiServicesAutoConfiguration.class.getName(), null);
        }
    }
}
