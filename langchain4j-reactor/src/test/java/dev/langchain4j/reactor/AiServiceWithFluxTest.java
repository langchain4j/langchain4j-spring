package dev.langchain4j.reactor;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.guardrail.OutputGuardrail;
import dev.langchain4j.guardrail.OutputGuardrailResult;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.mock.StreamingChatModelMock;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.PartialResponse;
import dev.langchain4j.model.chat.response.PartialResponseContext;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.chat.response.StreamingHandle;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.guardrail.OutputGuardrails;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class AiServiceWithFluxTest {

    interface Assistant {

        Flux<String> stream(String userMessage);

        @OutputGuardrails(OKGuardrail.class)
        Flux<String> streamWithOutputGuardrails(String userMessage);
    }

    @Test
    void should_stream() {

        // given
        List<String> tokens = List.of("H", "e", "l", "l", "o");

        StreamingChatModel model = StreamingChatModelMock.thatAlwaysStreams(tokens);

        Assistant assistant = AiServices.builder(Assistant.class)
                .streamingChatModel(model)
                .build();

        // when
        Flux<String> flux = assistant.stream("Hi");

        // then
        StepVerifier.create(flux)
                .expectNextSequence(tokens)
                .verifyComplete();
    }

    @Test
    void should_stream_with_output_guardrails() {

        // given
        List<String> tokens = List.of("H", "e", "l", "l", "o");

        StreamingChatModel model = StreamingChatModelMock.thatAlwaysStreams(tokens);

        Assistant assistant = AiServices.builder(Assistant.class)
                .streamingChatModel(model)
                .build();

        // when
        Flux<String> flux = assistant.streamWithOutputGuardrails("Hi");

        // then
        StepVerifier.create(flux)
                .expectNextSequence(tokens)
                .verifyComplete();
    }

    @Test
    void should_cancel_streaming_handle_when_flux_subscription_is_cancelled() {

        // given
        CancellableStreamingChatModel model = new CancellableStreamingChatModel();

        Assistant assistant = AiServices.builder(Assistant.class)
                .streamingChatModel(model)
                .build();

        // when
        Flux<String> flux = assistant.stream("Hi");

        // then
        StepVerifier.create(flux)
                .expectNext("hello")
                .thenCancel()
                .verify();

        assertThat(model.streamingHandle.isCancelled()).isTrue();
    }

    public static class OKGuardrail implements OutputGuardrail {

        @Override
        public OutputGuardrailResult validate(AiMessage responseFromLLM) {
            return success();
        }
    }

    private static class CancellableStreamingChatModel implements StreamingChatModel {

        private final TestStreamingHandle streamingHandle = new TestStreamingHandle();

        @Override
        public void doChat(ChatRequest chatRequest, StreamingChatResponseHandler handler) {
            handler.onPartialResponse(new PartialResponse("hello"), new PartialResponseContext(streamingHandle));
        }
    }

    private static class TestStreamingHandle implements StreamingHandle {

        private boolean cancelled;

        @Override
        public void cancel() {
            cancelled = true;
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }
    }
}
