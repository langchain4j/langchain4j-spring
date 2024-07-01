package dev.langchain4j.qianfan.spring;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.language.LanguageModel;
import dev.langchain4j.model.language.StreamingLanguageModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.qianfan.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @Author: fanjia
 * @createTime: 2024年04月18日 12:54:25
 * @version: 1.0
 * @Description:
 */
@EnabledIfEnvironmentVariable(named = "QIANFAN_API_KEY", matches = ".+")
class AutoConfigIT {

    private static final String API_KEY = System.getenv("QIANFAN_API_KEY");
    private static final String SECRET_KEY = System.getenv("QIANFAN_SECRET_KEY");

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AutoConfig.class));

    @Test
    void should_provide_chat_model() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.qianfan.chat-model.api-key=" + API_KEY,
                        "langchain4j.qianfan.chat-model.secret_key="+SECRET_KEY,
                        "langchain4j.qianfan.chat-model.endpoint=ernie-3.5-8k-1222"
                )
                .run(context -> {

                    ChatLanguageModel chatLanguageModel = context.getBean(ChatLanguageModel.class);
                    assertThat(chatLanguageModel).isInstanceOf(QianfanChatModel.class);
                    assertThat(chatLanguageModel.generate("What is the capital of Germany?")).contains("柏林");

                assertThat(context.getBean(QianfanChatModel.class)).isSameAs(chatLanguageModel);
                });
    }

    @Test
    void should_provide_streaming_chat_model() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.qianfan.streamingChatModel.api-key=" + API_KEY,
                        "langchain4j.qianfan.streamingChatModel.secret_key="+SECRET_KEY,
                        "langchain4j.qianfan.streamingChatModel.endpoint=ernie-3.5-8k-1222"
                )
                .run(context -> {

                    StreamingChatLanguageModel streamingChatLanguageModel = context.getBean(StreamingChatLanguageModel.class);
                    assertThat(streamingChatLanguageModel).isInstanceOf(QianfanStreamingChatModel.class);
                    CompletableFuture<Response<AiMessage>> future = new CompletableFuture<>();
                    streamingChatLanguageModel.generate("德国的首都是哪里?", new StreamingResponseHandler<AiMessage>() {

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
                    assertThat(response.content().text()).isNotNull();

                    assertThat(context.getBean(QianfanStreamingChatModel.class)).isSameAs(streamingChatLanguageModel);
                });
    }

    @Test
    void should_provide_language_model() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.qianfan.languageModel.api-key=" + API_KEY,
                        "langchain4j.qianfan.languageModel.secret_key="+SECRET_KEY,
                        "langchain4j.qianfan.languageModel.modelName=CodeLlama-7b-Instruct",
                        "langchain4j.qianfan.languageModel.logRequests=true",
                        "langchain4j.qianfan.languageModel.logResponses=true"
                )
                .run(context -> {

                    LanguageModel languageModel = context.getBean(LanguageModel.class);
                    assertThat(languageModel).isInstanceOf(QianfanLanguageModel.class);
                    assertThat(languageModel.generate("What is the capital of Germany?").content()).contains("Berlin");
                    assertThat(context.getBean(QianfanLanguageModel.class)).isSameAs(languageModel);
                });
    }

    @Test
    void should_provide_streaming_language_model() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.qianfan.streamingLanguageModel.api-key=" + API_KEY,
                        "langchain4j.qianfan.streamingLanguageModel.secret_key="+SECRET_KEY,
                        "langchain4j.qianfan.streamingLanguageModel.modelName=CodeLlama-7b-Instruct",
                        "langchain4j.qianfan.streamingLanguageModel.logRequests=true",
                        "langchain4j.qianfan.streamingLanguageModel.logResponses=true"
                )
                .run(context -> {

                    StreamingLanguageModel streamingLanguageModel = context.getBean(StreamingLanguageModel.class);
                    assertThat(streamingLanguageModel).isInstanceOf(QianfanStreamingLanguageModel.class);
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
                    assertThat(response.content()).isNotNull();

                    assertThat(context.getBean(QianfanStreamingLanguageModel.class)).isSameAs(streamingLanguageModel);
                });
    }

    @Test
    void should_provide_embedding_model() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.qianfan.embeddingModel.api-key=" + API_KEY,
                        "langchain4j.qianfan.embeddingModel.secret_key="+SECRET_KEY,
                        "langchain4j.qianfan.embeddingModel.modelName=bge-large-zh"
                )
                .run(context -> {

                    EmbeddingModel embeddingModel = context.getBean(EmbeddingModel.class);
                    assertThat(embeddingModel).isInstanceOf(QianfanEmbeddingModel.class);
                    assertThat(embeddingModel.embed("hi").content().dimension()).isEqualTo(1024);

                    assertThat(context.getBean(QianfanEmbeddingModel.class)).isSameAs(embeddingModel);
                });
    }
}