package dev.langchain4j.reactor;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.AiServices;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Tools, cancellation and error propagation through the Reactor bindings of the non-blocking AI Service modes.
 * These cover the parts of {@link MonoCompletableFutureAdapter} that the adapter's own javadoc claims but the
 * type-level tests do not reach: a {@code Mono}-returning {@code @Tool}, and cancellation reaching the future.
 */
class AiServiceWithReactorToolsTest {

    interface Assistant {
        Mono<String> answer(String userMessage);
    }

    static class SyncTools {
        final List<String> invoked = new CopyOnWriteArrayList<>();

        @Tool("Returns the weather in a given city")
        String getWeather(String city) {
            invoked.add(city);
            return "sunny";
        }
    }

    static class MonoTools {
        final List<String> invoked = new CopyOnWriteArrayList<>();

        @Tool("Returns the weather in a given city")
        Mono<String> getWeather(String city) {
            invoked.add(city);
            return Mono.just("sunny");
        }
    }

    @Test
    void a_blocking_tool_runs_during_a_Mono_returning_ai_service_call() {
        SyncTools tools = new SyncTools();
        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(toolThenAnswer())
                .tools(tools)
                .build();

        StepVerifier.create(assistant.answer("What is the weather in Munich?"))
                .expectNext("It is sunny")
                .verifyComplete();
        assertThat(tools.invoked).containsExactly("Munich");
    }

    @Test
    void a_Mono_returning_tool_runs_during_a_Mono_returning_ai_service_call() {
        // the claim MonoCompletableFutureAdapter.toCompletableFuture makes: a @Tool may return a Mono
        MonoTools tools = new MonoTools();
        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(toolThenAnswer())
                .tools(tools)
                .build();

        StepVerifier.create(assistant.answer("What is the weather in Munich?"))
                .expectNext("It is sunny")
                .verifyComplete();
        assertThat(tools.invoked).containsExactly("Munich");
    }

    @Test
    void a_model_failure_surfaces_as_a_Mono_error() {
        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(new ChatModel() {
                    @Override
                    public java.util.concurrent.CompletableFuture<ChatResponse> doChatAsync(ChatRequest chatRequest) {
                        return java.util.concurrent.CompletableFuture.failedFuture(new IllegalStateException("boom"));
                    }

                    @Override
                    public ChatResponse doChat(ChatRequest chatRequest) {
                        throw new IllegalStateException("boom");
                    }
                })
                .build();

        StepVerifier.create(assistant.answer("hi"))
                // the model's failure reaches the subscriber unwrapped, not buried in a CompletionException
                .expectErrorSatisfies(error -> assertThat(error)
                        .isInstanceOf(IllegalStateException.class)
                        .hasMessage("boom"))
                .verify(Duration.ofSeconds(10));
    }

    @Test
    void cancelling_the_Mono_cancels_the_underlying_future() {
        java.util.concurrent.CompletableFuture<String> future = new java.util.concurrent.CompletableFuture<>();

        Mono<?> mono = (Mono<?>) new MonoCompletableFutureAdapter()
                .fromCompletableFuture(null, future);

        mono.subscribe().dispose();

        assertThat(future).isCancelled();
    }

    @Test
    void a_Mono_tool_value_is_normalized_to_a_CompletableFuture() throws Exception {
        Object value = new MonoCompletableFutureAdapter()
                .toCompletableFuture(Mono.just("sunny"))
                .get(5, SECONDS);

        assertThat(value).isEqualTo("sunny");
    }

    /** Answers the first request with a tool call, the second with the final text. */
    private static ChatModel toolThenAnswer() {
        AtomicInteger calls = new AtomicInteger();
        return new ChatModel() {
            @Override
            public java.util.concurrent.CompletableFuture<ChatResponse> doChatAsync(ChatRequest chatRequest) {
                return java.util.concurrent.CompletableFuture.completedFuture(doChat(chatRequest));
            }

            @Override
            public ChatResponse doChat(ChatRequest chatRequest) {
                if (calls.getAndIncrement() == 0) {
                    return ChatResponse.builder()
                            .aiMessage(AiMessage.from(ToolExecutionRequest.builder()
                                    .id("1")
                                    .name("getWeather")
                                    .arguments("{\"arg0\": \"Munich\"}")
                                    .build()))
                            .build();
                }
                return ChatResponse.builder()
                        .aiMessage(AiMessage.from("It is sunny"))
                        .build();
            }
        };
    }
}
