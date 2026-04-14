package dev.langchain4j.openaiofficial.spring;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.image.ImageModel;
import dev.langchain4j.model.openaiofficial.OpenAiOfficialChatModel;
import dev.langchain4j.model.openaiofficial.OpenAiOfficialEmbeddingModel;
import dev.langchain4j.model.openaiofficial.OpenAiOfficialImageModel;
import dev.langchain4j.model.openaiofficial.OpenAiOfficialStreamingChatModel;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

class OpenAiOfficialAutoConfigurationIT {

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(OpenAiOfficialAutoConfiguration.class));

    @Nested
    @EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
    class OpenAi {

        private static final String API_KEY = System.getenv("OPENAI_API_KEY");
        private static final String MODEL_NAME = "gpt-5-mini";

        @Test
        void should_provide_chat_model() {
            contextRunner
                    .withPropertyValues(
                            "langchain4j.open-ai-official.chat-model.api-key=" + API_KEY,
                            "langchain4j.open-ai-official.chat-model.model-name=" + MODEL_NAME,
                            "langchain4j.open-ai-official.chat-model.max-completion-tokens=200"
                    )
                    .run(context -> {

                        ChatModel model = context.getBean(ChatModel.class);
                        assertThat(model).isInstanceOf(OpenAiOfficialChatModel.class);
                        assertThat(context.getBean(OpenAiOfficialChatModel.class)).isSameAs(model);

                        assertThat(model.chat("What is the capital of Germany?")).contains("Berlin");
                    });
        }

        @Test
        void should_provide_chat_model_with_listeners() {
            contextRunner
                    .withPropertyValues(
                            "langchain4j.open-ai-official.chat-model.api-key=" + API_KEY,
                            "langchain4j.open-ai-official.chat-model.model-name=" + MODEL_NAME,
                            "langchain4j.open-ai-official.chat-model.max-completion-tokens=200"
                    )
                    .withUserConfiguration(ListenerConfig.class)
                    .run(context -> {

                        ChatModel model = context.getBean(ChatModel.class);
                        assertThat(model).isInstanceOf(OpenAiOfficialChatModel.class);

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
        void should_provide_streaming_chat_model() {
            contextRunner
                    .withPropertyValues(
                            "langchain4j.open-ai-official.streaming-chat-model.api-key=" + API_KEY,
                            "langchain4j.open-ai-official.streaming-chat-model.model-name=" + MODEL_NAME,
                            "langchain4j.open-ai-official.streaming-chat-model.max-completion-tokens=200"
                    )
                    .run(context -> {

                        StreamingChatModel model = context.getBean(StreamingChatModel.class);
                        assertThat(model).isInstanceOf(OpenAiOfficialStreamingChatModel.class);
                        assertThat(context.getBean(OpenAiOfficialStreamingChatModel.class)).isSameAs(model);

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
    }

    @Nested
    @EnabledIfEnvironmentVariable(named = "MICROSOFT_FOUNDRY_API_KEY", matches = ".+")
    class MicrosoftFoundry {

        private static final String ENDPOINT = System.getenv("MICROSOFT_FOUNDRY_ENDPOINT");
        private static final String API_KEY = System.getenv("MICROSOFT_FOUNDRY_API_KEY");
        private static final String MODEL_NAME = "gpt-5-mini";

        @Test
        void should_provide_chat_model() {
            contextRunner
                    .withPropertyValues(
                            "langchain4j.open-ai-official.chat-model.base-url=" + ENDPOINT,
                            "langchain4j.open-ai-official.chat-model.api-key=" + API_KEY,
                            "langchain4j.open-ai-official.chat-model.model-name=" + MODEL_NAME,
                            "langchain4j.open-ai-official.chat-model.max-completion-tokens=200"
                    )
                    .run(context -> {

                        ChatModel model = context.getBean(ChatModel.class);
                        assertThat(model).isInstanceOf(OpenAiOfficialChatModel.class);
                        assertThat(context.getBean(OpenAiOfficialChatModel.class)).isSameAs(model);

                        assertThat(model.chat("What is the capital of Germany?")).contains("Berlin");
                    });
        }

        @Test
        void should_provide_chat_model_with_listeners() {
            contextRunner
                    .withPropertyValues(
                            "langchain4j.open-ai-official.chat-model.base-url=" + ENDPOINT,
                            "langchain4j.open-ai-official.chat-model.api-key=" + API_KEY,
                            "langchain4j.open-ai-official.chat-model.model-name=" + MODEL_NAME,
                            "langchain4j.open-ai-official.chat-model.max-completion-tokens=200"
                    )
                    .withUserConfiguration(ListenerConfig.class)
                    .run(context -> {

                        ChatModel model = context.getBean(ChatModel.class);
                        assertThat(model).isInstanceOf(OpenAiOfficialChatModel.class);

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
        void should_provide_streaming_chat_model() {
            contextRunner
                    .withPropertyValues(
                            "langchain4j.open-ai-official.streaming-chat-model.base-url=" + ENDPOINT,
                            "langchain4j.open-ai-official.streaming-chat-model.api-key=" + API_KEY,
                            "langchain4j.open-ai-official.streaming-chat-model.model-name=" + MODEL_NAME,
                            "langchain4j.open-ai-official.streaming-chat-model.max-completion-tokens=200"
                    )
                    .run(context -> {

                        StreamingChatModel model = context.getBean(StreamingChatModel.class);
                        assertThat(model).isInstanceOf(OpenAiOfficialStreamingChatModel.class);
                        assertThat(context.getBean(OpenAiOfficialStreamingChatModel.class)).isSameAs(model);

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

    // --- Tests that don't require API calls ---

    @Test
    void should_not_create_beans_when_api_key_is_not_set() {
        contextRunner
                .run(context -> {
                    assertThat(context).doesNotHaveBean(OpenAiOfficialChatModel.class);
                    assertThat(context).doesNotHaveBean(OpenAiOfficialStreamingChatModel.class);
                    assertThat(context).doesNotHaveBean(OpenAiOfficialEmbeddingModel.class);
                    assertThat(context).doesNotHaveBean(OpenAiOfficialImageModel.class);
                });
    }

    @Test
    void should_not_create_chat_model_when_user_provides_own_bean() {
        OpenAiOfficialChatModel customChatModel = mock(OpenAiOfficialChatModel.class);
        contextRunner
                .withBean(OpenAiOfficialChatModel.class, () -> customChatModel)
                .withPropertyValues(
                        "langchain4j.open-ai-official.chat-model.api-key=test-key",
                        "langchain4j.open-ai-official.chat-model.model-name=gpt-5-mini"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(OpenAiOfficialChatModel.class);
                    assertThat(context.getBean(OpenAiOfficialChatModel.class)).isSameAs(customChatModel);
                });
    }

    @Test
    void should_not_create_streaming_chat_model_when_user_provides_own_bean() {
        OpenAiOfficialStreamingChatModel customModel = mock(OpenAiOfficialStreamingChatModel.class);
        contextRunner
                .withBean(OpenAiOfficialStreamingChatModel.class, () -> customModel)
                .withPropertyValues(
                        "langchain4j.open-ai-official.streaming-chat-model.api-key=test-key",
                        "langchain4j.open-ai-official.streaming-chat-model.model-name=gpt-5-mini"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(OpenAiOfficialStreamingChatModel.class);
                    assertThat(context.getBean(OpenAiOfficialStreamingChatModel.class)).isSameAs(customModel);
                });
    }

    @Test
    void should_not_create_embedding_model_when_user_provides_own_bean() {
        OpenAiOfficialEmbeddingModel customModel = mock(OpenAiOfficialEmbeddingModel.class);
        contextRunner
                .withBean(OpenAiOfficialEmbeddingModel.class, () -> customModel)
                .withPropertyValues(
                        "langchain4j.open-ai-official.embedding-model.api-key=test-key",
                        "langchain4j.open-ai-official.embedding-model.model-name=text-embedding-3-small"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(OpenAiOfficialEmbeddingModel.class);
                    assertThat(context.getBean(OpenAiOfficialEmbeddingModel.class)).isSameAs(customModel);
                });
    }

    @Test
    void should_not_create_image_model_when_user_provides_own_bean() {
        OpenAiOfficialImageModel customModel = mock(OpenAiOfficialImageModel.class);
        contextRunner
                .withBean(OpenAiOfficialImageModel.class, () -> customModel)
                .withPropertyValues(
                        "langchain4j.open-ai-official.image-model.api-key=test-key",
                        "langchain4j.open-ai-official.image-model.model-name=gpt-image-1"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(OpenAiOfficialImageModel.class);
                    assertThat(context.getBean(OpenAiOfficialImageModel.class)).isSameAs(customModel);
                });
    }
}
