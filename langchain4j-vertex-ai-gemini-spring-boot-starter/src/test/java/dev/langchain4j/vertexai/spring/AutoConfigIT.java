package dev.langchain4j.vertexai.spring;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.vertexai.VertexAiGeminiChatModel;
import dev.langchain4j.model.vertexai.VertexAiGeminiStreamingChatModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

@EnabledIfEnvironmentVariable(named = "VERTEXAI_PROJECT_ID", matches = ".+")
class AutoConfigIT {

    private static final String PROJECT_ID = System.getenv("VERTEXAI_PROJECT_ID");
    private static final String MODEL = "gemini-1.5-flash";
    private static final String LOCATION = "us-east4";

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AutoConfig.class));

    @Test
    void should_provide_chat_model() {
        // given
        contextRunner
                .withPropertyValues(
                        "langchain4j.vertexai-gemini.chat-model.enabled=true",
                        "langchain4j.vertexai-gemini.chat-model.project=" + PROJECT_ID,
                        "langchain4j.vertexai-gemini.chat-model.modelName=" + MODEL,
                        "langchain4j.vertexai-gemini.chat-model.location=" + LOCATION
                )
                .run(context -> {
                    ChatLanguageModel chatLanguageModel = context.getBean(ChatLanguageModel.class);
                    assertThat(chatLanguageModel).isInstanceOf(VertexAiGeminiChatModel.class);

                    // when
                    String message = chatLanguageModel.generate("What is the capital of Germany?");

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
                        "langchain4j.vertexai-gemini.streaming-chat-model.enabled=true",
                        "langchain4j.vertexai-gemini.streaming-chat-model.project=" + PROJECT_ID,
                        "langchain4j.vertexai-gemini.streaming-chat-model.modelName=gemini-1.5-flash",
                        "langchain4j.vertexai-gemini.streaming-chat-model.location=" + LOCATION
                )
                .run(context -> {

                    StreamingChatLanguageModel streamingChatLanguageModel = context.getBean(StreamingChatLanguageModel.class);
                    assertThat(streamingChatLanguageModel).isInstanceOf(VertexAiGeminiStreamingChatModel.class);
                    CompletableFuture<Response<AiMessage>> future = new CompletableFuture<>();
                    // when
                    streamingChatLanguageModel.generate("What is the capital of Germany?", new StreamingResponseHandler<>() {

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

                    // then
                    assertThat(response.content().text()).contains("Berlin");
                    assertThat(context.getBean(VertexAiGeminiStreamingChatModel.class)).isSameAs(streamingChatLanguageModel);
                });
    }
}