package dev.langchain4j.azure.openai.spring;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.azure.AzureOpenAiChatModel;
import dev.langchain4j.model.azure.AzureOpenAiEmbeddingModel;
import dev.langchain4j.model.azure.AzureOpenAiImageModel;
import dev.langchain4j.model.azure.AzureOpenAiStreamingChatModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.chat.request.json.JsonArraySchema;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonSchema;
import dev.langchain4j.model.chat.request.json.JsonStringSchema;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.image.ImageModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.concurrent.CompletableFuture;

import static dev.langchain4j.data.message.UserMessage.userMessage;
import static dev.langchain4j.model.chat.request.ResponseFormatType.JSON;
import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

class AutoConfigIT {

    private static final String AZURE_OPENAI_KEY = System.getenv("AZURE_OPENAI_KEY");
    private static final String AZURE_OPENAI_ENDPOINT = System.getenv("AZURE_OPENAI_ENDPOINT");
    private static final String NO_AZURE_OPENAI_KEY = System.getenv("OPENAI_API_KEY");

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AutoConfig.class));

    @ParameterizedTest(name = "Deployment name: {0}")
    @CsvSource({
            "gpt-4o-mini",
            "gpt-4o"
    })
    @EnabledIfEnvironmentVariable(named = "AZURE_OPENAI_KEY", matches = ".+")
    void should_provide_chat_model(String deploymentName) {
        contextRunner
                .withPropertyValues(
                        "langchain4j.azure-open-ai.chat-model.api-key=" + AZURE_OPENAI_KEY,
                        "langchain4j.azure-open-ai.chat-model.endpoint=" + AZURE_OPENAI_ENDPOINT,
                        "langchain4j.azure-open-ai.chat-model.deployment-name=" + deploymentName,
                        "langchain4j.azure-open-ai.chat-model.max-tokens=20"
                )
                .run(context -> {

                    ChatModel chatModel = context.getBean(ChatModel.class);
                    assertThat(chatModel).isInstanceOf(AzureOpenAiChatModel.class);
                    assertThat(chatModel.chat("What is the capital of Germany?")).contains("Berlin");
                    assertThat(context.getBean(AzureOpenAiChatModel.class)).isSameAs(chatModel);
                });
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "AZURE_OPENAI_KEY", matches = ".+")
    void should_provide_chat_model_with_listeners() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.azure-open-ai.chat-model.api-key=" + AZURE_OPENAI_KEY,
                        "langchain4j.azure-open-ai.chat-model.endpoint=" + AZURE_OPENAI_ENDPOINT,
                        "langchain4j.azure-open-ai.chat-model.deployment-name=gpt-4o-mini",
                        "langchain4j.azure-open-ai.chat-model.max-tokens=20"
                )
                .withUserConfiguration(ListenerConfig.class)
                .run(context -> {

                    ChatModel chatModel = context.getBean(ChatModel.class);
                    assertThat(chatModel).isInstanceOf(AzureOpenAiChatModel.class);
                    assertThat(chatModel.chat("What is the capital of Germany?")).contains("Berlin");
                    assertThat(context.getBean(AzureOpenAiChatModel.class)).isSameAs(chatModel);

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

    @ParameterizedTest(name = "Deployment name: {0}")
    @CsvSource({
            "gpt-4o-mini"
    })
    @EnabledIfEnvironmentVariable(named = "AZURE_OPENAI_KEY", matches = ".+")
    void should_provide_chat_model_with_json_schema(String deploymentName) {
        contextRunner
                .withPropertyValues(
                        "langchain4j.azure-open-ai.chat-model.api-key=" + AZURE_OPENAI_KEY,
                        "langchain4j.azure-open-ai.chat-model.endpoint=" + AZURE_OPENAI_ENDPOINT,
                        "langchain4j.azure-open-ai.chat-model.deployment-name=" + deploymentName,
                        "langchain4j.azure-open-ai.chat-model.strict-json-schema=true"
                )
                .run(context -> {

                    ChatModel chatModel = context.getBean(ChatModel.class);

                    ChatRequest chatRequest = ChatRequest.builder()
                            .messages(singletonList(userMessage("Julien likes blue, white and red")))
                            .responseFormat(ResponseFormat.builder()
                                    .type(JSON)
                                    .jsonSchema(JsonSchema.builder()
                                            .name("Person")
                                            .rootElement(JsonObjectSchema.builder()
                                                    .addStringProperty("name")
                                                    .addProperty("favouriteColors", JsonArraySchema.builder()
                                                            .items(new JsonStringSchema())
                                                            .build())
                                                    .required("name", "favouriteColors")
                                                    .build())
                                            .build())
                                    .build())
                            .build();

                    assertThat(chatModel).isInstanceOf(AzureOpenAiChatModel.class);
                    AiMessage aiMessage = chatModel.chat(chatRequest).aiMessage();
                    assertThat(aiMessage.text()).contains("{\"name\":\"Julien\",\"favouriteColors\":[\"blue\",\"white\",\"red\"]}");
                    assertThat(context.getBean(AzureOpenAiChatModel.class)).isSameAs(chatModel);
                });
    }

    @ParameterizedTest(name = "Deployment name: {0}")
    @CsvSource({
            "gpt-3.5-turbo"
    })
    @EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
    void should_provide_chat_model_no_azure(String deploymentName) {
        contextRunner
                .withPropertyValues(
                        "langchain4j.azure-open-ai.chat-model.non-azure-api-key=" + NO_AZURE_OPENAI_KEY,
                        "langchain4j.azure-open-ai.chat-model.deployment-name=" + deploymentName,
                        "langchain4j.azure-open-ai.chat-model.max-tokens=20",
                        "langchain4j.azure-open-ai.chat-model.timeout=60"
                )
                .run(context -> {

                    ChatModel chatModel = context.getBean(ChatModel.class);
                    assertThat(chatModel).isInstanceOf(AzureOpenAiChatModel.class);
                    assertThat(chatModel.chat("What is the capital of Germany?")).contains("Berlin");

                    assertThat(context.getBean(AzureOpenAiChatModel.class)).isSameAs(chatModel);
                });
    }

    @ParameterizedTest(name = "Deployment name: {0}")
    @CsvSource({
            "gpt-4o-mini",
            "gpt-4o"
    })
    @EnabledIfEnvironmentVariable(named = "AZURE_OPENAI_KEY", matches = ".+")
    void should_provide_streaming_chat_model(String deploymentName) {
        contextRunner
                .withPropertyValues(
                        "langchain4j.azure-open-ai.streaming-chat-model.api-key=" + AZURE_OPENAI_KEY,
                        "langchain4j.azure-open-ai.streaming-chat-model.endpoint=" + AZURE_OPENAI_ENDPOINT,
                        "langchain4j.azure-open-ai.streaming-chat-model.deployment-name=" + deploymentName,
                        "langchain4j.azure-open-ai.streaming-chat-model.max-tokens=20",
                        "langchain4j.azure-open-ai.streaming-chat-model.timeout=60"
                )
                .run(context -> {

                    StreamingChatModel streamingChatModel = context.getBean(StreamingChatModel.class);
                    assertThat(streamingChatModel).isInstanceOf(AzureOpenAiStreamingChatModel.class);
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

                    assertThat(context.getBean(AzureOpenAiStreamingChatModel.class)).isSameAs(streamingChatModel);
                });
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "AZURE_OPENAI_KEY", matches = ".+")
    void should_provide_streaming_chat_model_with_listeners() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.azure-open-ai.streaming-chat-model.api-key=" + AZURE_OPENAI_KEY,
                        "langchain4j.azure-open-ai.streaming-chat-model.endpoint=" + AZURE_OPENAI_ENDPOINT,
                        "langchain4j.azure-open-ai.streaming-chat-model.deployment-name=gpt-4o-mini",
                        "langchain4j.azure-open-ai.streaming-chat-model.max-tokens=20",
                        "langchain4j.azure-open-ai.streaming-chat-model.timeout=60"
                )
                .withUserConfiguration(ListenerConfig.class)
                .run(context -> {

                    StreamingChatModel streamingChatModel = context.getBean(StreamingChatModel.class);
                    assertThat(streamingChatModel).isInstanceOf(AzureOpenAiStreamingChatModel.class);
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

                    assertThat(context.getBean(AzureOpenAiStreamingChatModel.class)).isSameAs(streamingChatModel);

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
    @EnabledIfEnvironmentVariable(named = "AZURE_OPENAI_KEY", matches = ".+")
    void should_provide_embedding_model() {
        contextRunner
                .withPropertyValues("langchain4j.azure-open-ai.embedding-model.api-key=" + AZURE_OPENAI_KEY,
                        "langchain4j.azure-open-ai.embedding-model.endpoint=" + AZURE_OPENAI_ENDPOINT,
                        "langchain4j.azure-open-ai.embedding-model.deployment-name=" + "text-embedding-ada-002")
                .run(context -> {

                    EmbeddingModel embeddingModel = context.getBean(EmbeddingModel.class);
                    assertThat(embeddingModel).isInstanceOf(AzureOpenAiEmbeddingModel.class);
                    assertThat(embeddingModel.embed("hi").content().dimension()).isEqualTo(1536);

                    assertThat(context.getBean(AzureOpenAiEmbeddingModel.class)).isSameAs(embeddingModel);
                });
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "AZURE_OPENAI_KEY", matches = ".+")
    void should_provide_image_model() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.azure-open-ai.image-model.api-key=" + AZURE_OPENAI_KEY,
                        "langchain4j.azure-open-ai.image-model.endpoint=" + AZURE_OPENAI_ENDPOINT,
                        "langchain4j.azure-open-ai.image-model.deployment-name=" + "dall-e-3-30"
                )
                .run(context -> {

                    ImageModel imageModel = context.getBean(ImageModel.class);
                    assertThat(imageModel).isInstanceOf(AzureOpenAiImageModel.class);
                    assertThat(imageModel.generate("coffee").content().url()).isNotNull();

                    assertThat(context.getBean(AzureOpenAiImageModel.class)).isSameAs(imageModel);
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