package dev.langchain4j.reactor;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingHandle;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.ToolExecution;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

class TokenStreamToFluxAdapterTest {

    interface Assistant {

        Flux<String> fluxOfString();

        Flux flux();

        Flux<Object> fluxOfObject();
    }

    @Test
    void test_canAdapt() {

        TokenStreamToFluxAdapter adapter = new TokenStreamToFluxAdapter();

        assertThat(adapter.canAdaptTokenStreamTo(getReturnTypeOfMethod("fluxOfString"))).isTrue();

        assertThat(adapter.canAdaptTokenStreamTo(getReturnTypeOfMethod("flux"))).isFalse();
        assertThat(adapter.canAdaptTokenStreamTo(getReturnTypeOfMethod("fluxOfObject"))).isFalse();
    }

    @Test
    void should_cancel_streaming_handle_when_flux_subscription_is_cancelled() {

        TestTokenStream tokenStream = new TestTokenStream();

        @SuppressWarnings("unchecked")
        Flux<String> flux = (Flux<String>) new TokenStreamToFluxAdapter().adapt(tokenStream);

        tokenStream.emitStreamingHandle();
        tokenStream.emitPartialResponse("hello");

        StepVerifier.create(flux)
                .expectNext("hello")
                .thenCancel()
                .verify();

        assertThat(tokenStream.streamingHandle.isCancelled()).isTrue();
        assertThat(tokenStream.streamingHandle.cancellationAttempts()).isOne();

        tokenStream.emitStreamingHandle();

        assertThat(tokenStream.streamingHandle.cancellationAttempts()).isOne();
    }

    @Test
    void should_cancel_streaming_handle_when_handle_is_received_after_flux_subscription_is_cancelled() {

        TestTokenStream tokenStream = new TestTokenStream();

        @SuppressWarnings("unchecked")
        Flux<String> flux = (Flux<String>) new TokenStreamToFluxAdapter().adapt(tokenStream);

        StepVerifier.create(flux)
                .thenCancel()
                .verify();

        tokenStream.emitStreamingHandle();

        assertThat(tokenStream.streamingHandle.isCancelled()).isTrue();
    }

    @Test
    void should_not_cancel_streaming_handle_when_flux_completes_normally() {

        TestTokenStream tokenStream = new TestTokenStream();

        @SuppressWarnings("unchecked")
        Flux<String> flux = (Flux<String>) new TokenStreamToFluxAdapter().adapt(tokenStream);

        tokenStream.emitStreamingHandle();
        tokenStream.emitPartialResponse("hello");
        tokenStream.emitCompleteResponse();

        StepVerifier.create(flux)
                .expectNext("hello")
                .verifyComplete();

        assertThat(tokenStream.streamingHandle.isCancelled()).isFalse();
        assertThat(tokenStream.streamingHandle.cancellationAttempts()).isZero();
    }

    @Test
    void should_ignore_runtime_exception_when_cancelling_streaming_handle() {

        ThrowingStreamingHandle streamingHandle = new ThrowingStreamingHandle();
        TestTokenStream tokenStream = new TestTokenStream(streamingHandle);

        @SuppressWarnings("unchecked")
        Flux<String> flux = (Flux<String>) new TokenStreamToFluxAdapter().adapt(tokenStream);

        tokenStream.emitStreamingHandle();
        tokenStream.emitPartialResponse("hello");

        StepVerifier.create(flux)
                .expectNext("hello")
                .thenCancel()
                .verify();

        assertThat(streamingHandle.cancellationAttempts()).isOne();
    }

    private static Type getReturnTypeOfMethod(String methodName) {
        try {
            return Assistant.class.getDeclaredMethod(methodName).getGenericReturnType();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private static class TestTokenStream implements TokenStream {

        private final TestStreamingHandle streamingHandle;
        private Consumer<String> partialResponseHandler;
        private Consumer<StreamingHandle> streamingHandleHandler;
        private Consumer<ChatResponse> completionHandler;

        TestTokenStream() {
            this(new TestStreamingHandle());
        }

        TestTokenStream(TestStreamingHandle streamingHandle) {
            this.streamingHandle = streamingHandle;
        }

        @Override
        public TokenStream onPartialResponse(Consumer<String> partialResponseHandler) {
            this.partialResponseHandler = partialResponseHandler;
            return this;
        }

        @Override
        public TokenStream onStreamingHandle(Consumer<StreamingHandle> streamingHandleHandler) {
            this.streamingHandleHandler = streamingHandleHandler;
            return this;
        }

        @Override
        public TokenStream onRetrieved(Consumer<List<Content>> contentHandler) {
            return this;
        }

        @Override
        public TokenStream onToolExecuted(Consumer<ToolExecution> toolExecutionHandler) {
            return this;
        }

        @Override
        public TokenStream onCompleteResponse(Consumer<ChatResponse> completionHandler) {
            this.completionHandler = completionHandler;
            return this;
        }

        @Override
        public TokenStream onError(Consumer<Throwable> errorHandler) {
            return this;
        }

        @Override
        public TokenStream ignoreErrors() {
            return this;
        }

        @Override
        public void start() {}

        void emitStreamingHandle() {
            streamingHandleHandler.accept(streamingHandle);
        }

        void emitPartialResponse(String partialResponse) {
            partialResponseHandler.accept(partialResponse);
        }

        void emitCompleteResponse() {
            completionHandler.accept(ChatResponse.builder()
                    .aiMessage(AiMessage.from("hello"))
                    .build());
        }
    }

    private static class TestStreamingHandle implements StreamingHandle {

        private boolean cancelled;
        private int cancellationAttempts;

        @Override
        public void cancel() {
            cancellationAttempts++;
            cancelled = true;
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        int cancellationAttempts() {
            return cancellationAttempts;
        }
    }

    private static class ThrowingStreamingHandle extends TestStreamingHandle {

        @Override
        public void cancel() {
            super.cancel();
            throw new RuntimeException("cancel failed");
        }
    }
}
