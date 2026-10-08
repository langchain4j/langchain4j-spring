package dev.langchain4j.spring.it;

import dev.langchain4j.spi.CapturedContext;
import dev.langchain4j.spi.ExecutorProvider;
import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.task.TaskDecorator;
import org.springframework.util.ClassUtils;

import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Micrometer context propagation is an optional dependency of the starter, and this application does not have it.
 */
class ExecutorProviderWithoutMicrometerIT {

    private static final ThreadLocal<String> REQUEST_ID = new ThreadLocal<>();

    @Test
    void should_not_have_micrometer_context_propagation_on_the_classpath() {
        // guards the purpose of this test: with Micrometer on the classpath, it would prove nothing
        assertThat(ClassUtils.isPresent("io.micrometer.context.ContextSnapshotFactory", null)).isFalse();
    }

    @Test
    void should_capture_context_with_the_application_task_decorator() throws Exception {
        try (ConfigurableApplicationContext context = start(new SpringApplicationBuilder(TestApplication.class)
                .initializers(applicationContext -> applicationContext.getBeanFactory()
                        .registerSingleton("requestIdTaskDecorator", requestIdTaskDecorator())))) {

            assertThat(runCapturedOnAnotherThread()).isEqualTo("request-42");
        }
    }

    @Test
    void should_capture_nothing_without_a_task_decorator() {
        try (ConfigurableApplicationContext context = start(new SpringApplicationBuilder(TestApplication.class))) {

            Runnable task = () -> {};
            assertThat(ExecutorProvider.get().captureContext().wrap(task)).isSameAs(task);
        }
    }

    private static ConfigurableApplicationContext start(SpringApplicationBuilder application) {
        return application
                .properties("server.port=0", "langchain4j.executor.use-spring-task-executor=true")
                .run();
    }

    private static String runCapturedOnAnotherThread() throws Exception {
        REQUEST_ID.set("request-42");
        CapturedContext captured = ExecutorProvider.get().captureContext();
        REQUEST_ID.remove();

        CompletableFuture<String> seen = new CompletableFuture<>();
        new Thread(captured.wrap(() -> seen.complete(REQUEST_ID.get()))).start();
        return seen.get(10, SECONDS);
    }

    private static TaskDecorator requestIdTaskDecorator() {
        return task -> {
            String captured = REQUEST_ID.get();
            return () -> {
                REQUEST_ID.set(captured);
                try {
                    task.run();
                } finally {
                    REQUEST_ID.remove();
                }
            };
        };
    }
}
