package dev.langchain4j.reactor;

import static org.assertj.core.api.Assertions.assertThat;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatModelStreamingEvent;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.test.mock.MockChatModel;
import dev.langchain4j.test.mock.MockStreamingChatModel;
import dev.langchain4j.service.AiServiceStreamingEvent;
import dev.langchain4j.service.AiServices;
import java.util.List;
import java.util.concurrent.Flow;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * The Reactor bindings of the non-blocking AI Service modes: {@code Mono<T>} over the {@code CompletableFuture}
 * path, and {@code Flux<AiServiceStreamingEvent>} over the reactive publisher path.
 */
class AiServiceWithReactorNonBlockingTest {

    interface Assistant {

        Mono<String> answer(String userMessage);

        Flux<AiServiceStreamingEvent> events(String userMessage);
    }

    @Test
    void should_return_a_Mono_from_the_non_blocking_path() {
        ChatModel model = MockChatModel.thatResponds("Hello");

        Assistant assistant =
                AiServices.builder(Assistant.class).chatModel(model).build();

        StepVerifier.create(assistant.answer("Hi")).expectNext("Hello").verifyComplete();
    }

    @Test
    void should_stream_events_as_a_Flux() {
        MockStreamingChatModel model = MockStreamingChatModel.thatStreams("H", "e", "l", "l", "o");

        Assistant assistant = AiServices.builder(Assistant.class)
                .streamingChatModel(publisherOnly(model))
                .build();

        List<AiServiceStreamingEvent> events =
                assistant.events("Hi").collectList().block();

        assertThat(events).isNotEmpty();
        assertThat(events).last().isInstanceOf(AiServiceStreamingEvent.FinalResponseEvent.class);
        String text = events.stream()
                .filter(AiServiceStreamingEvent.PartialResponseEvent.class::isInstance)
                .map(e -> ((AiServiceStreamingEvent.PartialResponseEvent) e)
                        .partialResponse()
                        .text())
                .reduce("", String::concat);
        assertThat(text).isEqualTo("Hello");
        assertThat(model.requests()).hasSize(1);
    }

    @Test
    void Flux_of_String_still_uses_the_TokenStream_path_so_every_provider_keeps_working() throws Exception {
        // FluxPublisherAdapter deliberately does not claim Flux<String>: AiServices consults a PublisherAdapter
        // before a TokenStreamAdapter, so claiming it would move existing Flux<String> methods onto the
        // non-blocking path and break them on providers that have not implemented the reactive chat SPI.
        FluxPublisherAdapter adapter = new FluxPublisherAdapter();

        assertThat(adapter.canAdapt(Returns.class.getMethod("text").getGenericReturnType()))
                .isFalse();
        assertThat(adapter.canAdapt(Returns.class.getMethod("events").getGenericReturnType()))
                .isTrue();
    }

    /**
     * The given model with only the reactive publisher path: the handler path fails, so the test proves that a
     * {@code Flux<AiServiceStreamingEvent>} uses the publisher path. Which path is used is what this test checks, so
     * the wrapper is written here.
     */
    private static StreamingChatModel publisherOnly(StreamingChatModel model) {
        return new StreamingChatModel() {
            @Override
            public void doChat(ChatRequest chatRequest, StreamingChatResponseHandler handler) {
                throw new UnsupportedOperationException("only the publisher path may be used");
            }

            @Override
            public Flow.Publisher<ChatModelStreamingEvent> doChat(ChatRequest chatRequest) {
                return model.doChat(chatRequest);
            }
        };
    }

    interface Returns {
        Flux<String> text();

        Flux<AiServiceStreamingEvent> events();
    }
}