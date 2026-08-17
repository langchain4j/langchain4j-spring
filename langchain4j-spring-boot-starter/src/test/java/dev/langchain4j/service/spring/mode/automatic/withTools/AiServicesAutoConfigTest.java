package dev.langchain4j.service.spring.mode.automatic.withTools;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.mock.ChatModelMock;
import dev.langchain4j.service.spring.AiServicesAutoConfig;
import dev.langchain4j.service.spring.event.AiServiceRegisteredEvent;
import dev.langchain4j.service.spring.mode.automatic.withTools.aop.ToolObserverAspect;
import dev.langchain4j.service.spring.mode.automatic.withTools.listener.AiServiceRegisteredEventListener;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.List;

import static dev.langchain4j.service.spring.mode.automatic.withTools.AopEnhancedTools.TOOL_OBSERVER_KEY;
import static dev.langchain4j.service.spring.mode.automatic.withTools.AopEnhancedTools.TOOL_OBSERVER_KEY_NAME_DESCRIPTION;
import static dev.langchain4j.service.spring.mode.automatic.withTools.AopEnhancedTools.TOOL_OBSERVER_PACKAGE_NAME;
import static dev.langchain4j.service.spring.mode.automatic.withTools.AopEnhancedTools.TOOL_OBSERVER_PACKAGE_NAME_DESCRIPTION;
import static dev.langchain4j.service.spring.mode.automatic.withTools.PackagePrivateTools.CURRENT_TIME;
import static dev.langchain4j.service.spring.mode.automatic.withTools.PublicTools.CURRENT_DATE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiServicesAutoConfigTest {

    private static AiMessage callTools(String... toolNames) {
        List<ToolExecutionRequest> requests = new java.util.ArrayList<>();
        for (int i = 0; i < toolNames.length; i++) {
            requests.add(ToolExecutionRequest.builder()
                    .id(String.valueOf(i))
                    .name(toolNames[i])
                    .arguments("{}")
                    .build());
        }
        return AiMessage.from(requests);
    }

    private static ApplicationContextRunner contextRunnerWith(ChatModel chatModel) {
        return new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(AiServicesAutoConfig.class))
                .withBean(ChatModel.class, () -> chatModel);
    }

    @Test
    void should_create_AI_service_with_tool_which_is_public_method_in_public_class() {
        ChatModelMock chatModel = ChatModelMock.thatAlwaysResponds(
                callTools("getCurrentDate"), AiMessage.from("Today is " + CURRENT_DATE + "."));

        contextRunnerWith(chatModel)
                .withUserConfiguration(AiServiceWithToolsApplication.class)
                .run(context -> {

                    // given
                    AiServiceWithTools aiService = context.getBean(AiServiceWithTools.class);

                    // when
                    String answer = aiService.chat("What is the current date?");

                    // then should use PublicTools.getCurrentDate()
                    assertThat(answer).contains(String.valueOf(CURRENT_DATE.getDayOfMonth()));
                    assertThat(chatModel.requests().get(1).messages())
                            .filteredOn(ToolExecutionResultMessage.class::isInstance)
                            .extracting(message -> ((ToolExecutionResultMessage) message).text())
                            .containsExactly(CURRENT_DATE.toString());
                });
    }

    @Test
    void should_create_AI_service_with_tool_that_is_package_private_method_in_package_private_class() {
        ChatModelMock chatModel = ChatModelMock.thatAlwaysResponds(
                callTools("getCurrentTime"), AiMessage.from("It is " + CURRENT_TIME + " now."));

        contextRunnerWith(chatModel)
                .withUserConfiguration(AiServiceWithToolsApplication.class)
                .run(context -> {

                    // given
                    AiServiceWithTools aiService = context.getBean(AiServiceWithTools.class);

                    // when
                    String answer = aiService.chat("What is the current time?");

                    // then should use PackagePrivateTools.getCurrentTime()
                    assertThat(answer).contains(String.valueOf(CURRENT_TIME.getMinute()));
                    assertThat(chatModel.requests().get(1).messages())
                            .filteredOn(ToolExecutionResultMessage.class::isInstance)
                            .extracting(message -> ((ToolExecutionResultMessage) message).text())
                            .containsExactly(CURRENT_TIME.toString());
                });
    }

    @Test
    void should_receive_ai_service_registered_event() {
        contextRunnerWith(ChatModelMock.thatAlwaysResponds("Answer"))
                .withUserConfiguration(AiServiceWithToolsApplication.class)
                .run(context -> {

                    // given
                    AiServiceRegisteredEventListener listener = context.getBean(AiServiceRegisteredEventListener.class);

                    // then should receive AiServiceRegisteredEvent
                    assertTrue(listener.isEventReceived());
                    assertEquals(1, listener.getReceivedEvents().size());

                    AiServiceRegisteredEvent event = listener.getReceivedEvents().stream().findFirst().orElse(null);
                    assertNotNull(event);
                    assertEquals(AiServiceWithTools.class, event.aiServiceClass());
                    assertEquals(4, event.toolSpecifications().size());

                    List<String> tools = event.toolSpecifications().stream().map(ToolSpecification::name).toList();
                    assertTrue(tools.contains("getCurrentDate"));
                    assertTrue(tools.contains("getCurrentTime"));
                    assertTrue(tools.contains("getToolObserverPackageName"));
                    assertTrue(tools.contains("getToolObserverKey"));
                });
    }

    @Test
    void should_create_AI_service_with_tool_which_is_enhanced_by_spring_aop() {
        ChatModelMock chatModel = ChatModelMock.thatAlwaysResponds(
                callTools("getToolObserverPackageName", "getToolObserverKey", "getCurrentTime"),
                AiMessage.from("The annotation lives in " + TOOL_OBSERVER_PACKAGE_NAME + ", its key is "
                        + TOOL_OBSERVER_KEY + " and it is " + CURRENT_TIME + " now."));

        contextRunnerWith(chatModel)
                .withUserConfiguration(AiServiceWithToolsApplication.class)
                .run(context -> {

                    // given
                    AiServiceWithTools aiService = context.getBean(AiServiceWithTools.class);

                    // when
                    String answer = aiService.chat("Which package is the @ToolObserver annotation located in? "
                            + "And what is the key of the @ToolObserver annotation?"
                            + "And What is the current time?");

                    // then should use AopEnhancedTools.getAspectPackage()
                    // & AopEnhancedTools.getToolObserverKey()
                    // & PackagePrivateTools.getCurrentTime()
                    assertThat(answer).contains(TOOL_OBSERVER_PACKAGE_NAME);
                    assertThat(answer).contains(TOOL_OBSERVER_KEY);
                    assertThat(answer).contains(String.valueOf(CURRENT_TIME.getMinute()));

                    // and all three tool beans ran, with their results fed back to the model
                    assertThat(chatModel.requests().get(1).messages())
                            .filteredOn(ToolExecutionResultMessage.class::isInstance)
                            .extracting(message -> ((ToolExecutionResultMessage) message).text())
                            .containsExactly(TOOL_OBSERVER_PACKAGE_NAME, TOOL_OBSERVER_KEY, CURRENT_TIME.toString());

                    // and AOP aspect should be called
                    // & only for getToolObserverKey() which is annotated with @ToolObserver
                    ToolObserverAspect aspect = context.getBean(ToolObserverAspect.class);
                    assertTrue(aspect.aspectHasBeenCalled());

                    assertEquals(1, aspect.getObservedTools().size());
                    assertTrue(aspect.getObservedTools().contains(TOOL_OBSERVER_KEY_NAME_DESCRIPTION));
                    assertFalse(aspect.getObservedTools().contains(TOOL_OBSERVER_PACKAGE_NAME_DESCRIPTION));
                });
    }

    // TODO tools which are not @Beans?
    // TODO negative cases
    // TODO no @AiServices in app, just models
    // TODO @AiServices as inner class?
    // TODO streaming, memory, tools, etc
}
