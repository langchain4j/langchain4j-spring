package dev.langchain4j.mistralai.spring;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junitpioneer.jupiter.RetryingTest;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.language.LanguageModel;
import dev.langchain4j.model.language.StreamingLanguageModel;
import dev.langchain4j.model.mistralai.MistralAiChatModel;
import dev.langchain4j.model.mistralai.MistralAiEmbeddingModel;
import dev.langchain4j.model.mistralai.MistralAiFimModel;
import dev.langchain4j.model.mistralai.MistralAiModerationModel;
import dev.langchain4j.model.mistralai.MistralAiStreamingChatModel;
import dev.langchain4j.model.mistralai.MistralAiStreamingFimModel;
import dev.langchain4j.model.moderation.ModerationModel;
import dev.langchain4j.model.output.Response;

@EnabledIfEnvironmentVariable(named = "MISTRAL_AI_API_KEY", matches = ".+")
public class AutoConfigIT {

    private static final String API_KEY = System.getenv("MISTRAL_AI_API_KEY");

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AutoConfig.class));


    @Test
    void should_provide_chat_model() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.mistral-ai.chat-model.api-key=" + API_KEY,
                        "langchain4j.mistral-ai.chat-model.model-name=ministral-3b-latest",
                        "langchain4j.mistral-ai.chat-model.max-tokens=10")
                .run(context -> {

                    ChatModel chatModel = context.getBean(ChatModel.class);
                    assertThat(chatModel).isInstanceOf(MistralAiChatModel.class);
                    assertThat(chatModel.chat("What is the capital of Germany?")).contains("Berlin");

                    assertThat(context.getBean(MistralAiChatModel.class)).isSameAs(chatModel);
                });
    }

    @Test
    void should_provide_streaming_chat_model() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.mistral-ai.streaming-chat-model.api-key=" + API_KEY,
                        "langchain4j.mistral-ai.streaming-chat-model.model-name=ministral-3b-latest",
                        "langchain4j.mistral-ai.streaming-chat-model.max-tokens=10")
                .run(context -> {

                    StreamingChatModel streamingChatModel = context.getBean(StreamingChatModel.class);
                    assertThat(streamingChatModel).isInstanceOf(MistralAiStreamingChatModel.class);
                    CompletableFuture<ChatResponse> future = new CompletableFuture<>();
                    streamingChatModel.chat("What is the capital of Germany?", new StreamingChatResponseHandler() {

                        @Override
                        public void onPartialResponse(String partialResponse) {
                        }

                        @Override
                        public void onCompleteResponse(ChatResponse completeResponse) {
                            future.complete(completeResponse);
                        }

                        @Override
                        public void onError(Throwable error) {
                        }
                    });
                    ChatResponse response = future.get(60, SECONDS);
                    assertThat(response.aiMessage().text()).contains("Berlin");

                    assertThat(context.getBean(MistralAiStreamingChatModel.class)).isSameAs(streamingChatModel);
                });
    }

    @Test
    void should_provide_embedding_model() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.mistral-ai.embedding-model.api-key=" + API_KEY,
                        "langchain4j.mistral-ai.embedding-model.model-name=mistral-embed")
                .run(context -> {

                    EmbeddingModel embeddingModel = context.getBean(EmbeddingModel.class);
                    assertThat(embeddingModel).isInstanceOf(MistralAiEmbeddingModel.class);
                    assertThat(context.getBean(MistralAiEmbeddingModel.class)).isSameAs(embeddingModel);

                    assertThat(embeddingModel.embed("hello").content().dimension()).isGreaterThan(0);
                });
    }

    @Test
    void should_provide_fim_model() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.mistral-ai.fim-model.api-key=" + API_KEY,
                        "langchain4j.mistral-ai.fim-model.model-name=codestral-2508",
                        "langchain4j.mistral-ai.fim-model.max-tokens=20")
                .run(context -> {

                    LanguageModel fimModel = context.getBean(LanguageModel.class);
                    assertThat(fimModel).isInstanceOf(MistralAiFimModel.class);
                    assertThat(context.getBean(MistralAiFimModel.class)).isSameAs(fimModel);

                    assertThat(fimModel.generate("public static void main(").content()).isNotBlank();
                });
    }

    @Test
    void should_provide_streaming_fim_model() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.mistral-ai.streaming-fim-model.api-key=" + API_KEY,
                        "langchain4j.mistral-ai.streaming-fim-model.model-name=codestral-2508",
                        "langchain4j.mistral-ai.streaming-fim-model.max-tokens=20")
                .run(context -> {

                    StreamingLanguageModel streamingFimModel = context.getBean(StreamingLanguageModel.class);
                    assertThat(streamingFimModel).isInstanceOf(MistralAiStreamingFimModel.class);
                    assertThat(context.getBean(MistralAiStreamingFimModel.class)).isSameAs(streamingFimModel);

                    CompletableFuture<Response<String>> future = new CompletableFuture<>();
                    streamingFimModel.generate("public static void main(", new StreamingResponseHandler<String>() {

                        @Override
                        public void onNext(String token) {
                        }

                        @Override
                        public void onComplete(Response<String> response) {
                            future.complete(response);
                        }

                        @Override
                        public void onError(Throwable error) {
                            future.completeExceptionally(error);
                        }
                    });
                    Response<String> response = future.get(30, SECONDS);
                    assertThat(response.content()).isNotBlank();
                });
    }

    @Test
    void should_provide_moderation_model() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.mistral-ai.moderation-model.api-key=" + API_KEY,
                        "langchain4j.mistral-ai.moderation-model.model-name=mistral-moderation-latest")
                .run(context -> {

                    ModerationModel moderationModel = context.getBean(ModerationModel.class);
                    assertThat(moderationModel).isInstanceOf(MistralAiModerationModel.class);
                    assertThat(context.getBean(MistralAiModerationModel.class)).isSameAs(moderationModel);

                    assertThat(moderationModel.moderate("I want to hug them.").content().flagged()).isFalse();
                });
    }

    @Test
    void should_provide_chat_model_with_listeners() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.mistral-ai.chat-model.api-key=" + API_KEY,
                        "langchain4j.mistral-ai.chat-model.model-name=ministral-3b-latest",
                        "langchain4j.mistral-ai.chat-model.max-tokens=10")
                .withUserConfiguration(ListenerConfig.class)
                .run(context -> {

                    ChatModel chatModel = context.getBean(ChatModel.class);
                    assertThat(chatModel).isInstanceOf(MistralAiChatModel.class);
                    assertThat(context.getBean(MistralAiChatModel.class)).isSameAs(chatModel);

                    assertThat(chatModel.chat("What is the capital of Germany?")).contains("Berlin");

                    ChatModelListener listener1 = context.getBean("listener1", ChatModelListener.class);
                    ChatModelListener listener2 = context.getBean("listener2", ChatModelListener.class);
                    InOrder inOrder = Mockito.inOrder(listener1, listener2);
                    inOrder.verify(listener2).onRequest(any());
                    inOrder.verify(listener1).onRequest(any());
                    inOrder.verify(listener2).onResponse(any());
                    inOrder.verify(listener1).onResponse(any());
                    inOrder.verifyNoMoreInteractions();
                });
    }

    @Test
    void should_provide_streaming_chat_model_with_listeners() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.mistral-ai.streaming-chat-model.api-key=" + API_KEY,
                        "langchain4j.mistral-ai.streaming-chat-model.model-name=ministral-3b-latest",
                        "langchain4j.mistral-ai.streaming-chat-model.max-tokens=10")
                .withUserConfiguration(ListenerConfig.class)
                .run(context -> {

                    StreamingChatModel streamingChatModel = context.getBean(StreamingChatModel.class);
                    assertThat(streamingChatModel).isInstanceOf(MistralAiStreamingChatModel.class);
                    assertThat(context.getBean(MistralAiStreamingChatModel.class)).isSameAs(streamingChatModel);

                    CompletableFuture<ChatResponse> future = new CompletableFuture<>();
                    streamingChatModel.chat("What is the capital of Germany?", new StreamingChatResponseHandler() {

                        @Override
                        public void onPartialResponse(String partialResponse) {
                        }

                        @Override
                        public void onCompleteResponse(ChatResponse completeResponse) {
                            future.complete(completeResponse);
                        }

                        @Override
                        public void onError(Throwable error) {
                            future.completeExceptionally(error);
                        }
                    });
                    ChatResponse chatResponse = future.get(30, SECONDS);
                    assertThat(chatResponse.aiMessage().text()).contains("Berlin");

                    ChatModelListener listener1 = context.getBean("listener1", ChatModelListener.class);
                    ChatModelListener listener2 = context.getBean("listener2", ChatModelListener.class);
                    InOrder inOrder = Mockito.inOrder(listener1, listener2);
                    inOrder.verify(listener2).onRequest(any());
                    inOrder.verify(listener1).onRequest(any());
                    inOrder.verify(listener2).onResponse(any());
                    inOrder.verify(listener1).onResponse(any());
                    inOrder.verifyNoMoreInteractions();
                });
    }

    @Test
    void should_provide_streaming_chat_model_with_custom_task_executor() {

        ThreadPoolTaskExecutor customExecutor = spy(new ThreadPoolTaskExecutor());

        contextRunner
                .withBean("mistralAiStreamingChatModelTaskExecutor", ThreadPoolTaskExecutor.class, () -> customExecutor)
                .withPropertyValues(
                        "langchain4j.mistral-ai.streaming-chat-model.api-key=" + API_KEY,
                        "langchain4j.mistral-ai.streaming-chat-model.model-name=ministral-3b-latest",
                        "langchain4j.mistral-ai.streaming-chat-model.max-tokens=10")
                .run(context -> {

                    StreamingChatModel streamingChatModel = context.getBean(StreamingChatModel.class);

                    CompletableFuture<ChatResponse> future = new CompletableFuture<>();
                    streamingChatModel.chat("What is the capital of Germany?", new StreamingChatResponseHandler() {

                        @Override
                        public void onPartialResponse(String partialResponse) {
                        }

                        @Override
                        public void onCompleteResponse(ChatResponse completeResponse) {
                            future.complete(completeResponse);
                        }

                        @Override
                        public void onError(Throwable error) {
                            future.completeExceptionally(error);
                        }
                    });
                    ChatResponse chatResponse = future.get(30, SECONDS);
                    assertThat(chatResponse.aiMessage().text()).contains("Berlin");

                    verify(customExecutor).execute(any());
                });
    }

    @Test
    void should_provide_streaming_fim_model_with_custom_task_executor() {

        ThreadPoolTaskExecutor customExecutor = spy(new ThreadPoolTaskExecutor());

        contextRunner
                .withBean("mistralAiStreamingFimModelTaskExecutor", ThreadPoolTaskExecutor.class, () -> customExecutor)
                .withPropertyValues(
                        "langchain4j.mistral-ai.streaming-fim-model.api-key=" + API_KEY,
                        "langchain4j.mistral-ai.streaming-fim-model.model-name=codestral-2508",
                        "langchain4j.mistral-ai.streaming-fim-model.max-tokens=20")
                .run(context -> {

                    StreamingLanguageModel streamingFimModel = context.getBean(StreamingLanguageModel.class);

                    CompletableFuture<Response<String>> future = new CompletableFuture<>();
                    streamingFimModel.generate("public static void main(", new StreamingResponseHandler<String>() {

                        @Override
                        public void onNext(String token) {
                        }

                        @Override
                        public void onComplete(Response<String> response) {
                            future.complete(response);
                        }

                        @Override
                        public void onError(Throwable error) {
                            future.completeExceptionally(error);
                        }
                    });
                    Response<String> response = future.get(30, SECONDS);
                    assertThat(response.content()).isNotBlank();

                    verify(customExecutor).execute(any());
                });
    }

    @Test
    void should_create_chat_model_with_default_http_client() {

        MistralAiChatModel model = MistralAiChatModel.builder()
                .apiKey(API_KEY)
                .modelName("ministral-3b-latest")
                .maxTokens(10)
                .build();

        assertThat(model.chat("What is the capital of Germany?")).contains("Berlin");
    }

    @RetryingTest(maxAttempts = 3, suspendForMs = 1_000)
    void should_create_streaming_chat_model_with_default_http_client() throws Exception {

        MistralAiStreamingChatModel model = MistralAiStreamingChatModel.builder()
                .apiKey(API_KEY)
                .modelName("ministral-3b-latest")
                .maxTokens(20)
                .build();

        CompletableFuture<ChatResponse> future1 = new CompletableFuture<>();
        AtomicReference<LocalDateTime> streamingStarted1 = new AtomicReference<>();
        AtomicReference<LocalDateTime> streamingFinished1 = new AtomicReference<>();
        model.chat("Tell me a story exactly 20 words long", new StreamingChatResponseHandler() {

            @Override
            public void onPartialResponse(String partialResponse) {
                if (streamingStarted1.get() == null) {
                    streamingStarted1.set(LocalDateTime.now());
                }
            }

            @Override
            public void onCompleteResponse(ChatResponse completeResponse) {
                streamingFinished1.set(LocalDateTime.now());
                future1.complete(completeResponse);
            }

            @Override
            public void onError(Throwable error) {
                future1.completeExceptionally(error);
            }
        });

        CompletableFuture<ChatResponse> future2 = new CompletableFuture<>();
        AtomicReference<LocalDateTime> streamingStarted2 = new AtomicReference<>();
        AtomicReference<LocalDateTime> streamingFinished2 = new AtomicReference<>();
        model.chat("Tell me a story exactly 20 words long", new StreamingChatResponseHandler() {

            @Override
            public void onPartialResponse(String partialResponse) {
                if (streamingStarted2.get() == null) {
                    streamingStarted2.set(LocalDateTime.now());
                }
            }

            @Override
            public void onCompleteResponse(ChatResponse completeResponse) {
                streamingFinished2.set(LocalDateTime.now());
                future2.complete(completeResponse);
            }

            @Override
            public void onError(Throwable error) {
                future2.completeExceptionally(error);
            }
        });

        ChatResponse chatResponse1 = future1.get(30, SECONDS);
        assertThat(chatResponse1.aiMessage().text()).isNotBlank();

        ChatResponse chatResponse2 = future2.get(30, SECONDS);
        assertThat(chatResponse2.aiMessage().text()).isNotBlank();

        assertThat(streamingStarted1.get()).isBefore(streamingFinished2.get());
        assertThat(streamingStarted2.get()).isBefore(streamingFinished1.get());
    }

    @Test
    void should_create_embedding_model_with_default_http_client() {

        MistralAiEmbeddingModel model = MistralAiEmbeddingModel.builder()
                .apiKey(API_KEY)
                .modelName("mistral-embed")
                .build();

        assertThat(model.embed("hello").content().dimension()).isGreaterThan(0);
    }

    @Test
    void should_create_fim_model_with_default_http_client() {

        MistralAiFimModel model = MistralAiFimModel.builder()
                .apiKey(API_KEY)
                .modelName("codestral-2508")
                .maxTokens(20)
                .build();

        assertThat(model.generate("public static void main(").content()).isNotBlank();
    }

    @Test
    void should_create_streaming_fim_model_with_default_http_client() throws Exception {

        MistralAiStreamingFimModel model = MistralAiStreamingFimModel.builder()
                .apiKey(API_KEY)
                .modelName("codestral-2508")
                .maxTokens(20)
                .build();

        CompletableFuture<Response<String>> future = new CompletableFuture<>();
        model.generate("public static void main(", new StreamingResponseHandler<String>() {

            @Override
            public void onNext(String token) {
            }

            @Override
            public void onComplete(Response<String> response) {
                future.complete(response);
            }

            @Override
            public void onError(Throwable error) {
                future.completeExceptionally(error);
            }
        });
        Response<String> response = future.get(30, SECONDS);
        assertThat(response.content()).isNotBlank();
    }

    @Test
    void should_create_moderation_model_with_default_http_client() {

        MistralAiModerationModel model = new MistralAiModerationModel.Builder()
                .apiKey(API_KEY)
                .modelName("mistral-moderation-latest")
                .build();

        assertThat(model.moderate("I want to hug them.").content().flagged()).isFalse();
    }

    @Configuration
    static class ListenerConfig {

        @Bean
        @Order(2)
        ChatModelListener listener1() {
            return mock(ChatModelListener.class);
        }

        @Bean
        @Order(1)
        ChatModelListener listener2() {
            return mock(ChatModelListener.class);
        }
    }
}
