package dev.langchain4j.service.spring.mode.automatic.conflictingSyncAndStreamingModels.sync;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.mock.ChatModelMock;
import dev.langchain4j.service.spring.AiServicesAutoConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class AiServiceWithConflictingSyncAndStreamingModelsTest {

    ChatModelMock chatModel = ChatModelMock.thatAlwaysResponds("Berlin");

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AiServicesAutoConfig.class))
            .withBean(ChatModel.class, () -> chatModel);


    @Test
    void should_create_AI_service_with_sync_model_when_both_sync_and_streaming_models_are_found_and_no_TokenStream_is_used() {
        contextRunner
                .withUserConfiguration(AiServiceWithConflictingSyncAndStreamingModelsApplication.class)
                .run(context -> {

                    // given
                    AiServiceWithConflictingSyncAndStreamingModels aiService = context.getBean(AiServiceWithConflictingSyncAndStreamingModels.class);

                    // when
                    String answer = aiService.chat("What is the capital of Germany?");

                    // then
                    assertThat(answer).containsIgnoringCase("Berlin");
                });
    }
}