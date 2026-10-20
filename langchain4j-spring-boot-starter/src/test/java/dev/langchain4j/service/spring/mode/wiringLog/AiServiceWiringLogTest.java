package dev.langchain4j.service.spring.mode.wiringLog;

import ch.qos.logback.classic.Level;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.service.IllegalConfigurationException;
import dev.langchain4j.service.spring.AiServicesAutoConfig;
import dev.langchain4j.service.spring.LogCapture;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.NestedExceptionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static dev.langchain4j.service.spring.mode.wiringLog.WiringLogApplication.CHAT_MEMORY_BEAN_NAME;
import static dev.langchain4j.service.spring.mode.wiringLog.WiringLogApplication.CHAT_MODEL_BEAN_NAME;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * The @AiService wiring log is diagnostic only: enabling DEBUG must not change startup, wiring,
 * model requests, tool execution or bean initialization.
 */
class AiServiceWiringLogTest {

    private static final String REGISTERED_LOG_PREFIX = "Registered @AiService bean";

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AiServicesAutoConfig.class))
            .withUserConfiguration(WiringLogApplication.class);

    @Test
    void should_behave_identically_with_debug_logging_disabled_and_enabled() {
        Outcome debugDisabled = run(Level.INFO, contextRunner);
        Outcome debugEnabled = run(Level.DEBUG, contextRunner);

        assertThat(debugDisabled.startupFailure()).isNull();
        assertThat(debugDisabled.responses()).containsExactly(
                "Tool said: all systems go #1",
                "Tool said: all systems go #2");
        assertThat(debugDisabled.modelRequests()).hasSize(4);
        assertThat(debugDisabled.modelRequests())
                .allSatisfy(request -> assertThat(request.toolSpecifications())
                        .extracting(ToolSpecification::name)
                        .containsExactly("currentStatus"));
        assertThat(debugDisabled.toolExecutions()).isEqualTo(2);

        assertSameBehaviour(debugDisabled, debugEnabled);

        assertThat(debugDisabled.logs()).doesNotContain(REGISTERED_LOG_PREFIX);
        assertThat(debugEnabled.logs())
                .contains(REGISTERED_LOG_PREFIX + " 'automaticAssistant'")
                .contains("chatModel=chatModel, chatMemory=chatMemory, tools=[statusTool]")
                .contains(REGISTERED_LOG_PREFIX + " 'explicitAssistant'")
                .contains("chatModel=chatModel, tools=[statusTool]")
                .doesNotContain("(no such bean)");
    }

    @Test
    void should_not_initialize_beans_prematurely_or_more_often_with_debug_logging_enabled() {
        Outcome debugDisabled = run(Level.INFO, contextRunner);
        Outcome debugEnabled = run(Level.DEBUG, contextRunner);

        assertThat(debugDisabled.initializedBeforeBeanPostProcessors()).isEmpty();
        assertThat(debugEnabled.initializedBeforeBeanPostProcessors()).isEmpty();

        Map<String, Integer> expectedAtStartup = Map.of(
                CHAT_MODEL_BEAN_NAME, 1,
                CHAT_MEMORY_BEAN_NAME, 1,
                StatusTool.BEAN_NAME, 1);
        assertThat(debugDisabled.initializedAtStartup()).isEqualTo(expectedAtStartup);
        assertThat(debugEnabled.initializedAtStartup()).isEqualTo(expectedAtStartup);
    }

    @Test
    void should_fail_identically_when_explicitly_named_chat_model_does_not_exist() {
        ApplicationContextRunner runner = contextRunner.withPropertyValues("wiring-log.chat-model=missingChatModel");

        Outcome debugDisabled = run(Level.INFO, runner);
        Outcome debugEnabled = run(Level.DEBUG, runner);

        assertThat(debugDisabled.startupFailure())
                .startsWith("org.springframework.beans.factory.BeanCreationException -> "
                        + NoSuchBeanDefinitionException.class.getName() + ": ")
                .contains("'missingChatModel'");
        assertSameBehaviour(debugDisabled, debugEnabled);
        assertThat(debugEnabled.logs()).contains("chatModel=missingChatModel (no such bean)");
    }

    @Test
    void should_fail_identically_when_explicitly_named_tool_does_not_exist() {
        ApplicationContextRunner runner = contextRunner.withPropertyValues("wiring-log.tools=statusTool, missingTool");

        Outcome debugDisabled = run(Level.INFO, runner);
        Outcome debugEnabled = run(Level.DEBUG, runner);

        assertThat(debugDisabled.startupFailure())
                .startsWith("org.springframework.beans.factory.BeanCreationException -> "
                        + NoSuchBeanDefinitionException.class.getName() + ": ")
                .contains("'missingTool'");
        assertSameBehaviour(debugDisabled, debugEnabled);
        assertThat(debugEnabled.logs()).contains("tools=[statusTool, missingTool (no such bean)]");
    }

    @Test
    void should_fail_identically_when_multiple_chat_models_are_candidates_for_automatic_wiring() {
        ApplicationContextRunner runner = contextRunner.withBean("chatModel2", ChatModel.class, RecordingChatModel::new);

        Outcome debugDisabled = run(Level.INFO, runner);
        Outcome debugEnabled = run(Level.DEBUG, runner);

        assertThat(debugDisabled.startupFailure())
                .startsWith(IllegalConfigurationException.class.getName() + " -> "
                        + IllegalConfigurationException.class.getName() + ": ")
                .contains("Conflict: multiple beans of type " + ChatModel.class.getName() + " are found");
        assertSameBehaviour(debugDisabled, debugEnabled);
    }

    private static void assertSameBehaviour(Outcome debugDisabled, Outcome debugEnabled) {
        assertThat(debugEnabled.startupFailure()).isEqualTo(debugDisabled.startupFailure());
        assertThat(debugEnabled.beanNames()).isEqualTo(debugDisabled.beanNames());
        assertThat(debugEnabled.initializedBeforeBeanPostProcessors())
                .isEqualTo(debugDisabled.initializedBeforeBeanPostProcessors());
        assertThat(debugEnabled.initializedAtStartup()).isEqualTo(debugDisabled.initializedAtStartup());
        assertThat(debugEnabled.responses()).isEqualTo(debugDisabled.responses());
        assertThat(debugEnabled.modelRequests()).isEqualTo(debugDisabled.modelRequests());
        assertThat(debugEnabled.toolExecutions()).isEqualTo(debugDisabled.toolExecutions());
    }

    private static Outcome run(Level level, ApplicationContextRunner runner) {
        WiringLogApplication.resetInitializations();
        AtomicReference<Outcome> outcome = new AtomicReference<>();
        try (LogCapture logs = LogCapture.atLevel(AiServicesAutoConfig.class, level)) {
            runner.run(context -> {
                Map<String, Integer> initializedBeforeBeanPostProcessors =
                        WiringLogApplication.initializationsBeforeBeanPostProcessors();
                Throwable startupFailure = context.getStartupFailure();
                if (startupFailure != null) {
                    outcome.set(new Outcome(describe(startupFailure), List.of(), initializedBeforeBeanPostProcessors,
                            WiringLogApplication.initializations(), List.of(), List.of(), 0, logs.messages()));
                    return;
                }

                Map<String, Integer> initializedAtStartup = WiringLogApplication.initializations();
                List<String> responses = List.of(
                        context.getBean(AutomaticAssistant.class).chat("Hello from automatic"),
                        context.getBean(ExplicitAssistant.class).chat("Hello from explicit"));
                List<String> beanNames = Arrays.stream(context.getBeanDefinitionNames()).sorted().toList();

                outcome.set(new Outcome(
                        null,
                        beanNames,
                        initializedBeforeBeanPostProcessors,
                        initializedAtStartup,
                        responses,
                        context.getBean(CHAT_MODEL_BEAN_NAME, RecordingChatModel.class).requests(),
                        context.getBean(StatusTool.class).executions(),
                        logs.messages()));
            });
        }
        return outcome.get();
    }

    private static String describe(Throwable failure) {
        Throwable rootCause = NestedExceptionUtils.getMostSpecificCause(failure);
        return failure.getClass().getName() + " -> " + rootCause.getClass().getName() + ": " + rootCause.getMessage();
    }

    private record Outcome(String startupFailure,
                           List<String> beanNames,
                           Map<String, Integer> initializedBeforeBeanPostProcessors,
                           Map<String, Integer> initializedAtStartup,
                           List<String> responses,
                           List<ChatRequest> modelRequests,
                           int toolExecutions,
                           String logs) {
    }
}
