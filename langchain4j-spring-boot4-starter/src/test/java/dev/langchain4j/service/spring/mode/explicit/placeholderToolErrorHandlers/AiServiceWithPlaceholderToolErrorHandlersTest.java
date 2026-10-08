package dev.langchain4j.service.spring.mode.explicit.placeholderToolErrorHandlers;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.mock.ChatModelMock;
import dev.langchain4j.service.IllegalConfigurationException;
import dev.langchain4j.service.spring.AiServicesAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.List;

import static dev.langchain4j.service.spring.mode.explicit.placeholderToolErrorHandlers.AiServiceWithPlaceholderToolErrorHandlersApplication.CONFIGURED_ARGUMENTS_ERROR_HANDLER_BEAN_NAME;
import static dev.langchain4j.service.spring.mode.explicit.placeholderToolErrorHandlers.AiServiceWithPlaceholderToolErrorHandlersApplication.CONFIGURED_EXECUTION_ERROR_HANDLER_BEAN_NAME;
import static dev.langchain4j.service.spring.mode.explicit.placeholderToolErrorHandlers.AiServiceWithPlaceholderToolErrorHandlersApplication.NOT_WIRED;
import static org.assertj.core.api.Assertions.assertThat;

class AiServiceWithPlaceholderToolErrorHandlersTest {

    @Test
    void should_resolve_tool_execution_error_handler_from_placeholder() {

        // given the tool throws, and there are two ToolExecutionErrorHandler beans
        ChatModelMock chatModel = chatModelRequesting("{\"arg0\": \"42\"}");

        contextRunner(chatModel).run(context -> {

            AiServiceWithPlaceholderToolErrorHandlers aiService =
                    context.getBean(AiServiceWithPlaceholderToolErrorHandlers.class);

            // when
            aiService.chat("What is the status of order 42?");

            // then the handler named by the property was wired, not the other one
            assertThat(toolResults(chatModel)).containsExactly("The order service is unavailable.");
        });
    }

    @Test
    void should_resolve_tool_arguments_error_handler_from_placeholder() {

        // given the LLM generated arguments that cannot be parsed,
        // and there are two ToolArgumentsErrorHandler beans
        ChatModelMock chatModel = chatModelRequesting("{\"arg0\": ");

        contextRunner(chatModel).run(context -> {

            AiServiceWithPlaceholderToolErrorHandlers aiService =
                    context.getBean(AiServiceWithPlaceholderToolErrorHandlers.class);

            // when
            aiService.chat("What is the status of order 42?");

            // then the handler named by the property was wired, not the other one
            assertThat(toolResults(chatModel)).containsExactly("The order ID could not be read.");
        });
    }

    @Test
    void should_fail_when_tool_error_handler_placeholder_cannot_be_resolved() {

        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(AiServicesAutoConfiguration.class))
                .withBean("chatModel", ChatModel.class, () -> chatModelRequesting("{}"))
                .withUserConfiguration(AiServiceWithPlaceholderToolErrorHandlersApplication.class)
                .withPropertyValues("my.tool-execution-error-handler.name=" + CONFIGURED_EXECUTION_ERROR_HANDLER_BEAN_NAME)
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .isInstanceOf(IllegalConfigurationException.class)
                            .hasMessageContaining("Cannot resolve the 'toolArgumentsErrorHandler' attribute of @AiService on "
                                    + AiServiceWithPlaceholderToolErrorHandlers.class.getName())
                            .hasMessageContaining("Could not resolve placeholder 'my.tool-arguments-error-handler.name'");
                });
    }

    private static List<String> toolResults(ChatModelMock chatModel) {
        assertThat(chatModel.requests().get(1).messages().toString()).doesNotContain(NOT_WIRED);
        return chatModel.requests().get(1).messages().stream()
                .filter(ToolExecutionResultMessage.class::isInstance)
                .map(ToolExecutionResultMessage.class::cast)
                .map(ToolExecutionResultMessage::text)
                .toList();
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
                .withBean("chatModel", ChatModel.class, () -> chatModel)
                .withUserConfiguration(AiServiceWithPlaceholderToolErrorHandlersApplication.class)
                .withPropertyValues(
                        "my.tool-execution-error-handler.name=" + CONFIGURED_EXECUTION_ERROR_HANDLER_BEAN_NAME,
                        "my.tool-arguments-error-handler.name=" + CONFIGURED_ARGUMENTS_ERROR_HANDLER_BEAN_NAME);
    }
}
