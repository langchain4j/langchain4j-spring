package dev.langchain4j.service.spring.mode.automatic.withToolErrorHandler;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.mock.ChatModelMock;
import dev.langchain4j.service.spring.AiServicesAutoConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class AiServiceWithToolErrorHandlerTest {

    ChatModelMock chatModel = ChatModelMock.thatAlwaysResponds(
            AiMessage.from(ToolExecutionRequest.builder()
                    .id("1")
                    .name("orderStatus")
                    .arguments("{\"arg0\": \"42\"}")
                    .build()),
            AiMessage.from("I could not check the order"));

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AiServicesAutoConfig.class))
            .withBean(ChatModel.class, () -> chatModel);

    @Test
    void should_wire_tool_execution_error_handler_bean_into_ai_service() {
        contextRunner
                .withUserConfiguration(AiServiceWithToolErrorHandlerApplication.class)
                .run(context -> {

                    // given
                    AiServiceWithToolErrorHandler aiService = context.getBean(AiServiceWithToolErrorHandler.class);

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
}
