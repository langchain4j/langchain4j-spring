package dev.langchain4j.googleaigemini.spring;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiEmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiStreamingChatModel;
import dev.langchain4j.model.output.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

@EnabledIfEnvironmentVariable(named = "GOOGLE_AI_GEMINI_API_KEY", matches = ".+")
class AutoConfigIT {

    private static final String API_KEY = System.getenv("GOOGLE_AI_GEMINI_API_KEY");

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AutoConfig.class));

    @Test
    void provide_chat_model() {
        contextRunner.withPropertyValues(
                        "langchain4j.google-ai-gemini.chat-model.api-key=" + API_KEY,
                        "langchain4j.google-ai-gemini.chat-model.model-name=gemini-2.0-flash-exp"
                )
                .run(context -> {
                    ChatModel chatModel = context.getBean(ChatModel.class);
                    assertThat(context.getBean(GoogleAiGeminiChatModel.class)).isSameAs(chatModel);

                    String response = chatModel.chat("What is the capital of India");
                    assertThat(response).contains("Delhi");

                    String newResponse = chatModel.chat("Calculate the Fibonacci of 22 and give me the result as an integer value along with the code. ");
                    assertThat(newResponse).contains("17711");
                });
    }

    @Test
    void provide_chat_model_with_property_values() {
        contextRunner.withPropertyValues(
                        "langchain4j.google-ai-gemini.chat-model.api-key=" + API_KEY,
                        "langchain4j.google-ai-gemini.enabled=true",
                        "langchain4j.google-ai-gemini.chatModel.enabled=true",
                        "langchain4j.google-ai-gemini.chatModel.modelName=gemini-2.0-flash-exp",
                        "langchain4j.google-ai-gemini.chatModel.temperature=0.7",
                        "langchain4j.google-ai-gemini.chatModel.topP=0.9",
                        "langchain4j.google-ai-gemini.chatModel.topK=40",
                        "langchain4j.google-ai-gemini.chatModel.maxOutputTokens=800",
                        "langchain4j.google-ai-gemini.chatModel.safetySetting.HARM_CATEGORY_DANGEROUS_CONTENT=BLOCK_LOW_AND_ABOVE",
                        "langchain4j.google-ai-gemini.chatModel.functionCallingConfig.gemini-mode=ANY",
                        "langchain4j.google-ai-gemini.chatModel.functionCallingConfig.allowed-function-names=allowCodeExecution,includeCodeExecutionOutput"
                )
                .run(context -> {
                    ChatModel chatModel = context.getBean(ChatModel.class);
                    assertThat(chatModel).isInstanceOf(ChatModel.class);
                    String response = chatModel.chat("What is the capital of India");
                    assertThat(response).contains("Delhi");
                    String newResponse = chatModel.chat("Calculate the Fibonacci of 22 and give me the result as an integer value along with the code. ");
                    assertThat(newResponse).contains("17711");
                });
    }

    @Test
    void provide_streaming_chat_model() {
        contextRunner.withPropertyValues(
                        "langchain4j.google-ai-gemini.streaming-chat-model.api-key=" + API_KEY,
                        "langchain4j.google-ai-gemini.streaming-chat-model.model-name=gemini-2.0-flash-exp"
                )
                .run(context -> {
                    StreamingChatModel streamingChatModel = context.getBean(StreamingChatModel.class);
                    assertThat(context.getBean(GoogleAiGeminiStreamingChatModel.class)).isSameAs(streamingChatModel);

                    CompletableFuture<ChatResponse> future = new CompletableFuture<>();
                    streamingChatModel.chat("What is the capital of India", new StreamingChatResponseHandler() {

                        @Override
                        public void onPartialResponse(String partialResponse) {
                        }

                        @Override
                        public void onCompleteResponse(ChatResponse completeResponse) {
                            future.complete(completeResponse);
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            future.completeExceptionally(throwable);
                        }
                    });

                    ChatResponse response = future.get(60, SECONDS);
                    assertThat(response.aiMessage().text()).contains("Delhi");
                });
    }

    @Test
    void provide_streaming_chat_model_with_property_values() {
        contextRunner.withPropertyValues(
                        "langchain4j.google-ai-gemini.streaming-chat-model.api-key=" + API_KEY,
                        "langchain4j.google-ai-gemini.enabled=true",
                        "langchain4j.google-ai-gemini.streamingChatModel.enabled=true",
                        "langchain4j.google-ai-gemini.streamingChatModel.modelName=gemini-2.0-flash-exp",
                        "langchain4j.google-ai-gemini.streamingChatModel.temperature=0.7",
                        "langchain4j.google-ai-gemini.streamingChatModel.topP=0.9",
                        "langchain4j.google-ai-gemini.streamingChatModel.topK=40",
                        "langchain4j.google-ai-gemini.streamingChatModel.maxOutputTokens=400",
                        "langchain4j.google-ai-gemini.streamingChatModel.safetySetting.HARM_CATEGORY_SEXUALLY_EXPLICIT=HARM_BLOCK_THRESHOLD_UNSPECIFIED",
                        "langchain4j.google-ai-gemini.streamingChatModel.functionCallingConfig.gemini-mode=ANY",
                        "langchain4j.google-ai-gemini.streamingChatModel.functionCallingConfig.allowed-function-names=allowCodeExecution,includeCodeExecutionOutput"
                )
                .run(context -> {
                    StreamingChatModel streamingChatModel = context.getBean(StreamingChatModel.class);
                    assertThat(streamingChatModel).isInstanceOf(StreamingChatModel.class);
                    CompletableFuture<ChatResponse> future = new CompletableFuture<>();
                    streamingChatModel.chat("What is the capital of India", new StreamingChatResponseHandler() {

                        @Override
                        public void onPartialResponse(String partialResponse) {
                        }

                        @Override
                        public void onCompleteResponse(ChatResponse completeResponse) {
                            future.complete(completeResponse);
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            future.completeExceptionally(throwable);
                        }
                    });
                    ChatResponse response = future.get(60, SECONDS);
                    assertThat(response.aiMessage().text()).contains("Delhi");
                });
    }

    @Test
    void provide_embedding_model() {
        contextRunner.withPropertyValues(
                "langchain4j.google-ai-gemini.embedding-model.apiKey=" + API_KEY,
                "langchain4j.google-ai-gemini.embedding-model.model-name=text-embedding-004"
        ).run(context -> {
            EmbeddingModel embeddingModel = context.getBean(EmbeddingModel.class);
            assertThat(context.getBean(GoogleAiEmbeddingModel.class)).isSameAs(embeddingModel);

            Response<Embedding> response = embeddingModel.embed("Hi, I live in India");
            assertThat(response.content().dimension()).isEqualTo(768);
        });
    }

    @Test
    void provide_embedding_model_with_property_values() {
        contextRunner.withPropertyValues(
                "langchain4j.google-ai-gemini.embedding-model.apiKey=" + API_KEY,
                "langchain4j.google-ai-gemini.embeddingModel.enabled=true",
                "langchain4j.google-ai-gemini.embeddingModel.titleMetadataKey=title-key",
                "langchain4j.google-ai-gemini.embeddingModel.modelName=text-embedding-004",
                "langchain4j.google-ai-gemini.embeddingModel.logRequestsAndResponses=true",
                "langchain4j.google-ai-gemini.embeddingModel.maxRetries=3",
                "langchain4j.google-ai-gemini.embeddingModel.outputDimensionality=512",
                "langchain4j.google-ai-gemini.embeddingModel.taskType=CLASSIFICATION",
                "langchain4j.google-ai-gemini.embeddingModel.timeout=PT30S"
        ).run(context -> {
            EmbeddingModel embeddingModel = context.getBean(GoogleAiEmbeddingModel.class);
            assertThat(embeddingModel).isInstanceOf(EmbeddingModel.class);
            assertThat(context.getBean(GoogleAiEmbeddingModel.class)).isSameAs(embeddingModel);
            Response<Embedding> response = embeddingModel.embed("Hi, I live in India");
            assertThat(response.content().dimension()).isEqualTo(512);
        });
    }
}