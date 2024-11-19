package dev.langchain4j.googleaigemini.spring;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiStreamingChatModel;
import dev.langchain4j.model.output.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

class AutoConfigIT {

    private static final String API_KEY =System.getenv("GOOGLE_AI_GEMINI_API_KEY");

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AutoConfig.class));

    @Test
    void provide_chat_model() {
        contextRunner.withPropertyValues(
                        "langchain4j.google-ai-gemini.apiKey=" + API_KEY,
                        "langchain4j.google-ai-gemini.enabled=true",
                        "langchain4j.google-ai-gemini.chatModel.enabled=true",
                        "langchain4j.google-ai-gemini.chatModel.modelName=gemini-1.0-pro  ",
                        "langchain4j.google-ai-gemini.chatModel.temperature=0.7",
                        "langchain4j.google-ai-gemini.chatModel.topP=0.9",
                        "langchain4j.google-ai-gemini.chatModel.topK=40",
                        "langchain4j.google-ai-gemini.chatModel.maxOutputTokens=100",
                        "langchain4j.google-ai-gemini.safetySetting.geminiHarmCategory=HARM_CATEGORY_SEXUALLY_EXPLICIT",
                        "langchain4j.google-ai-gemini.safetySetting.geminiHarmBlockThreshold=HARM_BLOCK_THRESHOLD_UNSPECIFIED",
                        "langchain4j.google-ai-gemini.functionCallingConfig.geminiMode=ANY",
                        "langchain4j.google-ai-gemini.functionCallingConfig.allowedFunctionNames=allowCodeExecution,includeCodeExecutionOutput"
                )
                .run(context -> {
                    ChatLanguageModel chatLanguageModel = context.getBean(ChatLanguageModel.class);
                    assertThat(chatLanguageModel).isInstanceOf(ChatLanguageModel.class);
                    assertThat(context.getBean(GoogleAiGeminiChatModel.class)).isSameAs(chatLanguageModel);
                    String response = chatLanguageModel.generate("What is the capital of India");
                    assertThat(response).contains("Delhi");
                    String newResponse = chatLanguageModel.generate("Calculate the Fibonacci of 22 and return the result as an integer value along with the code. ");
                    assertThat(newResponse).contains("17711");
                });
    }

    @Test
    void provide_streaming_chat_model() {
        contextRunner.withPropertyValues(
                        "langchain4j.google-ai-gemini.apiKey=" + API_KEY,
                        "langchain4j.google-ai-gemini.enabled=true",
                        "langchain4j.google-ai-gemini.streamingChatModel.enabled=true",
                        "langchain4j.google-ai-gemini.streamingChatModel.modelName=gemini-1.5-flash",
                        "langchain4j.google-ai-gemini.streamingChatModel.temperature=0.7",
                        "langchain4j.google-ai-gemini.streamingChatModel.topP=0.9",
                        "langchain4j.google-ai-gemini.streamingChatModel.topK=40",
                        "langchain4j.google-ai-gemini.streamingChatModel.maxOutputTokens=100"
                )
                .run(context -> {
                    StreamingChatLanguageModel streamingChatLanguageModel = context.getBean(StreamingChatLanguageModel.class);
                    assertThat(streamingChatLanguageModel).isInstanceOf(StreamingChatLanguageModel.class);
                    assertThat(context.getBean(GoogleAiGeminiStreamingChatModel.class)).isSameAs(streamingChatLanguageModel);
                    CompletableFuture<Response<AiMessage>> future = new CompletableFuture<>();
                    streamingChatLanguageModel.generate("What is the capital of India", new StreamingResponseHandler<>() {
                        @Override
                        public void onNext(String s) {}

                        @Override
                        public void onComplete(Response<AiMessage> response) {
                            future.complete(response);
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            future.completeExceptionally(throwable);
                        }
                    });

                    Response<AiMessage> response = future.get(60, SECONDS);
                    assertThat(response.content().text()).contains("Delhi");
                });
    }
}