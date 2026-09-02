package dev.langchain4j.reactor;

import static org.assertj.core.api.Assertions.assertThat;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatModelStreamingEvent;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.CompleteResponse;
import dev.langchain4j.model.chat.response.PartialResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import java.util.ArrayList;
import java.util.concurrent.Flow;
import org.reactivestreams.FlowAdapters;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.mock.ChatModelMock;
import dev.langchain4j.service.AiServiceStreamingEvent;
import dev.langchain4j.service.AiServices;
import java.util.List;
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
        ChatModel model = ChatModelMock.thatAlwaysResponds("Hello");

        Assistant assistant =
                AiServices.builder(Assistant.class).chatModel(model).build();

        StepVerifier.create(assistant.answer("Hi")).expectNext("Hello").verifyComplete();
    }

    @Test
    void should_stream_events_as_a_Flux() {
        StreamingChatModel model = reactiveModel("H", "e", "l", "l", "o");

        Assistant assistant =
                AiServices.builder(Assistant.class).streamingChatModel(model).build();

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
     * A minimal {@link StreamingChatModel} that implements the reactive SPI, emitting one
     * {@code PartialResponse} per token and then the terminal {@code CompleteResponse}. The shared
     * {@code StreamingChatModelMock} only implements the handler-based API, so a non-blocking AI Service
     * correctly refuses it with {@code AsyncNotSupportedException}.
     */
    private static StreamingChatModel reactiveModel(String... tokens) {
        return new StreamingChatModel() {

            @Override
            public void doChat(ChatRequest chatRequest, StreamingChatResponseHandler handler) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Flow.Publisher<ChatModelStreamingEvent> doChat(ChatRequest chatRequest) {
                List<ChatModelStreamingEvent> events = new ArrayList<>();
                for (String token : tokens) {
                    events.add(new PartialResponse(token));
                }
                events.add(new CompleteResponse(ChatResponse.builder()
                        .aiMessage(AiMessage.from(String.join("", tokens)))
                        .build()));
                return FlowAdapters.toFlowPublisher(Flux.fromIterable(events));
            }
        };
    }

    interface Returns {
        Flux<String> text();

        Flux<AiServiceStreamingEvent> events();
    }
}