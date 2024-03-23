package dev.langchain4j.service.spring.defaultConfig;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.TestStreamingResponseHandler;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.service.spring.AiServicesAutoConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AiServicesAutoConfigIT {

    private static final String API_KEY = System.getenv("OPENAI_API_KEY");

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AiServicesAutoConfig.class));

    @Test
    void should_create_AI_service_that_is_public_interface() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.open-ai.chat-model.api-key=" + API_KEY,
                        "langchain4j.open-ai.chat-model.max-tokens=20",
                        "langchain4j.open-ai.chat-model.temperature=0.0"
                )
                .withUserConfiguration(TestApplication.class)
                .run(context -> {

                    // given
                    PublicAssistant assistant = context.getBean(PublicAssistant.class);

                    // when
                    String answer = assistant.chat("What is the capital of Germany?");

                    // then
                    assertThat(answer).containsIgnoringCase("Berlin");
                });
    }

    @Test
    void should_create_AI_service_that_is_package_private_interface() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.open-ai.chat-model.api-key=" + API_KEY,
                        "langchain4j.open-ai.chat-model.max-tokens=20",
                        "langchain4j.open-ai.chat-model.temperature=0.0"
                )
                .withUserConfiguration(TestApplication.class)
                .run(context -> {

                    // given
                    PackagePrivateAssistant assistant = context.getBean(PackagePrivateAssistant.class);

                    // when
                    String answer = assistant.chat("What is the capital of Germany?");

                    // then
                    assertThat(answer).containsIgnoringCase("Berlin");
                });
    }

    @Test
    void should_create_AI_service_that_is_inner_interface() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.open-ai.chat-model.api-key=" + API_KEY,
                        "langchain4j.open-ai.chat-model.max-tokens=20",
                        "langchain4j.open-ai.chat-model.temperature=0.0"
                )
                .withUserConfiguration(TestApplication.class)
                .run(context -> {

                    // given
                    OuterClass.InnerAssistant assistant = context.getBean(OuterClass.InnerAssistant.class);

                    // when
                    String answer = assistant.chat("What is the capital of Germany?");

                    // then
                    assertThat(answer).containsIgnoringCase("Berlin");
                });
    }

    @Test
    void should_fail_to_create_AI_service_without_annotation() {
        contextRunner
                .withPropertyValues("langchain4j.open-ai.chat-model.api-key=" + API_KEY)
                .withUserConfiguration(TestApplication.class)
                .run(context -> {

                    // when-then
                    assertThatThrownBy(() -> context.getBean(AssistantWithoutAnnotation.class))
                            .isExactlyInstanceOf(NoSuchBeanDefinitionException.class);
                });
    }

    @Test
    void should_create_streaming_AI_service() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.open-ai.streaming-chat-model.api-key=" + API_KEY,
                        "langchain4j.open-ai.streaming-chat-model.max-tokens=20",
                        "langchain4j.open-ai.streaming-chat-model.temperature=0.0"
                )
                .withUserConfiguration(TestApplication.class)
                .run(context -> {

                    // given
                    StreamingAssistant assistant = context.getBean(StreamingAssistant.class);

                    TestStreamingResponseHandler<AiMessage> handler = new TestStreamingResponseHandler<>();

                    // when
                    assistant.chat("What is the capital of Germany?")
                            .onNext(handler::onNext)
                            .onComplete(handler::onComplete)
                            .onError(handler::onError)
                            .start();
                    Response<AiMessage> response = handler.get();

                    // then
                    assertThat(response.content().text()).containsIgnoringCase("Berlin");
                });
    }


    static class ChatMemoryProviderConfig {

        @Bean
        ChatMemoryProvider chatMemoryProvider() {
            return memoryId -> MessageWindowChatMemory.withMaxMessages(10);
        }
    }

    @Test
    void should_create_AI_service_with_chat_memory_provider() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.open-ai.chat-model.api-key=" + API_KEY,
                        "langchain4j.open-ai.chat-model.max-tokens=20",
                        "langchain4j.open-ai.chat-model.temperature=0.0"
                )
                .withUserConfiguration(TestApplication.class)
                .withUserConfiguration(ChatMemoryProviderConfig.class)
                .run(context -> {

                    // given
                    PublicAssistant assistant = context.getBean(PublicAssistant.class);
                    assistant.chat("My name is Klaus");

                    // when
                    String answer = assistant.chat("What is my name?");

                    // then
                    assertThat(answer).containsIgnoringCase("Klaus");
                });
    }

    @Test
    void should_create_AI_service_with_tool_which_is_public_method_in_public_class() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.open-ai.chat-model.api-key=" + API_KEY,
                        "langchain4j.open-ai.chat-model.max-tokens=20",
                        "langchain4j.open-ai.chat-model.temperature=0.0"
                )
                .withUserConfiguration(TestApplication.class)
                .withUserConfiguration(ChatMemoryProviderConfig.class)
                .run(context -> {

                    // given
                    AssistantWithPublicTools assistant = context.getBean(AssistantWithPublicTools.class);

                    // when
                    String answer = assistant.chat("What is the current hour?");

                    // then
                    assertThat(answer).contains(String.valueOf(LocalDateTime.now().getHour()));
                });
    }

    @Test
    void should_create_AI_service_with_tool_that_is_package_private_method_in_package_private_class() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.open-ai.chat-model.api-key=" + API_KEY,
                        "langchain4j.open-ai.chat-model.max-tokens=20",
                        "langchain4j.open-ai.chat-model.temperature=0.0"
                )
                .withUserConfiguration(TestApplication.class)
                .withUserConfiguration(ChatMemoryProviderConfig.class)
                .run(context -> {

                    // given
                    AssistantWithPackagePrivateTools assistant = context.getBean(AssistantWithPackagePrivateTools.class);

                    // when
                    String answer = assistant.chat("What is the current minute?");

                    // then
                    assertThat(answer).contains(String.valueOf(LocalDateTime.now().getMinute()));
                });
    }

    // TODO tools which are not @Beans?
    // TODO negative cases
    // TODO no @AiServices in app, just models
    // TODO @AiServices as inner class?
    // TODO streaming, memory, tools, etc
}