package dev.langchain4j.service.spring.mode.automatic.streaming;

import dev.langchain4j.model.chat.TestStreamingChatResponseHandler;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.spring.AiServicesAutoConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static dev.langchain4j.service.spring.mode.ApiKeys.OPENAI_API_KEY;
import static org.assertj.core.api.Assertions.assertThat;

class StreamingAiServiceIT {

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AiServicesAutoConfig.class));

    @Test
    void should_create_streaming_AI_service() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.open-ai.streaming-chat-model.api-key=" + OPENAI_API_KEY,
                        "langchain4j.open-ai.streaming-chat-model.model-name=gpt-4o-mini",
                        "langchain4j.open-ai.streaming-chat-model.max-tokens=20",
                        "langchain4j.open-ai.streaming-chat-model.temperature=0.0"
                )
                .withUserConfiguration(StreamingAiServiceApplication.class)
                .run(context -> {

                    // given
                    StreamingAiService aiService = context.getBean(StreamingAiService.class);

                    TestStreamingChatResponseHandler handler = new TestStreamingChatResponseHandler();

                    // when
                    aiService.chat("What is the capital of Germany?")
                            .onPartialResponse(handler::onPartialResponse)
                            .onCompleteResponse(handler::onCompleteResponse)
                            .onError(handler::onError)
                            .start();
                    ChatResponse response = handler.get();

                    // then
                    assertThat(response.aiMessage().text()).containsIgnoringCase("Berlin");
                });
    }
}