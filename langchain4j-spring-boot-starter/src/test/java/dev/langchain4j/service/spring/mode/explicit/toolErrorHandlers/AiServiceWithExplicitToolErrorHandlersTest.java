package dev.langchain4j.service.spring.mode.explicit.toolErrorHandlers;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.mock.ChatModelMock;
import dev.langchain4j.service.spring.AiServicesAutoConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static dev.langchain4j.service.spring.mode.explicit.toolErrorHandlers.AiServiceWithExplicitToolErrorHandlersApplication.NOT_WIRED;
import static org.assertj.core.api.Assertions.assertThat;

class AiServiceWithExplicitToolErrorHandlersTest {

    @Test
    void should_wire_tool_execution_error_handler_by_bean_name() {

        // given the tool throws, and there are two ToolExecutionErrorHandler beans
        ChatModelMock chatModel = chatModelRequesting("{\"arg0\": \"42\"}");

        contextRunner(chatModel).run(context -> {

            AiServiceWithExplicitToolErrorHandlers aiService =
                    context.getBean(AiServiceWithExplicitToolErrorHandlers.class);

            // when
            aiService.chat("What is the status of order 42?");

            // then the handler named in the annotation was wired, not the other one
            assertThat(chatModel.requests().get(1).messages())
                    .filteredOn(ToolExecutionResultMessage.class::isInstance)
                    .map(ToolExecutionResultMessage.class::cast)
                    .map(ToolExecutionResultMessage::text)
                    .containsExactly("The order service is unavailable.");
            assertThat(chatModel.requests().get(1).messages().toString()).doesNotContain(NOT_WIRED);
        });
    }

    @Test
    void should_wire_tool_arguments_error_handler_by_bean_name() {

        // given the LLM generated arguments that cannot be parsed,
        // and there are two ToolArgumentsErrorHandler beans
        ChatModelMock chatModel = chatModelRequesting("{\"arg0\": ");

        contextRunner(chatModel).run(context -> {

            AiServiceWithExplicitToolErrorHandlers aiService =
                    context.getBean(AiServiceWithExplicitToolErrorHandlers.class);

            // when
            aiService.chat("What is the status of order 42?");

            // then the handler named in the annotation was wired, not the other one
            assertThat(chatModel.requests().get(1).messages())
                    .filteredOn(ToolExecutionResultMessage.class::isInstance)
                    .map(ToolExecutionResultMessage.class::cast)
                    .map(ToolExecutionResultMessage::text)
                    .containsExactly("The order ID could not be read.");
            assertThat(chatModel.requests().get(1).messages().toString()).doesNotContain(NOT_WIRED);
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
                .withConfiguration(AutoConfigurations.of(AiServicesAutoConfig.class))
                .withBean("chatModel", ChatModel.class, () -> chatModel)
                .withUserConfiguration(AiServiceWithExplicitToolErrorHandlersApplication.class);
    }
}
