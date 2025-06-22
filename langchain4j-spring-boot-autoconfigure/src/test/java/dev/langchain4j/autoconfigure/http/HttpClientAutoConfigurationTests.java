package dev.langchain4j.autoconfigure.http;

import dev.langchain4j.http.client.HttpClientBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.condition.JRE;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import static dev.langchain4j.autoconfigure.http.HttpClientAutoConfiguration.TASK_EXECUTOR_BEAN_NAME;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link HttpClientAutoConfiguration}.
 */
class HttpClientAutoConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(HttpClientAutoConfiguration.class));

    @Test
    void httpClientBuilderWhenAutoConfiguredRestClient() {
        contextRunner
                .withConfiguration(AutoConfigurations.of(RestClientAutoConfiguration.class))
                .run(context -> {
                    assertThat(context).hasSingleBean(HttpClientBuilder.class);
                });
    }

    @Test
    void httpClientBuilderWhenNoAutoConfiguredRestClient() {
        contextRunner
                .run(context -> {
                    assertThat(context).hasSingleBean(HttpClientBuilder.class);
                });
    }

    @Test
    void httpClientBuilderWhenAutoConfiguredThreadPoolTaskExecutor() {
        contextRunner
                .withConfiguration(AutoConfigurations.of(TaskExecutionAutoConfiguration.class))
                .run(context -> {
                    assertThat(context).hasSingleBean(HttpClientBuilder.class);
                    assertThat(context).getBeans(AsyncTaskExecutor.class).hasSize(2);
                    assertThat(context).getBean(TASK_EXECUTOR_BEAN_NAME).isInstanceOf(ThreadPoolTaskExecutor.class);
                });
    }

    @Test
    void httpClientBuilderWhenNoAutoConfiguredThreadPoolTaskExecutor() {
        contextRunner
                .run(context -> {
                    assertThat(context).hasSingleBean(HttpClientBuilder.class);
                    assertThat(context).hasSingleBean(AsyncTaskExecutor.class);
                    assertThat(context).getBean(TASK_EXECUTOR_BEAN_NAME).isInstanceOf(ThreadPoolTaskExecutor.class);
                });
    }

    @Test
    @EnabledForJreRange(min = JRE.JAVA_21)
    void httpClientBuilderWhenAutoConfiguredVirtualThreadsTaskExecutor() {
        contextRunner
                .withPropertyValues("spring.threads.virtual.enabled=true")
                .withConfiguration(AutoConfigurations.of(TaskExecutionAutoConfiguration.class))
                .run(context -> {
                    assertThat(context).hasSingleBean(HttpClientBuilder.class);
                    assertThat(context).getBeans(AsyncTaskExecutor.class).hasSize(2);
                    assertThat(context).getBean(TASK_EXECUTOR_BEAN_NAME).isInstanceOf(SimpleAsyncTaskExecutor.class);
                });
    }

    @Test
    @EnabledForJreRange(min = JRE.JAVA_21)
    void httpClientBuilderWhenNoAutoConfiguredVirtualThreadsTaskExecutor() {
        contextRunner
                .withPropertyValues("spring.threads.virtual.enabled=true")
                .run(context -> {
                    assertThat(context).hasSingleBean(HttpClientBuilder.class);
                    assertThat(context).hasSingleBean(AsyncTaskExecutor.class);
                    assertThat(context).getBean(TASK_EXECUTOR_BEAN_NAME).isInstanceOf(SimpleAsyncTaskExecutor.class);
                });
    }

}
