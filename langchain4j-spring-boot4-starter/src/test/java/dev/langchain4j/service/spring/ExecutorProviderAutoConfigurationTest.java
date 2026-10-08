package dev.langchain4j.service.spring;

import static org.assertj.core.api.Assertions.assertThat;

import dev.langchain4j.spi.ExecutorProvider;
import dev.langchain4j.spring.LangChain4jAutoConfiguration;
import java.util.concurrent.Executor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

class ExecutorProviderAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(TaskExecutionAutoConfiguration.class))
            .withUserConfiguration(ExecutorProviderAutoConfiguration.class);

    @Test
    void does_nothing_unless_the_property_is_set() {
        ExecutorProvider before = ExecutorProvider.get();

        runner.run(context -> assertThat(ExecutorProvider.get()).isSameAs(before));
    }

    @Test
    void routes_offloaded_work_through_the_Spring_task_executor_when_enabled() {
        runner.withPropertyValues("langchain4j.executor.use-spring-task-executor=true")
                .run(context -> {
                    Executor spring = context.getBean(AsyncTaskExecutor.class);

                    assertThat(ExecutorProvider.get()).isNotNull();
                    assertThat(ExecutorProvider.get().executor()).isSameAs(spring);
                });
    }

    @Test
    void prefers_the_application_task_executor_over_the_streaming_executors_of_the_provider_starters() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        dev.langchain4j.openai.spring.OpenAiAutoConfiguration.class, TaskExecutionAutoConfiguration.class))
                .withUserConfiguration(ExecutorProviderAutoConfiguration.class)
                .withPropertyValues(
                        "langchain4j.open-ai.streaming-chat-model.api-key=test-api-key",
                        "langchain4j.open-ai.streaming-chat-model.model-name=test-model",
                        "langchain4j.executor.use-spring-task-executor=true")
                .run(context -> {
                    assertThat(context.getBeanNamesForType(AsyncTaskExecutor.class)).hasSizeGreaterThan(1);
                    assertThat(ExecutorProvider.get().executor())
                            .isSameAs(context.getBean(TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME));
                });
    }

    @Test
    void uses_the_only_async_task_executor_when_the_application_replaced_the_default_one() {
        ThreadPoolTaskExecutor applicationExecutor = new ThreadPoolTaskExecutor();

        new ApplicationContextRunner()
                .withUserConfiguration(ExecutorProviderAutoConfiguration.class)
                .withBean("myExecutor", AsyncTaskExecutor.class, () -> applicationExecutor)
                .withPropertyValues("langchain4j.executor.use-spring-task-executor=true")
                .run(context -> assertThat(ExecutorProvider.get().executor()).isSameAs(applicationExecutor));
    }

    @Test
    void is_applied_by_the_LangChain4j_auto_configuration() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(LangChain4jAutoConfiguration.class, TaskExecutionAutoConfiguration.class))
                // the AI Service scanner of that configuration needs a base package; this one contains no AI Services
                .withInitializer(context -> AutoConfigurationPackages.register(
                        (BeanDefinitionRegistry) context.getBeanFactory(), "dev.langchain4j.service.spring.none"))
                .withPropertyValues("langchain4j.executor.use-spring-task-executor=true")
                .run(context -> assertThat(ExecutorProvider.get().executor())
                        .isSameAs(context.getBean(AsyncTaskExecutor.class)));
    }

    @Test
    void restores_the_previous_provider_when_the_context_closes() {
        // ExecutorProvider.set is process-wide, so a context that took it over has to give it back - otherwise the
        // next context, or the next test, silently inherits an executor belonging to a closed application.
        ExecutorProvider before = ExecutorProvider.get();

        runner.withPropertyValues("langchain4j.executor.use-spring-task-executor=true")
                .run(context -> assertThat(ExecutorProvider.get().executor())
                        .isSameAs(context.getBean(AsyncTaskExecutor.class)));

        assertThat(ExecutorProvider.get()).isSameAs(before);
    }

    @Test
    void does_not_touch_a_provider_it_did_not_install() {
        // Without an AsyncTaskExecutor bean nothing is installed, so closing the context must not clear a provider
        // that the application registered itself.
        ExecutorProvider before = ExecutorProvider.get();
        ExecutorProvider applicationProvider = () -> Runnable::run;
        ExecutorProvider.set(applicationProvider);
        try {
            new ApplicationContextRunner()
                    .withUserConfiguration(ExecutorProviderAutoConfiguration.class)
                    .withPropertyValues("langchain4j.executor.use-spring-task-executor=true")
                    .run(context -> assertThat(ExecutorProvider.get()).isSameAs(applicationProvider));

            assertThat(ExecutorProvider.get()).isSameAs(applicationProvider);
        } finally {
            ExecutorProvider.set(before);
        }
    }
}
