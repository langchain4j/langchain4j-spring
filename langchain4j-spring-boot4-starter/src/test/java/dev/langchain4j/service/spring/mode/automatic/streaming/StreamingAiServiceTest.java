package dev.langchain4j.service.spring.mode.automatic.streaming;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.TestStreamingChatResponseHandler;
import dev.langchain4j.model.chat.mock.StreamingChatModelMock;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.spring.AiServicesAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class StreamingAiServiceTest {

    StreamingChatModelMock chatModel = StreamingChatModelMock.thatAlwaysStreams("Ber", "lin");

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AiServicesAutoConfiguration.class))
            .withBean(StreamingChatModel.class, () -> chatModel);


    @Test
    void should_create_streaming_AI_service() {
        contextRunner
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