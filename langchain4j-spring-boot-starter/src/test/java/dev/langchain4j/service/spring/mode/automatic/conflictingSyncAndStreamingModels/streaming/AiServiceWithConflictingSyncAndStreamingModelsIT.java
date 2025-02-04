package dev.langchain4j.service.spring.mode.automatic.conflictingSyncAndStreamingModels.streaming;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.TestStreamingResponseHandler;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.service.spring.AiServicesAutoConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static dev.langchain4j.service.spring.mode.ApiKeys.OPENAI_API_KEY;
import static org.assertj.core.api.Assertions.assertThat;

class AiServiceWithConflictingSyncAndStreamingModelsIT {

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AiServicesAutoConfig.class));

    @Test
    void should_create_AI_service_with_streaming_model_when_both_sync_and_streaming_models_are_found_and_TokenStream_is_used() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.open-ai.streaming-chat-model.api-key=" + OPENAI_API_KEY,
                        "langchain4j.open-ai.streaming-chat-model.max-tokens=20",
                        "langchain4j.open-ai.streaming-chat-model.temperature=0.0"
                )
                .withUserConfiguration(AiServiceWithConflictingSyncAndStreamingModelsApplication.class)
                .run(context -> {

                    // given
                    AiServiceWithConflictingSyncAndStreamingModels aiService = context.getBean(AiServiceWithConflictingSyncAndStreamingModels.class);

                    TestStreamingResponseHandler<AiMessage> handler = new TestStreamingResponseHandler<>();

                    // when
                    aiService.chat("What is the capital of Germany?")
                            .onNext(handler::onNext)
                            .onComplete(handler::onComplete)
                            .onError(handler::onError)
                            .start();
                    Response<AiMessage> response = handler.get();

                    // then
                    assertThat(response.content().text()).containsIgnoringCase("Berlin");
                });
    }
}