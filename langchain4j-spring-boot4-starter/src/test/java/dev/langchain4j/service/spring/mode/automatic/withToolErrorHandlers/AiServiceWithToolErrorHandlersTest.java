package dev.langchain4j.service.spring.mode.automatic.withToolErrorHandlers;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.mock.ChatModelMock;
import dev.langchain4j.service.spring.AiServicesAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class AiServiceWithToolErrorHandlersTest {

    @Test
    void should_wire_tool_execution_error_handler_bean_into_ai_service() {

        // given the tool throws
        ChatModelMock chatModel = chatModelRequesting("{\"arg0\": \"42\"}");

        contextRunner(chatModel).run(context -> {

            AiServiceWithToolErrorHandlers aiService = context.getBean(AiServiceWithToolErrorHandlers.class);

            // when
            aiService.chat("What is the status of order 42?");

            // then the handler decided what the LLM was told, not the exception
            assertThat(chatModel.requests().get(1).messages())
                    .filteredOn(ToolExecutionResultMessage.class::isInstance)
                    .map(ToolExecutionResultMessage.class::cast)
                    .map(ToolExecutionResultMessage::text)
                    .containsExactly("The order service is unavailable.");
            assertThat(chatModel.requests().get(1).messages().toString()).doesNotContain("hunter2");
        });
    }

    @Test
    void should_wire_tool_arguments_error_handler_bean_into_ai_service() {

        // given the LLM generated arguments that cannot be parsed
        ChatModelMock chatModel = chatModelRequesting("{\"arg0\": ");

        contextRunner(chatModel).run(context -> {

            AiServiceWithToolErrorHandlers aiService = context.getBean(AiServiceWithToolErrorHandlers.class);

            // when
            aiService.chat("What is the status of order 42?");

            // then the handler decided what the LLM was told, instead of the invocation failing
            assertThat(chatModel.requests().get(1).messages())
                    .filteredOn(ToolExecutionResultMessage.class::isInstance)
                    .map(ToolExecutionResultMessage.class::cast)
                    .map(ToolExecutionResultMessage::text)
                    .containsExactly("The order ID could not be read.");
        });
    }

    private static ChatModelMock chatModelRequesting(String toolArguments) {
        return ChatModelMock.thatAlwaysResponds(
                AiMessage.from(ToolExecutionRequest.builder()
                        .id("1")
                        .name("orderStatus")
                        .arguments(toolArguments)
                        .build()),
                AiMessage.from("I could not check the order"));
    }

    private static ApplicationContextRunner contextRunner(ChatModel chatModel) {
        return new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(AiServicesAutoConfiguration.class))
                .withBean(ChatModel.class, () -> chatModel)
                .withUserConfiguration(AiServiceWithToolErrorHandlersApplication.class);
    }
}
