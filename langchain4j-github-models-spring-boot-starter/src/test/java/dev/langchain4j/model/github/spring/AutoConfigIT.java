package dev.langchain4j.model.github.spring;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.github.GitHubModelsChatModel;
import dev.langchain4j.model.github.GitHubModelsEmbeddingModel;
import dev.langchain4j.model.github.GitHubModelsStreamingChatModel;
import dev.langchain4j.model.github.spring.AutoConfig;
import dev.langchain4j.model.image.ImageModel;
import dev.langchain4j.model.output.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

class AutoConfigIT {

    private static final String GITHUB_TOKEN = System.getenv("GITHUB_TOKEN");

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AutoConfig.class));

    @ParameterizedTest(name = "Model name: {0}")
    @CsvSource({
            "gpt-4o-mini",
            "gpt-4o"
    })
    void should_provide_chat_model(String modelName) {
        contextRunner
                .withPropertyValues(
                        "langchain4j.github-models.chat-model.github-token=" + GITHUB_TOKEN,
                        "langchain4j.github-models.chat-model.model-name=" + modelName,
                        "langchain4j.github-models.chat-model.max-tokens=20"
                )
                .run(context -> {

                    ChatLanguageModel chatLanguageModel = context.getBean(ChatLanguageModel.class);
                    assertThat(chatLanguageModel).isInstanceOf(GitHubModelsChatModel.class);
                    assertThat(chatLanguageModel.generate("What is the capital of France?")).contains("Paris");
                    assertThat(context.getBean(GitHubModelsChatModel.class)).isSameAs(chatLanguageModel);
                });
    }

    @ParameterizedTest(name = "Model name: {0}")
    @CsvSource({
            "gpt-4o-mini",
            "gpt-4o"
    })
    void should_provide_streaming_chat_model(String modelName) {
        contextRunner
                .withPropertyValues(
                        "langchain4j.github-models.streaming-chat-model.github-token=" + GITHUB_TOKEN,
                        "langchain4j.github-models.streaming-chat-model.model-name=" + modelName,
                        "langchain4j.github-models.streaming-chat-model.max-tokens=20",
                        "langchain4j.github-models.streaming-chat-model.timeout=60"
                )
                .run(context -> {

                    StreamingChatLanguageModel streamingChatLanguageModel = context.getBean(StreamingChatLanguageModel.class);
                    assertThat(streamingChatLanguageModel).isInstanceOf(GitHubModelsStreamingChatModel.class);
                    CompletableFuture<Response<AiMessage>> future = new CompletableFuture<>();
                    streamingChatLanguageModel.generate("What is the capital of France?", new StreamingResponseHandler<AiMessage>() {

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
                    assertThat(response.content().text()).contains("Paris");

                    assertThat(context.getBean(GitHubModelsStreamingChatModel.class)).isSameAs(streamingChatLanguageModel);
                });
    }

    @Test
    void should_provide_embedding_model() {
        contextRunner
                .withPropertyValues("langchain4j.github-models.embedding-model.github-token=" + GITHUB_TOKEN,
                        "langchain4j.github-models.embedding-model.model-name=" + "text-embedding-3-small")
                .run(context -> {

                    EmbeddingModel embeddingModel = context.getBean(EmbeddingModel.class);
                    assertThat(embeddingModel).isInstanceOf(GitHubModelsEmbeddingModel.class);
                    assertThat(embeddingModel.embed("hi").content().dimension()).isEqualTo(1536);

                    assertThat(context.getBean(GitHubModelsEmbeddingModel.class)).isSameAs(embeddingModel);
                });
    }
}