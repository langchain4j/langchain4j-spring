package dev.langchain4j.service.spring.mode.automatic.withToolProvider;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.mock.ChatModelMock;
import dev.langchain4j.service.spring.AiServicesAutoConfiguration;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class AiServiceWithToolProviderTest {

    ChatModelMock chatModel = ChatModelMock.thatAlwaysResponds(
            AiMessage.from(ToolExecutionRequest.builder().id("1").name("getName").arguments("{}").build()),
            AiMessage.from("I am Shrink"));

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AiServicesAutoConfiguration.class))
            .withBean(ChatModel.class, () -> chatModel);

    @Test
    void should_create_AI_service_with_tool_provider() {
        contextRunner
                .withUserConfiguration(AiServiceWithToolProviderApplication.class)
                .run(context -> {

                    // given
                    AiServiceWithToolProvider aiService = context.getBean(AiServiceWithToolProvider.class);

                    // when
                    String answer = aiService.chat("find name with tool and give me");

                    // then
                    assertThat(answer).containsIgnoringCase("Shrink");

                    // and the tool provider's specification was offered to the model
                    assertThat(chatModel.requests().get(0).toolSpecifications())
                            .extracting("name")
                            .containsExactly("getName");

                    // and its executor ran, with the result fed back to the model
                    assertThat(chatModel.requests().get(1).messages())
                            .last(InstanceOfAssertFactories.type(ToolExecutionResultMessage.class))
                            .satisfies(result -> {
                                assertThat(result.toolName()).isEqualTo("getName");
                                assertThat(result.text()).isEqualTo("I am Shrink");
                            });
                });
    }
}
