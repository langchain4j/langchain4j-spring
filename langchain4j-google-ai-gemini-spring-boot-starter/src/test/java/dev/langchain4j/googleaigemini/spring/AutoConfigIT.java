package dev.langchain4j.googleaigemini.spring;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.Tokenizer;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiStreamingChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiTokenizer;
import dev.langchain4j.model.output.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

class AutoConfigIT {

    private static final String API_KEY = System.getenv("GOOGLE_API_KEY");
    private static final String MODEL = "gemini-1.5-flash";

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AutoConfig.class));

    @Test
    void should_provide_chat_model() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.google-ai-gemini.chat-model.api-key="+API_KEY,
                        "langchain4j.google-ai-gemini.chat-model.model-name="+ MODEL,
                        "langchain4j.google-ai-gemini.chat-model.max-output-tokens=20"
                )
                .run(context -> {
                    ChatLanguageModel chatLanguageModel = context.getBean(ChatLanguageModel.class);
                    assertThat(chatLanguageModel).isInstanceOf(GoogleAiGeminiChatModel.class);
                    assertThat(chatLanguageModel.generate("What is the capital of Germany?")).contains("Berlin");
                    assertThat(context.getBean(GoogleAiGeminiChatModel.class)).isSameAs(chatLanguageModel);
                });
    }

    @Test
    void should_provide_streaming_chat_model() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.google-ai-gemini.streaming-chat-model.api-key=" + API_KEY,
                        "langchain4j.google-ai-gemini.streaming-chat-model.model-name="+ MODEL,
                        "langchain4j.google-ai-gemini.streaming-chat-model.max-tokens=20"
                )
                .run(context -> {

                    StreamingChatLanguageModel streamingChatLanguageModel = context.getBean(StreamingChatLanguageModel.class);
                    assertThat(streamingChatLanguageModel).isInstanceOf(GoogleAiGeminiStreamingChatModel.class);
                    CompletableFuture<Response<AiMessage>> future = new CompletableFuture<>();
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
                    assertThat(response.content().text()).contains("Berlin");

                    assertThat(context.getBean(GoogleAiGeminiStreamingChatModel.class)).isSameAs(streamingChatLanguageModel);
                });
    }

    @Test
    void should_provide_tokenizer() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.google-ai-gemini.tokenizer.api-key=" + API_KEY,
                        "langchain4j.google-ai-gemini.tokenizer.model-name=" + MODEL,
                        "langchain4j.google-ai-gemini.language-model.max-retries=3"
                )
                .run(context -> {

                    Tokenizer tokenizer = context.getBean(Tokenizer.class);
                    assertThat(tokenizer).isInstanceOf(GoogleAiGeminiTokenizer.class);
                    assertThat(tokenizer.estimateTokenCountInText("What is the capital of Germany?")).isPositive();

                    assertThat(context.getBean(GoogleAiGeminiTokenizer.class)).isSameAs(tokenizer);
                });
    }
    
    @Test
    void should_not_create_bean_when_no_api_key() {
        contextRunner
                .run(context -> {
                    assertThat(context).doesNotHaveBean(GoogleAiGeminiChatModel.class);
                    assertThat(context).doesNotHaveBean(GoogleAiGeminiStreamingChatModel.class);
                    assertThat(context).doesNotHaveBean(GoogleAiGeminiTokenizer.class);
                });
    }

}