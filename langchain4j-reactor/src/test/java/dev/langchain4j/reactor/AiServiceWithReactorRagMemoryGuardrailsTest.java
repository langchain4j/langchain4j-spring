package dev.langchain4j.reactor;

import static org.assertj.core.api.Assertions.assertThat;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.exception.UnsupportedFeatureException;
import dev.langchain4j.guardrail.InputGuardrailException;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailRequest;
import dev.langchain4j.guardrail.InputGuardrailResult;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.service.guardrail.InputGuardrails;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.service.AiServices;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * RAG, chat memory and guardrails through the Reactor bindings. Each is covered both where it works and where it
 * refuses: on the non-blocking path a component that has not implemented its asynchronous counterpart fails loudly
 * rather than blocking a thread, and that is what a Spring user will actually meet.
 */
class AiServiceWithReactorRagMemoryGuardrailsTest {

    interface Assistant {
        Mono<String> answer(String userMessage);
    }

    /** Records what the model was asked, so a test can assert on memory and retrieved content. */
    static class RecordingModel implements ChatModel {
        final List<ChatRequest> requests = new CopyOnWriteArrayList<>();

        @Override
        public CompletableFuture<ChatResponse> doChatAsync(ChatRequest chatRequest) {
            return CompletableFuture.completedFuture(doChat(chatRequest));
        }

        @Override
        public ChatResponse doChat(ChatRequest chatRequest) {
            requests.add(chatRequest);
            return ChatResponse.builder().aiMessage(AiMessage.from("ok")).build();
        }
    }

    // ---------- chat memory ----------

    @Test
    void chat_memory_accumulates_across_Mono_calls() {
        RecordingModel model = new RecordingModel();
        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(model)
                // MessageWindowChatMemory implements addAsync/messagesAsync, so it works on the non-blocking path
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .build();

        assistant.answer("first").block();
        assistant.answer("second").block();

        // second request carries the first exchange: user, ai, user
        assertThat(model.requests).hasSize(2);
        assertThat(model.requests.get(0).messages()).hasSize(1);
        assertThat(model.requests.get(1).messages()).hasSize(3);
    }

    // ---------- RAG ----------

    @Test
    void a_retriever_that_implements_retrieveAsync_augments_a_Mono_call() {
        RecordingModel model = new RecordingModel();
        ContentRetriever asyncRetriever = new ContentRetriever() {

            @Override
            public CompletableFuture<List<Content>> retrieveAsync(Query query) {
                return CompletableFuture.completedFuture(List.of(Content.from(TextSegment.from("Munich is sunny"))));
            }

            @Override
            public List<Content> retrieve(Query query) {
                throw new UnsupportedOperationException("the async path must not call the blocking one");
            }
        };

        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(model)
                .contentRetriever(asyncRetriever)
                .build();

        StepVerifier.create(assistant.answer("weather?")).expectNext("ok").verifyComplete();
        assertThat(model.requests.get(0).messages().get(0).toString()).contains("Munich is sunny");
    }

    @Test
    void a_blocking_only_retriever_fails_loudly_unless_offloading_is_enabled() {
        ContentRetriever blockingOnly = query -> List.of(Content.from(TextSegment.from("Munich is sunny")));

        Assistant refuses = AiServices.builder(Assistant.class)
                .chatModel(new RecordingModel())
                .contentRetriever(blockingOnly)
                .build();

        StepVerifier.create(refuses.answer("weather?"))
                // the augmentor re-reports the marker as its parent type, adding guidance. Catching
                // UnsupportedFeatureException covers both flavours, which is why AsyncNotSupportedException
                // extends it.
                .expectErrorSatisfies(error -> assertThat(error)
                        .isInstanceOf(UnsupportedFeatureException.class)
                        .hasMessageContaining("retrieveAsync")
                        .hasMessageContaining("offloadBlocking(true)"))
                .verify();

        RecordingModel model = new RecordingModel();
        Assistant offloads = AiServices.builder(Assistant.class)
                .chatModel(model)
                .retrievalAugmentor(DefaultRetrievalAugmentor.builder()
                        .contentRetriever(blockingOnly)
                        .offloadBlocking(true)
                        .build())
                .build();

        StepVerifier.create(offloads.answer("weather?")).expectNext("ok").verifyComplete();
        assertThat(model.requests.get(0).messages().get(0).toString()).contains("Munich is sunny");
    }

    // ---------- guardrails ----------

    public static class AsyncGuardrail implements InputGuardrail {

        static final List<String> seen = new CopyOnWriteArrayList<>();

        @Override
        public CompletableFuture<InputGuardrailResult> validateAsync(InputGuardrailRequest request) {
            seen.add(request.userMessage().singleText());
            return CompletableFuture.completedFuture(success());
        }

        @Override
        public InputGuardrailResult validate(UserMessage userMessage) {
            throw new UnsupportedOperationException("the async path must not call the blocking one");
        }
    }

    public static class BlockingOnlyGuardrail implements InputGuardrail {

        @Override
        public InputGuardrailResult validate(UserMessage userMessage) {
            return success();
        }
    }

    interface AsyncGuarded {
        @InputGuardrails(AsyncGuardrail.class)
        Mono<String> answer(String userMessage);
    }

    interface BlockingGuarded {
        @InputGuardrails(BlockingOnlyGuardrail.class)
        Mono<String> answer(String userMessage);
    }

    @Test
    void a_guardrail_that_implements_validateAsync_runs_on_a_Mono_call() {
        AsyncGuardrail.seen.clear();
        AsyncGuarded assistant = AiServices.builder(AsyncGuarded.class)
                .chatModel(new RecordingModel())
                .build();

        StepVerifier.create(assistant.answer("hi")).expectNext("ok").verifyComplete();
        assertThat(AsyncGuardrail.seen).containsExactly("hi");
    }

    @Test
    void a_blocking_only_guardrail_fails_loudly_with_actionable_guidance() {
        BlockingGuarded assistant = AiServices.builder(BlockingGuarded.class)
                .chatModel(new RecordingModel())
                .build();

        StepVerifier.create(assistant.answer("hi"))
                .expectErrorSatisfies(error -> assertThat(error)
                        .isInstanceOf(InputGuardrailException.class)
                        .hasMessageContaining("does not implement validateAsync()")
                        .hasMessageContaining("override validateAsync()"))
                .verify();
    }
}
