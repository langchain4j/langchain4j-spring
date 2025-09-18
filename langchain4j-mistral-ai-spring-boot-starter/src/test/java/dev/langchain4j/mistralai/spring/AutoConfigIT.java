package dev.langchain4j.mistralai.spring;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
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

    @AfterEach
    void afterEach() throws InterruptedException {
        Thread.sleep(10_000);
    }

    @Test
    void should_provide_chat_model() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.mistral-ai.chat-model.api-key=" + API_KEY,
                        "langchain4j.mistral-ai.chat-model.model-name=ministral-3b-latest",
                        "langchain4j.mistral-ai.chat-model.max-tokens=20")
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
                        "langchain4j.mistral-ai.streaming-chat-model.max-tokens=20")
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
                        "langchain4j.mistral-ai.fim-model.max-tokens=50")
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
                        "langchain4j.mistral-ai.streaming-fim-model.max-tokens=50")
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
                    Response<String> response = future.get(60, SECONDS);
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
}
