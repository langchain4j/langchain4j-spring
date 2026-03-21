package dev.langchain4j.service.spring.mode.automatic.conflictingChatMemories;

import dev.langchain4j.service.IllegalConfigurationException;
import dev.langchain4j.service.spring.AiServicesAutoConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AiServiceWithConflictingChatMemoriesIT {

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AiServicesAutoConfig.class));

    @Test
    void should_fail_to_create_AI_service_when_conflicting_chat_memories_are_found() {
        contextRunner
                .withUserConfiguration(AiServiceWithConflictingChatModelsApplication.class)
                .run(context -> {

                    assertThatThrownBy(() -> context.getBean(AiServiceWithConflictingChatMemories.class))
                            .isExactlyInstanceOf(IllegalStateException.class)
                            .hasCauseExactlyInstanceOf(IllegalConfigurationException.class)
                            .hasRootCauseMessage("Conflict: multiple beans of type " +
                                    "dev.langchain4j.memory.ChatMemory are found: " +
                                    "[chatMemory, chatMemory2]. " +
                                    "Please specify which one you wish to wire in the @AiService annotation like this: " +
                                    "@AiService(wiringMode = EXPLICIT, chatMemory = \"<beanName>\").");
                });
    }
}