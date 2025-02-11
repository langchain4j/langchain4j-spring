package dev.langchain4j.openai.spring;

import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.image.ImageModel;
import dev.langchain4j.model.language.LanguageModel;
import dev.langchain4j.model.language.StreamingLanguageModel;
import dev.langchain4j.model.moderation.ModerationModel;
import dev.langchain4j.model.openai.*;
import dev.langchain4j.model.output.Response;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AutoConfigIT {

    private static final String BASE_URL = System.getenv("OPENAI_BASE_URL");
    private static final String API_KEY = System.getenv("OPENAI_API_KEY");

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AutoConfig.class));

    @Test
    void should_provide_chat_model() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.open-ai.chat-model.base-url=" + BASE_URL,
                        "langchain4j.open-ai.chat-model.api-key=" + API_KEY,
                        "langchain4j.open-ai.chat-model.model-name=gpt-4o-mini",
                        "langchain4j.open-ai.chat-model.max-tokens=20"
                )
                .run(context -> {

                    ChatLanguageModel model = context.getBean(ChatLanguageModel.class);
                    assertThat(model).isInstanceOf(OpenAiChatModel.class);
                    assertThat(context.getBean(OpenAiChatModel.class)).isSameAs(model);

                    assertThat(model.chat("What is the capital of Germany?")).contains("Berlin");
                });
    }

    @Test
    void should_provide_chat_model_with_listeners() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.open-ai.chat-model.base-url=" + BASE_URL,
                        "langchain4j.open-ai.chat-model.api-key=" + API_KEY,
                        "langchain4j.open-ai.chat-model.model-name=gpt-4o-mini",
                        "langchain4j.open-ai.chat-model.max-tokens=20"
                )
                .withUserConfiguration(ListenerConfig.class)
                .run(context -> {

                    ChatLanguageModel model = context.getBean(ChatLanguageModel.class);
                    assertThat(model).isInstanceOf(OpenAiChatModel.class);
                    assertThat(context.getBean(OpenAiChatModel.class)).isSameAs(model);

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
    void should_create_chat_model_with_default_http_client() {

        OpenAiChatModel model = OpenAiChatModel.builder()
                .baseUrl(System.getenv("OPENAI_BASE_URL"))
                .apiKey(API_KEY)
                .modelName("gpt-4o-mini")
                .temperature(0.0)
                .maxTokens(20)
                .build();

        assertThat(model.chat("What is the capital of Germany?")).contains("Berlin");
    }

    @Test
    void should_provide_streaming_chat_model() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.open-ai.streaming-chat-model.base-url=" + BASE_URL,
                        "langchain4j.open-ai.streaming-chat-model.api-key=" + API_KEY,
                        "langchain4j.open-ai.streaming-chat-model.model-name=gpt-4o-mini",
                        "langchain4j.open-ai.streaming-chat-model.max-tokens=20"
                )
                .run(context -> {

                    StreamingChatLanguageModel model = context.getBean(StreamingChatLanguageModel.class);
                    assertThat(model).isInstanceOf(OpenAiStreamingChatModel.class);
                    assertThat(context.getBean(OpenAiStreamingChatModel.class)).isSameAs(model);

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
                    ChatResponse chatResponse = future.get(15, SECONDS);
                    assertThat(chatResponse.aiMessage().text()).contains("Berlin");
                });
    }

    @Test
    void should_provide_streaming_chat_model_with_listeners() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.open-ai.streaming-chat-model.base-url=" + BASE_URL,
                        "langchain4j.open-ai.streaming-chat-model.api-key=" + API_KEY,
                        "langchain4j.open-ai.streaming-chat-model.model-name=gpt-4o-mini",
                        "langchain4j.open-ai.streaming-chat-model.max-tokens=20"
                )
                .withUserConfiguration(ListenerConfig.class)
                .run(context -> {

                    StreamingChatLanguageModel model = context.getBean(StreamingChatLanguageModel.class);
                    assertThat(model).isInstanceOf(OpenAiStreamingChatModel.class);
                    assertThat(context.getBean(OpenAiStreamingChatModel.class)).isSameAs(model);

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
                    ChatResponse chatResponse = future.get(15, SECONDS);
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
                .withBean("openAiStreamingChatModelTaskExecutor", ThreadPoolTaskExecutor.class, () -> customExecutor)
                .withPropertyValues(
                        "langchain4j.open-ai.streaming-chat-model.base-url=" + BASE_URL,
                        "langchain4j.open-ai.streaming-chat-model.api-key=" + API_KEY,
                        "langchain4j.open-ai.streaming-chat-model.model-name=gpt-4o-mini",
                        "langchain4j.open-ai.streaming-chat-model.max-tokens=20",
                        "langchain4j.open-ai.streaming-chat-model.temperature=0.0"
                )
                .run(context -> {

                    StreamingChatLanguageModel model = context.getBean(StreamingChatLanguageModel.class);

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
                    ChatResponse chatResponse = future.get(15, SECONDS);
                    assertThat(chatResponse.aiMessage().text()).contains("Berlin");

                    verify(customExecutor).execute(any());
                });
    }

    @Test
    void should_create_streaming_chat_model_with_default_http_client() throws Exception {

        OpenAiStreamingChatModel model = OpenAiStreamingChatModel.builder()
                .baseUrl(BASE_URL)
                .apiKey(API_KEY)
                .modelName("gpt-4o-mini")
                .temperature(0.0)
                .maxTokens(20)
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
        ChatResponse chatResponse = future.get(15, SECONDS);
        assertThat(chatResponse.aiMessage().text()).contains("Berlin");
    }

    @Test
    void should_provide_language_model() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.open-ai.language-model.base-url=" + BASE_URL,
                        "langchain4j.open-ai.language-model.api-key=" + API_KEY,
                        "langchain4j.open-ai.language-model.model-name=gpt-3.5-turbo-instruct",
                        "langchain4j.open-ai.language-model.temperature=0.0"
                )
                .run(context -> {

                    LanguageModel model = context.getBean(LanguageModel.class);
                    assertThat(model).isInstanceOf(OpenAiLanguageModel.class);
                    assertThat(context.getBean(OpenAiLanguageModel.class)).isSameAs(model);

                    assertThat(model.generate("What is the capital of Germany?").content()).contains("Berlin");
                });
    }

    @Test
    void should_provide_streaming_language_model_with_custom_task_executor() {

        ThreadPoolTaskExecutor customExecutor = spy(new ThreadPoolTaskExecutor());

        contextRunner
                .withBean("openAiStreamingLanguageModelTaskExecutor", ThreadPoolTaskExecutor.class, () -> customExecutor)
                .withPropertyValues(
                        "langchain4j.open-ai.streaming-language-model.base-url=" + BASE_URL,
                        "langchain4j.open-ai.streaming-language-model.api-key=" + API_KEY,
                        "langchain4j.open-ai.streaming-language-model.model-name=gpt-3.5-turbo-instruct",
                        "langchain4j.open-ai.streaming-language-model.temperature=0.0"
                )
                .run(context -> {

                    StreamingLanguageModel model = context.getBean(StreamingLanguageModel.class);

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
                            future.completeExceptionally(error);
                        }
                    });
                    Response<String> response = future.get(15, SECONDS);
                    assertThat(response.content()).contains("Berlin");

                    verify(customExecutor).execute(any());
                });
    }

    @Test
    void should_create_language_model_with_default_http_client() {

        OpenAiLanguageModel model = OpenAiLanguageModel.builder()
                .baseUrl(BASE_URL)
                .apiKey(API_KEY)
                .modelName("gpt-3.5-turbo-instruct")
                .temperature(0.0)
                .build();

        assertThat(model.generate("What is the capital of Germany?").content()).contains("Berlin");
    }

    @Test
    void should_provide_streaming_language_model() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.open-ai.streaming-language-model.base-url=" + BASE_URL,
                        "langchain4j.open-ai.streaming-language-model.api-key=" + API_KEY,
                        "langchain4j.open-ai.streaming-language-model.model-name=gpt-3.5-turbo-instruct",
                        "langchain4j.open-ai.streaming-language-model.temperature=0.0"
                )
                .run(context -> {

                    StreamingLanguageModel model = context.getBean(StreamingLanguageModel.class);
                    assertThat(model).isInstanceOf(OpenAiStreamingLanguageModel.class);
                    assertThat(context.getBean(OpenAiStreamingLanguageModel.class)).isSameAs(model);

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
                            future.completeExceptionally(error);
                        }
                    });
                    Response<String> response = future.get(15, SECONDS);
                    assertThat(response.content()).contains("Berlin");
                });
    }

    @Test
    void should_create_streaming_language_model_with_default_http_client() throws Exception {

        OpenAiStreamingLanguageModel model = OpenAiStreamingLanguageModel.builder()
                .baseUrl(BASE_URL)
                .apiKey(API_KEY)
                .modelName("gpt-3.5-turbo-instruct")
                .temperature(0.0)
                .build();

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
                future.completeExceptionally(error);
            }
        });
        Response<String> response = future.get(15, SECONDS);
        assertThat(response.content()).contains("Berlin");

    }

    @Test
    void should_provide_embedding_model() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.open-ai.embedding-model.base-url=" + BASE_URL,
                        "langchain4j.open-ai.embedding-model.api-key=" + API_KEY,
                        "langchain4j.open-ai.embedding-model.model-name=text-embedding-3-small"
                )
                .run(context -> {

                    EmbeddingModel model = context.getBean(EmbeddingModel.class);
                    assertThat(model).isInstanceOf(OpenAiEmbeddingModel.class);
                    assertThat(context.getBean(OpenAiEmbeddingModel.class)).isSameAs(model);

                    assertThat(model.embed("hi").content().dimension()).isEqualTo(1536);
                });
    }

    @Test
    void should_create_embedding_model_with_default_http_client() {

        OpenAiEmbeddingModel model = OpenAiEmbeddingModel.builder()
                .baseUrl(BASE_URL)
                .apiKey(API_KEY)
                .modelName("text-embedding-3-small")
                .build();

        assertThat(model.embed("hi").content().dimension()).isEqualTo(1536);
    }

    @Test
    void should_provide_moderation_model() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.open-ai.moderation-model.base-url=" + BASE_URL,
                        "langchain4j.open-ai.moderation-model.api-key=" + API_KEY
                )
                .run(context -> {

                    ModerationModel model = context.getBean(ModerationModel.class);
                    assertThat(model).isInstanceOf(OpenAiModerationModel.class);
                    assertThat(context.getBean(OpenAiModerationModel.class)).isSameAs(model);

                    assertThat(model.moderate("He wants to kill them.").content().flagged()).isTrue();
                });
    }

    @Test
    void should_create_moderation_model_with_default_http_client() {

        OpenAiModerationModel model = OpenAiModerationModel.builder()
                .baseUrl(BASE_URL)
                .apiKey(API_KEY)
                .build();

        assertThat(model.moderate("He wants to kill them.").content().flagged()).isTrue();
    }

    @Test
    void should_provide_image_model() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.open-ai.image-model.base-url=" + BASE_URL,
                        "langchain4j.open-ai.image-model.api-key=" + API_KEY,
                        "langchain4j.open-ai.image-model.model-name=dall-e-2",
                        "langchain4j.open-ai.image-model.size=256x256"
                )
                .run(context -> {

                    ImageModel model = context.getBean(ImageModel.class);
                    assertThat(model).isInstanceOf(OpenAiImageModel.class);
                    assertThat(context.getBean(OpenAiImageModel.class)).isSameAs(model);

                    assertThat(model.generate("banana").content().url()).isNotNull();
                });
    }

    @Test
    void should_create_image_model_with_default_http_client() {

        OpenAiImageModel model = OpenAiImageModel.builder()
                .baseUrl(BASE_URL)
                .apiKey(API_KEY)
                .modelName("dall-e-2")
                .size("256x256")
                .build();

        assertThat(model.generate("banana").content().url()).isNotNull();
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