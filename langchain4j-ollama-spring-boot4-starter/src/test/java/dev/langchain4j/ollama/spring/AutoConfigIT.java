package dev.langchain4j.ollama.spring;

import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.language.LanguageModel;
import dev.langchain4j.model.language.StreamingLanguageModel;
import dev.langchain4j.model.ollama.*;
import dev.langchain4j.model.output.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.testcontainers.containers.GenericContainer;

import java.util.concurrent.CompletableFuture;

import static dev.langchain4j.internal.Utils.isNullOrEmpty;
import static dev.langchain4j.model.chat.Capability.RESPONSE_FORMAT_JSON_SCHEMA;
import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AutoConfigIT {

    private static final String OLLAMA_BASE_URL = System.getenv("OLLAMA_BASE_URL");
    private static final String MODEL_NAME = "phi";

    static GenericContainer<?> ollama;

    @BeforeAll
    static void beforeAll() {
        if (isNullOrEmpty(OLLAMA_BASE_URL)) {
            ollama = new GenericContainer<>("langchain4j/ollama-" + MODEL_NAME).withExposedPorts(11434);
            ollama.start();
        }
    }

    @AfterAll
    static void afterAll() {
        if (ollama != null) {
            ollama.stop();
        }
    }

    private static String baseUrl() {
        if (isNullOrEmpty(OLLAMA_BASE_URL)) {
            return format("http://%s:%s", ollama.getHost(), ollama.getFirstMappedPort());
        } else {
            return OLLAMA_BASE_URL;
        }
    }

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AutoConfig.class));

    @Test
    void should_provide_chat_model() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.ollama.chat-model.base-url=" + baseUrl(),
                        "langchain4j.ollama.chat-model.model-name=" + MODEL_NAME,
                        "langchain4j.ollama.chat-model.temperature=0.0",
                        "langchain4j.ollama.chat-model.num-predict=20"
                )
                .run(context -> {

                    ChatModel model = context.getBean(ChatModel.class);
                    assertThat(model).isInstanceOf(OllamaChatModel.class);
                    assertThat(context.getBean(OllamaChatModel.class)).isSameAs(model);

                    assertThat(model.chat("What is the capital of Germany?")).contains("Berlin");
                });
    }

    @Test
    void should_provide_chat_model_with_listeners() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.ollama.chat-model.base-url=" + baseUrl(),
                        "langchain4j.ollama.chat-model.model-name=" + MODEL_NAME,
                        "langchain4j.ollama.chat-model.temperature=0.0",
                        "langchain4j.ollama.chat-model.num-predict=20"
                )
                .withUserConfiguration(ListenerConfig.class)
                .run(context -> {

                    ChatModel model = context.getBean(ChatModel.class);
                    assertThat(model).isInstanceOf(OllamaChatModel.class);
                    assertThat(context.getBean(OllamaChatModel.class)).isSameAs(model);

                    assertThat(model.chat("What is the capital of Germany?")).contains("Berlin");

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
    void should_provide_chat_model_with_supported_capabilities() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.ollama.chat-model.base-url=" + baseUrl(),
                        "langchain4j.ollama.chat-model.model-name=" + MODEL_NAME,
                        "langchain4j.ollama.chat-model.supportedCapabilities=RESPONSE_FORMAT_JSON_SCHEMA"
                )
                .run(context -> {

                    ChatModel model = context.getBean(ChatModel.class);
                    assertThat(model).isInstanceOf(OllamaChatModel.class);
                    assertThat(context.getBean(OllamaChatModel.class)).isSameAs(model);

                    assertThat(model.supportedCapabilities()).contains(RESPONSE_FORMAT_JSON_SCHEMA);
                });
    }

    @Test
    void should_create_chat_model_with_default_http_client() {

        OllamaChatModel model = OllamaChatModel.builder()
                .baseUrl(baseUrl())
                .modelName(MODEL_NAME)
                .temperature(0.0)
                .numPredict(20)
                .build();

        assertThat(model.chat("What is the capital of Germany?")).contains("Berlin");
    }

    @Test
    void should_provide_streaming_chat_model() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.ollama.streaming-chat-model.base-url=" + baseUrl(),
                        "langchain4j.ollama.streaming-chat-model.model-name=" + MODEL_NAME,
                        "langchain4j.ollama.streaming-chat-model.temperature=0.0",
                        "langchain4j.ollama.streaming-chat-model.num-predict=20"
                )
                .run(context -> {

                    StreamingChatModel model = context.getBean(StreamingChatModel.class);
                    assertThat(model).isInstanceOf(OllamaStreamingChatModel.class);
                    assertThat(context.getBean(OllamaStreamingChatModel.class)).isSameAs(model);

                    CompletableFuture<ChatResponse> future = new CompletableFuture<>();
                    model.chat("What is the capital of Germany?", new StreamingChatResponseHandler() {

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
                });
    }

    @Test
    void should_provide_streaming_chat_model_with_custom_task_executor() {

        ThreadPoolTaskExecutor customExecutor = spy(new ThreadPoolTaskExecutor());

        contextRunner
                .withBean("ollamaStreamingChatModelTaskExecutor", ThreadPoolTaskExecutor.class, () -> customExecutor)
                .withPropertyValues(
                        "langchain4j.ollama.streaming-chat-model.base-url=" + baseUrl(),
                        "langchain4j.ollama.streaming-chat-model.model-name=" + MODEL_NAME,
                        "langchain4j.ollama.streaming-chat-model.temperature=0.0",
                        "langchain4j.ollama.streaming-chat-model.num-predict=20"
                )
                .run(context -> {

                    StreamingChatModel model = context.getBean(StreamingChatModel.class);

                    CompletableFuture<ChatResponse> future = new CompletableFuture<>();
                    model.chat("What is the capital of Germany?", new StreamingChatResponseHandler() {

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
    void should_provide_streaming_chat_model_with_listeners() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.ollama.streaming-chat-model.base-url=" + baseUrl(),
                        "langchain4j.ollama.streaming-chat-model.model-name=" + MODEL_NAME,
                        "langchain4j.ollama.streaming-chat-model.temperature=0.0",
                        "langchain4j.ollama.streaming-chat-model.num-predict=20"
                )
                .withUserConfiguration(ListenerConfig.class)
                .run(context -> {

                    StreamingChatModel model = context.getBean(StreamingChatModel.class);
                    assertThat(model).isInstanceOf(OllamaStreamingChatModel.class);
                    assertThat(context.getBean(OllamaStreamingChatModel.class)).isSameAs(model);

                    CompletableFuture<ChatResponse> future = new CompletableFuture<>();
                    model.chat("What is the capital of Germany?", new StreamingChatResponseHandler() {

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
    void should_create_streaming_chat_model_with_default_http_client() throws Exception {

        OllamaStreamingChatModel model = OllamaStreamingChatModel.builder()
                .baseUrl(baseUrl())
                .modelName(MODEL_NAME)
                .temperature(0.0)
                .numPredict(20)
                .build();

        CompletableFuture<ChatResponse> future = new CompletableFuture<>();
        model.chat("What is the capital of Germany?", new StreamingChatResponseHandler() {

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
    }

    @Test
    void should_provide_language_model() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.ollama.language-model.base-url=" + baseUrl(),
                        "langchain4j.ollama.language-model.model-name=" + MODEL_NAME,
                        "langchain4j.ollama.language-model.temperature=0.0",
                        "langchain4j.ollama.language-model.num-predict=20"
                )
                .run(context -> {

                    LanguageModel model = context.getBean(LanguageModel.class);
                    assertThat(model).isInstanceOf(OllamaLanguageModel.class);
                    assertThat(context.getBean(OllamaLanguageModel.class)).isSameAs(model);

                    assertThat(model.generate("What is the capital of Germany?").content()).contains("Berlin");
                });
    }

    @Test
    void should_provide_streaming_language_model() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.ollama.streaming-language-model.base-url=" + baseUrl(),
                        "langchain4j.ollama.streaming-language-model.model-name=" + MODEL_NAME,
                        "langchain4j.ollama.streaming-language-model.temperature=0.0",
                        "langchain4j.ollama.streaming-language-model.num-predict=20"
                )
                .run(context -> {

                    StreamingLanguageModel model = context.getBean(StreamingLanguageModel.class);
                    assertThat(model).isInstanceOf(OllamaStreamingLanguageModel.class);
                    assertThat(context.getBean(OllamaStreamingLanguageModel.class)).isSameAs(model);

                    CompletableFuture<Response<String>> future = new CompletableFuture<>();
                    model.generate("What is the capital of Germany?", new StreamingResponseHandler<>() {

                        @Override
                        public void onNext(String token) {
                        }

                        @Override
                        public void onComplete(Response<String> response) {
                            future.complete(response);
                        }

                        @Override
                        public void onError(Throwable error) {
                        }
                    });
                    Response<String> response = future.get(30, SECONDS);
                    assertThat(response.content()).contains("Berlin");
                });
    }

    @Test
    void should_provide_embedding_model() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.ollama.embedding-model.base-url=" + baseUrl(),
                        "langchain4j.ollama.embedding-model.model-name=" + MODEL_NAME
                )
                .run(context -> {

                    EmbeddingModel model = context.getBean(EmbeddingModel.class);
                    assertThat(model).isInstanceOf(OllamaEmbeddingModel.class);
                    assertThat(context.getBean(OllamaEmbeddingModel.class)).isSameAs(model);

                    assertThat(model.embed("hi").content().dimension()).isEqualTo(2560);
                });
    }

    @Test
    void should_create_embedding_model_with_default_http_client() {

        OllamaEmbeddingModel model = OllamaEmbeddingModel.builder()
                .baseUrl(baseUrl())
                .modelName(MODEL_NAME)
                .build();

        assertThat(model.embed("hi").content().dimension()).isEqualTo(2560);
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