package dev.langchain4j.service.spring;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.langchain4j.spi.CapturedContext;
import dev.langchain4j.spi.ExecutorProvider;
import dev.langchain4j.spring.LangChain4jAutoConfiguration;
import java.util.concurrent.Executor;
import org.springframework.core.task.TaskDecorator;
import java.util.concurrent.CompletableFuture;
import io.micrometer.context.ContextRegistry;
import io.micrometer.context.ContextSnapshotFactory;
import java.util.concurrent.CyclicBarrier;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.core.task.support.ContextPropagatingTaskDecorator;
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
    void captures_context_with_the_application_task_decorator() throws Exception {
        // a TaskDecorator captures context when decorate() is called, as ContextPropagatingTaskDecorator does
        ThreadLocal<String> requestId = new ThreadLocal<>();
        TaskDecorator capturingDecorator = task -> {
            String captured = requestId.get();
            return () -> {
                requestId.set(captured);
                try {
                    task.run();
                } finally {
                    requestId.remove();
                }
            };
        };

        runner.withBean(TaskDecorator.class, () -> capturingDecorator)
                .withPropertyValues("langchain4j.executor.use-spring-task-executor=true")
                .run(context -> {
                    requestId.set("request-42");
                    CapturedContext captured = ExecutorProvider.get().captureContext();
                    requestId.remove();

                    CompletableFuture<String> seen = new CompletableFuture<>();
                    Thread otherThread = new Thread(captured.wrap(() -> seen.complete(requestId.get())));
                    otherThread.start();

                    assertThat(seen.get(10, SECONDS)).isEqualTo("request-42");
                });
    }

    @Test
    void captures_nothing_without_a_task_decorator_and_without_micrometer() {
        runner.withClassLoader(new FilteredClassLoader(ContextSnapshotFactory.class))
                .withPropertyValues("langchain4j.executor.use-spring-task-executor=true")
                .run(context -> {
                    Runnable task = () -> {};
                    assertThat(ExecutorProvider.get().captureContext().wrap(task)).isSameAs(task);
                });
    }

    @Test
    void captures_context_with_micrometer_without_a_task_decorator() {
        ThreadLocal<String> requestId = new ThreadLocal<>();
        ContextRegistry.getInstance()
                .registerThreadLocalAccessor(REQUEST_ID_KEY, requestId::get, requestId::set, requestId::remove);
        try {
            runner.withPropertyValues("langchain4j.executor.use-spring-task-executor=true")
                    .run(context -> assertThat(runCapturedOnAnotherThread(requestId)).isEqualTo("request-42"));
        } finally {
            ContextRegistry.getInstance().removeThreadLocalAccessor(REQUEST_ID_KEY);
        }
    }

    @Test
    void runs_tasks_concurrently_with_a_task_decorator_that_keeps_state_while_running() {
        // like Spring Security's DelegatingSecurityContextRunnable: a decorated task must not run concurrently with itself
        AtomicInteger overlappingRuns = new AtomicInteger();
        TaskDecorator statefulDecorator = task -> {
            AtomicBoolean running = new AtomicBoolean();
            return () -> {
                if (running.getAndSet(true)) {
                    overlappingRuns.incrementAndGet();
                }
                try {
                    task.run();
                } finally {
                    running.set(false);
                }
            };
        };

        runner.withBean(TaskDecorator.class, () -> statefulDecorator)
                .withPropertyValues("langchain4j.executor.use-spring-task-executor=true")
                .run(context -> {
                    assertThat(maxConcurrentRuns(ExecutorProvider.get().captureContext())).isEqualTo(2);
                    assertThat(overlappingRuns).hasValue(0);
                });
    }

    @Test
    void keeps_running_tasks_after_a_task_failed() {
        ThreadLocal<String> requestId = new ThreadLocal<>();
        runner.withBean(TaskDecorator.class, () -> propagating(requestId))
                .withPropertyValues("langchain4j.executor.use-spring-task-executor=true")
                .run(context -> {
                    requestId.set("request-42");
                    CapturedContext captured = ExecutorProvider.get().captureContext();
                    requestId.remove();

                    assertThatThrownBy(() -> captured.wrap(() -> {
                                throw new IllegalStateException("tool failed");
                            }).run())
                            .hasMessage("tool failed");

                    CompletableFuture<String> seen = new CompletableFuture<>();
                    new Thread(captured.wrap(() -> seen.complete(requestId.get()))).start();
                    assertThat(seen.get(10, SECONDS)).isEqualTo("request-42");
                    assertThat(requestId.get()).isNull();
                });
    }

    @Test
    void runs_a_task_that_runs_another_task_of_the_same_invocation() {
        ThreadLocal<String> requestId = new ThreadLocal<>();
        runner.withBean(TaskDecorator.class, () -> propagating(requestId))
                .withPropertyValues("langchain4j.executor.use-spring-task-executor=true")
                .run(context -> {
                    requestId.set("request-42");
                    CapturedContext captured = ExecutorProvider.get().captureContext();
                    requestId.remove();

                    List<String> seen = new CopyOnWriteArrayList<>();
                    Runnable inner = captured.wrap(() -> seen.add("inner:" + requestId.get()));
                    Runnable outer = captured.wrap(() -> {
                        inner.run();
                        seen.add("outer:" + requestId.get());
                    });
                    CompletableFuture.runAsync(outer).get(10, SECONDS);

                    assertThat(seen).containsExactly("inner:request-42", "outer:request-42");
                });
    }

    @Test
    void runs_tasks_concurrently_with_the_context_propagating_task_decorator() {
        runner.withBean(TaskDecorator.class, ContextPropagatingTaskDecorator::new)
                .withPropertyValues("langchain4j.executor.use-spring-task-executor=true")
                .run(context -> assertThat(maxConcurrentRuns(ExecutorProvider.get().captureContext())).isEqualTo(2));
    }

    @Test
    void captures_context_with_every_task_decorator() {
        ThreadLocal<String> requestId = new ThreadLocal<>();
        ThreadLocal<String> tenant = new ThreadLocal<>();
        runner.withBean("requestIdDecorator", TaskDecorator.class, () -> propagating(requestId))
                .withBean("tenantDecorator", TaskDecorator.class, () -> propagating(tenant))
                .withPropertyValues("langchain4j.executor.use-spring-task-executor=true")
                .run(context -> {
                    requestId.set("request-42");
                    tenant.set("tenant-7");
                    CapturedContext captured = ExecutorProvider.get().captureContext();
                    requestId.remove();
                    tenant.remove();

                    CompletableFuture<String> seen = new CompletableFuture<>();
                    new Thread(captured.wrap(() -> seen.complete(requestId.get() + "/" + tenant.get()))).start();

                    assertThat(seen.get(10, SECONDS)).isEqualTo("request-42/tenant-7");
                });
    }

    private static final String REQUEST_ID_KEY = "langchain4j.test.request-id";

    private static String runCapturedOnAnotherThread(ThreadLocal<String> requestId) throws Exception {
        requestId.set("request-42");
        CapturedContext captured = ExecutorProvider.get().captureContext();
        requestId.remove();

        CompletableFuture<String> seen = new CompletableFuture<>();
        new Thread(captured.wrap(() -> seen.complete(requestId.get()))).start();
        return seen.get(10, SECONDS);
    }

    private static TaskDecorator propagating(ThreadLocal<String> threadLocal) {
        return task -> {
            String captured = threadLocal.get();
            return () -> {
                String previous = threadLocal.get();
                threadLocal.set(captured);
                try {
                    task.run();
                } finally {
                    threadLocal.set(previous);
                }
            };
        };
    }

    /**
     * Runs two tasks wrapped by the same capture at the same time; each waits for the other to start.
     */
    private static int maxConcurrentRuns(CapturedContext captured) throws Exception {
        AtomicInteger running = new AtomicInteger();
        AtomicInteger maxRunning = new AtomicInteger();
        CyclicBarrier bothRunning = new CyclicBarrier(2);
        Runnable task = () -> {
            maxRunning.accumulateAndGet(running.incrementAndGet(), Math::max);
            try {
                bothRunning.await(5, SECONDS);
            } catch (Exception ignored) {
                // the other task did not start while this one ran
            } finally {
                running.decrementAndGet();
            }
        };
        Thread first = new Thread(captured.wrap(task));
        Thread second = new Thread(captured.wrap(task));
        first.start();
        second.start();
        first.join(10_000);
        second.join(10_000);
        return maxRunning.get();
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
