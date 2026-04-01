package dev.langchain4j.service.spring.mode.automatic.withTools;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.service.spring.AiServicesAutoConfig;
import dev.langchain4j.service.spring.event.AiServiceRegisteredEvent;
import dev.langchain4j.service.spring.mode.automatic.withTools.aop.ToolObserverAspect;
import dev.langchain4j.service.spring.mode.automatic.withTools.listener.AiServiceRegisteredEventListener;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.List;

import static dev.langchain4j.service.spring.mode.ApiKeys.OPENAI_API_KEY;
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

class AiServicesAutoConfigIT {

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AiServicesAutoConfig.class));

    @Test
    void should_create_AI_service_with_tool_which_is_public_method_in_public_class() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.open-ai.chat-model.api-key=" + OPENAI_API_KEY,
                        "langchain4j.open-ai.chat-model.model-name=gpt-4o-mini",
                        "langchain4j.open-ai.chat-model.temperature=0.0",
                        "langchain4j.open-ai.chat-model.log-requests=true",
                        "langchain4j.open-ai.chat-model.log-responses=true"
                )
                .withUserConfiguration(AiServiceWithToolsApplication.class)
                .run(context -> {

                    // given
                    AiServiceWithTools aiService = context.getBean(AiServiceWithTools.class);

                    // when
                    String answer = aiService.chat("What is the current date?");

                    // then should use PublicTools.getCurrentDate()
                    assertThat(answer).contains(String.valueOf(CURRENT_DATE.getDayOfMonth()));
                });
    }

    @Test
    void should_create_AI_service_with_tool_that_is_package_private_method_in_package_private_class() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.open-ai.chat-model.api-key=" + OPENAI_API_KEY,
                        "langchain4j.open-ai.chat-model.model-name=gpt-4o-mini",
                        "langchain4j.open-ai.chat-model.temperature=0.0",
                        "langchain4j.open-ai.chat-model.log-requests=true",
                        "langchain4j.open-ai.chat-model.log-responses=true"
                )
                .withUserConfiguration(AiServiceWithToolsApplication.class)
                .run(context -> {

                    // given
                    AiServiceWithTools aiService = context.getBean(AiServiceWithTools.class);

                    // when
                    String answer = aiService.chat("What is the current time?");

                    // then should use PackagePrivateTools.getCurrentTime()
                    assertThat(answer).contains(String.valueOf(CURRENT_TIME.getMinute()));
                });
    }

    @Test
    void should_receive_ai_service_registered_event() {
        contextRunner
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
        contextRunner
                .withPropertyValues(
                        "langchain4j.open-ai.chat-model.api-key=" + OPENAI_API_KEY,
                        "langchain4j.open-ai.chat-model.model-name=gpt-4o-mini",
                        "langchain4j.open-ai.chat-model.temperature=0.0",
                        "langchain4j.open-ai.chat-model.log-requests=true",
                        "langchain4j.open-ai.chat-model.log-responses=true"
                )
                .withUserConfiguration(AiServiceWithToolsApplication.class)
                .run(context -> {

                    // given
                    AiServiceWithTools aiService = context.getBean(AiServiceWithTools.class);

                    // when
                    String answer = aiService.chat("Which package is the @ToolObserver annotation located in? " +
                            "And what is the key of the @ToolObserver annotation?" +
                            "And What is the current time?");

                    System.out.println("Answer: " + answer);

                    // then should use AopEnhancedTools.getAspectPackage()
                    // & AopEnhancedTools.getToolObserverKey()
                    // & PackagePrivateTools.getCurrentTime()
                    assertThat(answer).contains(TOOL_OBSERVER_PACKAGE_NAME);
                    assertThat(answer).contains(TOOL_OBSERVER_KEY);
                    assertThat(answer).contains(String.valueOf(CURRENT_TIME.getMinute()));

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