package dev.langchain4j.ollama.spring;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.chat.listener.ChatModelListener;
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
                        "langchain4j.ollama.chat-model.max-tokens=20",
                        "langchain4j.ollama.chat-model.temperature=0.0"
                )
                .run(context -> {

                    ChatLanguageModel chatLanguageModel = context.getBean(ChatLanguageModel.class);
                    assertThat(chatLanguageModel).isInstanceOf(OllamaChatModel.class);
                    assertThat(chatLanguageModel.generate("What is the capital of Germany?")).contains("Berlin");

                    assertThat(context.getBean(OllamaChatModel.class)).isSameAs(chatLanguageModel);
                });
    }

    @Test
    void should_provide_chat_model_with_listeners() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.ollama.chat-model.base-url=" + baseUrl(),
                        "langchain4j.ollama.chat-model.model-name=" + MODEL_NAME,
                        "langchain4j.ollama.chat-model.max-tokens=20",
                        "langchain4j.ollama.chat-model.temperature=0.0"
                )
                .withUserConfiguration(ListenerConfig.class)
                .run(context -> {

                    ChatLanguageModel chatLanguageModel = context.getBean(ChatLanguageModel.class);
                    assertThat(chatLanguageModel).isInstanceOf(OllamaChatModel.class);
                    assertThat(chatLanguageModel.generate("What is the capital of Germany?")).contains("Berlin");

                    assertThat(context.getBean(OllamaChatModel.class)).isSameAs(chatLanguageModel);

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

                    ChatLanguageModel chatLanguageModel = context.getBean(ChatLanguageModel.class);
                    assertThat(chatLanguageModel).isInstanceOf(OllamaChatModel.class);
                    assertThat(chatLanguageModel.supportedCapabilities()).contains(RESPONSE_FORMAT_JSON_SCHEMA);

                    assertThat(context.getBean(OllamaChatModel.class)).isSameAs(chatLanguageModel);
                });
    }

    @Test
    void should_create_chat_model_with_default_http_client() {
        contextRunner.run(context -> {

            OllamaChatModel model = OllamaChatModel.builder()
                    .baseUrl(OLLAMA_BASE_URL)
                    .modelName(MODEL_NAME)
                    .temperature(0.0)
                    .numPredict(20)
                    .build();

            assertThat(model.generate("What is the capital of Germany?")).contains("Berlin");
        });
    }

    @Test
    void should_provide_streaming_chat_model() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.ollama.streaming-chat-model.base-url=" + baseUrl(),
                        "langchain4j.ollama.streaming-chat-model.model-name=" + MODEL_NAME,
                        "langchain4j.ollama.streaming-chat-model.max-tokens=20",
                        "langchain4j.ollama.streaming-chat-model.temperature=0.0"
                )
                .run(context -> {

                    StreamingChatLanguageModel streamingChatLanguageModel = context.getBean(StreamingChatLanguageModel.class);
                    assertThat(streamingChatLanguageModel).isInstanceOf(OllamaStreamingChatModel.class);
                    CompletableFuture<Response<AiMessage>> future = new CompletableFuture<>();
                    streamingChatLanguageModel.generate("What is the capital of Germany?", new StreamingResponseHandler<AiMessage>() {

                        @Override
                        public void onNext(String token) {
                        }

                        @Override
                        public void onComplete(Response<AiMessage> response) {
                            future.complete(response);
                        }

                        @Override
                        public void onError(Throwable error) {
                        }
                    });
                    Response<AiMessage> response = future.get(60, SECONDS);
                    assertThat(response.content().text()).contains("Berlin");

                    assertThat(context.getBean(OllamaStreamingChatModel.class)).isSameAs(streamingChatLanguageModel);
                });
    }

    // TODO test with custom http client builder

    @Test
    void should_provide_streaming_chat_model_with_custom_task_executor() {

        ThreadPoolTaskExecutor customExecutor = spy(new ThreadPoolTaskExecutor());

        contextRunner
                .withBean("ollamaTaskExecutor", ThreadPoolTaskExecutor.class, () -> customExecutor)
                .withPropertyValues(
                        "langchain4j.ollama.streaming-chat-model.base-url=" + baseUrl(),
                        "langchain4j.ollama.streaming-chat-model.model-name=" + MODEL_NAME,
                        "langchain4j.ollama.streaming-chat-model.max-tokens=20",
                        "langchain4j.ollama.streaming-chat-model.temperature=0.0"
                )
                .run(context -> {

                    StreamingChatLanguageModel streamingChatLanguageModel = context.getBean(StreamingChatLanguageModel.class);
                    CompletableFuture<Response<AiMessage>> future = new CompletableFuture<>();
                    streamingChatLanguageModel.generate("What is the capital of Germany?", new StreamingResponseHandler<AiMessage>() {

                        @Override
                        public void onNext(String token) {
                        }

                        @Override
                        public void onComplete(Response<AiMessage> response) {
                            future.complete(response);
                        }

                        @Override
                        public void onError(Throwable error) {
                        }
                    });
                    Response<AiMessage> response = future.get(60, SECONDS);
                    assertThat(response.content().text()).contains("Berlin");

                    verify(customExecutor).execute(any());
                });
    }

    @Test
    void should_provide_streaming_chat_model_with_listeners() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.ollama.streaming-chat-model.base-url=" + baseUrl(),
                        "langchain4j.ollama.streaming-chat-model.model-name=" + MODEL_NAME,
                        "langchain4j.ollama.streaming-chat-model.max-tokens=20",
                        "langchain4j.ollama.streaming-chat-model.temperature=0.0"
                )
                .withUserConfiguration(ListenerConfig.class)
                .run(context -> {

                    StreamingChatLanguageModel streamingChatLanguageModel = context.getBean(StreamingChatLanguageModel.class);
                    assertThat(streamingChatLanguageModel).isInstanceOf(OllamaStreamingChatModel.class);
                    CompletableFuture<Response<AiMessage>> future = new CompletableFuture<>();
                    streamingChatLanguageModel.generate("What is the capital of Germany?", new StreamingResponseHandler<AiMessage>() {

                        @Override
                        public void onNext(String token) {
                        }

                        @Override
                        public void onComplete(Response<AiMessage> response) {
                            future.complete(response);
                        }

                        @Override
                        public void onError(Throwable error) {
                        }
                    });
                    Response<AiMessage> response = future.get(60, SECONDS);
                    assertThat(response.content().text()).contains("Berlin");

                    assertThat(context.getBean(OllamaStreamingChatModel.class)).isSameAs(streamingChatLanguageModel);

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
    void should_create_streaming_chat_model_with_default_http_client() {
        contextRunner.run(context -> {

            OllamaStreamingChatModel model = OllamaStreamingChatModel.builder()
                    .baseUrl(OLLAMA_BASE_URL)
                    .modelName(MODEL_NAME)
                    .temperature(0.0)
                    .numPredict(20)
                    .build();

            CompletableFuture<Response<AiMessage>> future = new CompletableFuture<>();
            model.generate("What is the capital of Germany?", new StreamingResponseHandler<>() {

                @Override
                public void onNext(String token) {
                }

                @Override
                public void onComplete(Response<AiMessage> response) {
                    future.complete(response);
                }

                @Override
                public void onError(Throwable error) {
                }
            });
            Response<AiMessage> response = future.get(60, SECONDS);
            assertThat(response.content().text()).contains("Berlin");
        });
    }

    @Test
    void should_provide_language_model() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.ollama.language-model.base-url=" + baseUrl(),
                        "langchain4j.ollama.language-model.model-name=" + MODEL_NAME,
                        "langchain4j.ollama.language-model.max-tokens=20",
                        "langchain4j.ollama.language-model.temperature=0.0"
                )
                .run(context -> {

                    LanguageModel languageModel = context.getBean(LanguageModel.class);
                    assertThat(languageModel).isInstanceOf(OllamaLanguageModel.class);
                    assertThat(languageModel.generate("What is the capital of Germany?").content()).contains("Berlin");

                    assertThat(context.getBean(OllamaLanguageModel.class)).isSameAs(languageModel);
                });
    }

    @Test
    void should_provide_streaming_language_model() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.ollama.streaming-language-model.base-url=" + baseUrl(),
                        "langchain4j.ollama.streaming-language-model.model-name=" + MODEL_NAME,
                        "langchain4j.ollama.streaming-language-model.max-tokens=20",
                        "langchain4j.ollama.streaming-language-model.temperature=0.0"
                )
                .run(context -> {

                    StreamingLanguageModel streamingLanguageModel = context.getBean(StreamingLanguageModel.class);
                    assertThat(streamingLanguageModel).isInstanceOf(OllamaStreamingLanguageModel.class);
                    CompletableFuture<Response<String>> future = new CompletableFuture<>();
                    streamingLanguageModel.generate("What is the capital of Germany?", new StreamingResponseHandler<String>() {

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
                    Response<String> response = future.get(60, SECONDS);
                    assertThat(response.content()).contains("Berlin");

                    assertThat(context.getBean(OllamaStreamingLanguageModel.class)).isSameAs(streamingLanguageModel);
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

                    EmbeddingModel embeddingModel = context.getBean(EmbeddingModel.class);
                    assertThat(embeddingModel).isInstanceOf(OllamaEmbeddingModel.class);
                    assertThat(embeddingModel.embed("hi").content().dimension()).isEqualTo(2560);

                    assertThat(context.getBean(OllamaEmbeddingModel.class)).isSameAs(embeddingModel);
                });
    }

    @Test
    void should_create_embedding_model_with_default_http_client() {
        contextRunner.run(context -> {

            OllamaEmbeddingModel model = OllamaEmbeddingModel.builder()
                    .baseUrl(OLLAMA_BASE_URL)
                    .modelName(MODEL_NAME)
                    .build();

            assertThat(model.embed("hi").content().dimension()).isEqualTo(2560);
        });
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