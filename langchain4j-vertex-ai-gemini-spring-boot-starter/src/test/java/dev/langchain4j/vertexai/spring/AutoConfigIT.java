package dev.langchain4j.vertexai.spring;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.vertexai.VertexAiGeminiChatModel;
import dev.langchain4j.model.vertexai.VertexAiGeminiStreamingChatModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

@EnabledIfEnvironmentVariable(named = "GCP_PROJECT_ID", matches = ".+")
@EnabledIfEnvironmentVariable(named = "GCP_LOCATION", matches = ".+")
class AutoConfigIT {

    private static final String PROJECT_ID = System.getenv("GCP_PROJECT_ID");
    private static final String LOCATION = System.getenv("GCP_LOCATION");
    private static final String MODEL = "gemini-1.5-flash";

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AutoConfig.class));

    @Test
    void should_provide_chat_model() {
        // given
        contextRunner
                .withPropertyValues(
                        "langchain4j.vertex-ai-gemini.chat-model.enabled=true",
                        "langchain4j.vertex-ai-gemini.chat-model.project=" + PROJECT_ID,
                        "langchain4j.vertex-ai-gemini.chat-model.modelName=" + MODEL,
                        "langchain4j.vertex-ai-gemini.chat-model.location=" + LOCATION
                )
                .run(context -> {
                    ChatLanguageModel chatLanguageModel = context.getBean(ChatLanguageModel.class);
                    assertThat(chatLanguageModel).isInstanceOf(VertexAiGeminiChatModel.class);

                    // when
                    String message = chatLanguageModel.chat("What is the capital of Germany?");

                    // then
                    assertThat(message).contains("Berlin");
                    assertThat(context.getBean(VertexAiGeminiChatModel.class)).isSameAs(chatLanguageModel);
                });
    }

    @Test
    void should_provide_streaming_chat_model() {
        // given
        contextRunner
                .withPropertyValues(
                        "langchain4j.vertex-ai-gemini.streaming-chat-model.enabled=true",
                        "langchain4j.vertex-ai-gemini.streaming-chat-model.project=" + PROJECT_ID,
                        "langchain4j.vertex-ai-gemini.streaming-chat-model.modelName=" + MODEL,
                        "langchain4j.vertex-ai-gemini.streaming-chat-model.location=" + LOCATION
                )
                .run(context -> {

                    StreamingChatLanguageModel streamingChatLanguageModel = context.getBean(StreamingChatLanguageModel.class);
                    assertThat(streamingChatLanguageModel).isInstanceOf(VertexAiGeminiStreamingChatModel.class);
                    CompletableFuture<ChatResponse> future = new CompletableFuture<>();
                    // when
                    streamingChatLanguageModel.chat("What is the capital of Germany?", new StreamingChatResponseHandler() {

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

                    // then
                    assertThat(response.aiMessage().text()).contains("Berlin");
                    assertThat(context.getBean(VertexAiGeminiStreamingChatModel.class)).isSameAs(streamingChatLanguageModel);
                });
    }
}