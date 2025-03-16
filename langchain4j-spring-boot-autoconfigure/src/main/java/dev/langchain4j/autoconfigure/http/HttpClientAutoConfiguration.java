package dev.langchain4j.autoconfigure.http;

import dev.langchain4j.http.client.HttpClientBuilder;
import dev.langchain4j.http.client.spring.restclient.SpringRestClientBuilder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnThreading;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.autoconfigure.thread.Threading;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.boot.task.SimpleAsyncTaskExecutorBuilder;
import org.springframework.boot.task.ThreadPoolTaskExecutorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.TaskDecorator;
import org.springframework.core.task.support.ContextPropagatingTaskDecorator;
import org.springframework.web.client.RestClient;

/**
 * Auto-configuration for {@link HttpClientBuilder}.
 */
@AutoConfiguration(after = { RestClientAutoConfiguration.class, TaskExecutionAutoConfiguration.class })
public class HttpClientAutoConfiguration {

    static final String TASK_EXECUTOR_BEAN_NAME = "httpClientTaskExecutor";
    static final String TASK_EXECUTOR_THREAD_NAME_PREFIX = "langchain4j-http-";

    /**
     * A {@link HttpClientBuilder} bean that is used to create {@link dev.langchain4j.http.client.HttpClient}s.
     * It's a prototype bean (not a singleton) to allow for customizing the builder
     * per {@link dev.langchain4j.http.client.HttpClient} instance.
     */
    @Bean
    @ConditionalOnMissingBean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    HttpClientBuilder httpClientBuilder(
            @Qualifier(TASK_EXECUTOR_BEAN_NAME) AsyncTaskExecutor asyncTaskExecutor,
            ObjectProvider<RestClient.Builder> restClientBuilder,
            ObjectProvider<HttpClientBuilderCustomizer> customizers
    ) {
        HttpClientBuilder httpClientBuilder = new SpringRestClientBuilder()
                .streamingRequestExecutor(asyncTaskExecutor)
                .restClientBuilder(restClientBuilder.getIfAvailable(RestClient::builder));
        customizers.orderedStream().forEach(customizer -> customizer.customize(httpClientBuilder));
        return httpClientBuilder;
    }

    @Bean(TASK_EXECUTOR_BEAN_NAME)
    @ConditionalOnMissingBean(name = TASK_EXECUTOR_BEAN_NAME)
    @ConditionalOnThreading(Threading.VIRTUAL)
    AsyncTaskExecutor httpClientVirtualThreadsTaskExecutor(ObjectProvider<SimpleAsyncTaskExecutorBuilder> builder, ObjectProvider<TaskDecorator> taskDecorator) {
        return builder.getIfAvailable(() -> defaultSimpleAsyncTaskExecutorBuilder(taskDecorator))
                .threadNamePrefix(TASK_EXECUTOR_THREAD_NAME_PREFIX)
                .build();
    }

    @Bean(TASK_EXECUTOR_BEAN_NAME)
    @ConditionalOnMissingBean(name = TASK_EXECUTOR_BEAN_NAME)
    @ConditionalOnThreading(Threading.PLATFORM)
    AsyncTaskExecutor httpClientThreadPoolTaskExecutor(ObjectProvider<ThreadPoolTaskExecutorBuilder> builder, ObjectProvider<TaskDecorator> taskDecorator) {
        return builder.getIfAvailable(() -> defaultThreadPoolTaskExecutorBuilder(taskDecorator))
                .threadNamePrefix(TASK_EXECUTOR_THREAD_NAME_PREFIX)
                .build();
    }

    /**
     * This is picked up by {@link TaskExecutionAutoConfiguration}.
     * In case the autoconfiguration is disabled, we use this bean
     * explicitly to build default {@link AsyncTaskExecutor}s.
     */
    @Bean
    @ConditionalOnClass(name = "io.micrometer.context.ContextSnapshotFactory")
    TaskDecorator contextPropagatingTaskDecorator() {
        return new ContextPropagatingTaskDecorator();
    }

    private SimpleAsyncTaskExecutorBuilder defaultSimpleAsyncTaskExecutorBuilder(ObjectProvider<TaskDecorator> taskDecorator) {
        var builder = new SimpleAsyncTaskExecutorBuilder().virtualThreads(true);
        taskDecorator.ifAvailable(builder::taskDecorator);
        return builder;
    }

    private ThreadPoolTaskExecutorBuilder defaultThreadPoolTaskExecutorBuilder(ObjectProvider<TaskDecorator> taskDecorator) {
        var builder = new ThreadPoolTaskExecutorBuilder();
        taskDecorator.ifAvailable(builder::taskDecorator);
        return builder;
    }

}
