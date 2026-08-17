package dev.langchain4j.service.spring.mode.explicit.tools;

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

import static dev.langchain4j.service.spring.mode.explicit.tools.Tools1.TOOL_1_TEMPERATURE;
import static dev.langchain4j.service.spring.mode.explicit.tools.Tools2.TOOL_2_TEMPERATURE;
import static org.assertj.core.api.Assertions.assertThat;

class AiServiceWithExplicitToolsTest {

    ChatModelMock chatModel = ChatModelMock.thatAlwaysResponds(
            AiMessage.from(
                    ToolExecutionRequest.builder().id("1").name("getCurrentTemperature").arguments("{}").build()),
            AiMessage.from("The current temperature is " + TOOL_1_TEMPERATURE + " degrees."));

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AiServicesAutoConfiguration.class))
            .withBean("openAiChatModel", ChatModel.class, () -> chatModel);

    @Test
    void should_create_AI_service_with_explicit_tools() {
        contextRunner
                .withUserConfiguration(AiServiceWithExplicitToolsApplication.class)
                .run(context -> {

                    // given
                    AiServiceWithExplicitTools aiService = context.getBean(AiServiceWithExplicitTools.class);

                    // when
                    String answer = aiService.chat("What is the temperature?");

                    // then
                    assertThat(answer).contains(TOOL_1_TEMPERATURE).doesNotContain(TOOL_2_TEMPERATURE);

                    // and only the explicitly wired Tools1 was offered to the model
                    assertThat(chatModel.requests().get(0).toolSpecifications())
                            .extracting("name")
                            .containsExactly("getCurrentTemperature");

                    // and Tools1 was the bean that ran, not Tools2
                    assertThat(chatModel.requests().get(1).messages())
                            .last(InstanceOfAssertFactories.type(ToolExecutionResultMessage.class))
                            .satisfies(result -> assertThat(result.text()).isEqualTo(TOOL_1_TEMPERATURE));
                });
    }
}
