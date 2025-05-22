package dev.langchain4j.vertexai.gemini.spring;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.vertexai.gemini.VertexAiGeminiChatModel;
import dev.langchain4j.model.vertexai.gemini.VertexAiGeminiStreamingChatModel;
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
                    ChatModel chatModel = context.getBean(ChatModel.class);
                    assertThat(chatModel).isInstanceOf(VertexAiGeminiChatModel.class);

                    // when
                    String message = chatModel.chat("What is the capital of Germany?");

                    // then
                    assertThat(message).contains("Berlin");
                    assertThat(context.getBean(VertexAiGeminiChatModel.class)).isSameAs(chatModel);
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

                    StreamingChatModel streamingChatModel = context.getBean(StreamingChatModel.class);
                    assertThat(streamingChatModel).isInstanceOf(VertexAiGeminiStreamingChatModel.class);
                    CompletableFuture<ChatResponse> future = new CompletableFuture<>();
                    // when
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

                    // then
                    assertThat(response.aiMessage().text()).contains("Berlin");
                    assertThat(context.getBean(VertexAiGeminiStreamingChatModel.class)).isSameAs(streamingChatModel);
                });
    }
}