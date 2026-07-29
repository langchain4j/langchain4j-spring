package dev.langchain4j.ollama.spring;

import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.language.LanguageModel;
import dev.langchain4j.model.language.StreamingLanguageModel;
import dev.langchain4j.model.ollama.*;
import dev.langchain4j.model.output.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.BindMode;
import org.testcontainers.ollama.OllamaContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.File;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class AutoConfigIT {

    private static final String MODEL_NAME = "phi";

    private static final String OLLAMA_HOST_CACHE = System.getProperty("user.home") + "/.ollama";
    private static final String OLLAMA_CONTAINER_CACHE = "/root/.ollama";

    @Container
    static OllamaContainer ollama = createOllamaContainer();

    private static OllamaContainer createOllamaContainer() {
        OllamaContainer container = new OllamaContainer(
                DockerImageName.parse("alpine/ollama:latest")
                        .asCompatibleSubstituteFor("ollama/ollama")
        ).withExposedPorts(11434);

        // Mount cache directory if it exists (e.g., in GitHub Actions)
        File cacheDir = new File(OLLAMA_HOST_CACHE);
        if (cacheDir.exists()) {
            container.withFileSystemBind(OLLAMA_HOST_CACHE, OLLAMA_CONTAINER_CACHE, BindMode.READ_WRITE);
        }

        return container;
    }

    @BeforeAll
    static void beforeAll() throws Exception {
        ollama.execInContainer("ollama", "pull", MODEL_NAME);
    }

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AutoConfig.class));

    private static String baseUrl() {
        return ollama.getEndpoint();
    }

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

                    ChatModel chatLanguageModel = context.getBean(ChatModel.class);
                    assertThat(chatLanguageModel).isInstanceOf(OllamaChatModel.class);

                    assertThat(chatLanguageModel.chat("What is the capital of Germany?")).contains("Berlin");

                    assertThat(context.getBean(OllamaChatModel.class)).isSameAs(chatLanguageModel);
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

                    StreamingChatModel streamingChatLanguageModel = context.getBean(StreamingChatModel.class);
                    assertThat(streamingChatLanguageModel).isInstanceOf(OllamaStreamingChatModel.class);
                    CompletableFuture<ChatResponse> future = new CompletableFuture<>();
                    streamingChatLanguageModel.chat("What is the capital of Germany?", new StreamingChatResponseHandler() {

                        @Override
                        public void onPartialResponse(String partialResponse) {
                        }

                        @Override
                        public void onCompleteResponse(ChatResponse response) {
                            future.complete(response);
                        }

                        @Override
                        public void onError(Throwable error) {
                        }
                    });
                    ChatResponse response = future.get(60, SECONDS);
                    assertThat(response.aiMessage().text()).contains("Berlin");

                    assertThat(context.getBean(OllamaStreamingChatModel.class)).isSameAs(streamingChatLanguageModel);
                });
    }

    @Test
    void should_create_streaming_chat_model_with_default_http_client() throws Exception {

        OllamaStreamingChatModel model = OllamaStreamingChatModel.builder()
                .baseUrl(baseUrl())
                .modelName(MODEL_NAME)
                .temperature(0.0)
                // replaced maxTokens with numPredict
                .numPredict(20)
                .build();

        CompletableFuture<ChatResponse> future = new CompletableFuture<>();
        model.chat("What is the capital of Germany?", new StreamingChatResponseHandler() {

            @Override
            public void onPartialResponse(String partialResponse) {
            }

            @Override
            public void onCompleteResponse(ChatResponse response) {
                future.complete(response);
            }

            @Override
            public void onError(Throwable error) {
            }
        });
        ChatResponse response = future.get(60, SECONDS);
        assertThat(response.aiMessage().text()).contains("Berlin");
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
}