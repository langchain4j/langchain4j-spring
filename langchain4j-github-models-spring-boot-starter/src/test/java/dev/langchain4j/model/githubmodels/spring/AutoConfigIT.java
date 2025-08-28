package dev.langchain4j.model.githubmodels.spring;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.github.GitHubModelsChatModel;
import dev.langchain4j.model.github.GitHubModelsEmbeddingModel;
import dev.langchain4j.model.github.GitHubModelsStreamingChatModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

@EnabledIfEnvironmentVariable(named = "GITHUB_TOKEN", matches = ".+")
class AutoConfigIT {

    private static final String GITHUB_TOKEN = System.getenv("GITHUB_TOKEN");

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AutoConfig.class));

    @Test
    void should_provide_chat_model() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.github-models.chat-model.github-token=" + GITHUB_TOKEN,
                        "langchain4j.github-models.chat-model.model-name=gpt-4o-mini",
                        "langchain4j.github-models.chat-model.max-tokens=20"
                )
                .run(context -> {

                    ChatModel chatModel = context.getBean(ChatModel.class);
                    assertThat(chatModel).isInstanceOf(GitHubModelsChatModel.class);
                    assertThat(chatModel.chat("What is the capital of France?")).contains("Paris");
                    assertThat(context.getBean(GitHubModelsChatModel.class)).isSameAs(chatModel);
                });
    }

    @Test
    void should_provide_streaming_chat_model() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.github-models.streaming-chat-model.github-token=" + GITHUB_TOKEN,
                        "langchain4j.github-models.streaming-chat-model.model-name=gpt-4o-mini",
                        "langchain4j.github-models.streaming-chat-model.max-tokens=20"
                )
                .run(context -> {

                    StreamingChatModel streamingChatModel = context.getBean(StreamingChatModel.class);
                    assertThat(streamingChatModel).isInstanceOf(GitHubModelsStreamingChatModel.class);
                    CompletableFuture<ChatResponse> future = new CompletableFuture<>();
                    streamingChatModel.chat("What is the capital of France?", new StreamingChatResponseHandler() {

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
                    ChatResponse response = future.get(120, SECONDS);
                    assertThat(response.aiMessage().text()).contains("Paris");

                    assertThat(context.getBean(GitHubModelsStreamingChatModel.class)).isSameAs(streamingChatModel);
                });
    }

    @Test
    void should_provide_embedding_model() {
        contextRunner
                .withPropertyValues("langchain4j.github-models.embedding-model.github-token=" + GITHUB_TOKEN,
                        "langchain4j.github-models.embedding-model.model-name=text-embedding-3-small")
                .run(context -> {

                    EmbeddingModel embeddingModel = context.getBean(EmbeddingModel.class);
                    assertThat(embeddingModel).isInstanceOf(GitHubModelsEmbeddingModel.class);
                    assertThat(embeddingModel.embed("hi").content().dimension()).isEqualTo(1536);

                    assertThat(context.getBean(GitHubModelsEmbeddingModel.class)).isSameAs(embeddingModel);
                });
    }
}