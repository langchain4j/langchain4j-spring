package dev.langchain4j.azure.openai.spring;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.azure.AzureOpenAiChatModel;
import dev.langchain4j.model.azure.AzureOpenAiEmbeddingModel;
import dev.langchain4j.model.azure.AzureOpenAiImageModel;
import dev.langchain4j.model.azure.AzureOpenAiStreamingChatModel;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.chat.request.json.JsonArraySchema;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonSchema;
import dev.langchain4j.model.chat.request.json.JsonStringSchema;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.image.ImageModel;
import dev.langchain4j.model.output.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static dev.langchain4j.data.message.UserMessage.userMessage;
import static dev.langchain4j.model.chat.request.ResponseFormatType.JSON;
import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

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
    void should_provide_chat_model(String deploymentName) {
        contextRunner
                .withPropertyValues(
                        "langchain4j.azure-open-ai.chat-model.api-key=" + AZURE_OPENAI_KEY,
                        "langchain4j.azure-open-ai.chat-model.endpoint=" + AZURE_OPENAI_ENDPOINT,
                        "langchain4j.azure-open-ai.chat-model.deployment-name=" + deploymentName,
                        "langchain4j.azure-open-ai.chat-model.max-tokens=20"
                )
                .run(context -> {

                    ChatLanguageModel chatLanguageModel = context.getBean(ChatLanguageModel.class);
                    assertThat(chatLanguageModel).isInstanceOf(AzureOpenAiChatModel.class);
                    assertThat(chatLanguageModel.generate("What is the capital of Germany?")).contains("Berlin");
                    assertThat(context.getBean(AzureOpenAiChatModel.class)).isSameAs(chatLanguageModel);
                });
    }

    class Person {

        String name;
        List<String> favouriteColors;
    }

    @ParameterizedTest(name = "Deployment name: {0}")
    @CsvSource({
            "gpt-4o-mini"
    })
    void should_provide_chat_model_with_json_schema(String deploymentName) {
        contextRunner
                .withPropertyValues(
                        "langchain4j.azure-open-ai.chat-model.api-key=" + AZURE_OPENAI_KEY,
                        "langchain4j.azure-open-ai.chat-model.endpoint=" + AZURE_OPENAI_ENDPOINT,
                        "langchain4j.azure-open-ai.chat-model.deployment-name=" + deploymentName,
                        "langchain4j.azure-open-ai.chat-model.strict-json-schema=true"
                )
                .run(context -> {

                    ChatLanguageModel chatLanguageModel = context.getBean(ChatLanguageModel.class);

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

                    assertThat(chatLanguageModel).isInstanceOf(AzureOpenAiChatModel.class);
                    AiMessage aiMessage = chatLanguageModel.chat(chatRequest).aiMessage();
                    assertThat(aiMessage.text()).contains("{\"name\":\"Julien\",\"favouriteColors\":[\"blue\",\"white\",\"red\"]}");
                    assertThat(context.getBean(AzureOpenAiChatModel.class)).isSameAs(chatLanguageModel);
                });
    }

    @ParameterizedTest(name = "Deployment name: {0}")
    @CsvSource({
            "gpt-3.5-turbo"
    })
    void should_provide_chat_model_no_azure(String deploymentName) {
        contextRunner
                .withPropertyValues(
                        "langchain4j.azure-open-ai.chat-model.non-azure-api-key=" + NO_AZURE_OPENAI_KEY,
                        "langchain4j.azure-open-ai.chat-model.deployment-name=" + deploymentName,
                        "langchain4j.azure-open-ai.chat-model.max-tokens=20",
                        "langchain4j.azure-open-ai.chat-model.timeout=60"
                )
                .run(context -> {

                    ChatLanguageModel chatLanguageModel = context.getBean(ChatLanguageModel.class);
                    assertThat(chatLanguageModel).isInstanceOf(AzureOpenAiChatModel.class);
                    assertThat(chatLanguageModel.generate("What is the capital of Germany?")).contains("Berlin");

                    assertThat(context.getBean(AzureOpenAiChatModel.class)).isSameAs(chatLanguageModel);
                });
    }

    @ParameterizedTest(name = "Deployment name: {0}")
    @CsvSource({
            "gpt-4o-mini",
            "gpt-4o"
    })
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

                    StreamingChatLanguageModel streamingChatLanguageModel = context.getBean(StreamingChatLanguageModel.class);
                    assertThat(streamingChatLanguageModel).isInstanceOf(AzureOpenAiStreamingChatModel.class);
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

                    assertThat(context.getBean(AzureOpenAiStreamingChatModel.class)).isSameAs(streamingChatLanguageModel);
                });
    }

    @Test
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
                    assertThat(imageModel.generate("banana").content().url()).isNotNull();

                    assertThat(context.getBean(AzureOpenAiImageModel.class)).isSameAs(imageModel);
                });
    }
}