package dev.langchain4j.service.spring;

import static org.assertj.core.api.Assertions.assertThat;

import dev.langchain4j.spi.ExecutorProvider;
import java.util.concurrent.Executor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.task.AsyncTaskExecutor;

class ExecutorProviderAutoConfigTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(TaskExecutionAutoConfiguration.class))
            .withUserConfiguration(ExecutorProviderAutoConfig.class);

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
    void restores_the_previous_provider_when_the_context_closes() {
        // ExecutorProvider.set is process-wide, so a context that took it over has to give it back - otherwise the
        // next context, or the next test, silently inherits an executor belonging to a closed application.
        ExecutorProvider before = ExecutorProvider.get();

        runner.withPropertyValues("langchain4j.executor.use-spring-task-executor=true")
                .run(context -> assertThat(ExecutorProvider.get()).isNotNull());

        assertThat(ExecutorProvider.get()).isSameAs(before);
    }
}
