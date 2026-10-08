package dev.langchain4j.service.spring;

import dev.langchain4j.spi.ExecutorProvider;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.concurrent.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;

/**
 * Routes the work LangChain4j runs off the caller thread - blocking tools, offloaded retrieval, retry backoff -
 * through Spring's application {@link AsyncTaskExecutor} instead of LangChain4j's own default executor. That is the
 * executor Spring Boot auto-configures under the name {@code applicationTaskExecutor}; if the application replaced
 * it, its own {@link AsyncTaskExecutor} is used, provided there is only one.
 * <p>
 * This is what makes ambient context follow an asynchronous or reactive AI Service invocation: LangChain4j does
 * not copy {@code ThreadLocal} state across its thread hops, so tracing spans, MDC and security context survive
 * only if the executor itself propagates them. Spring's executor does when it is configured with a
 * {@code TaskDecorator} (Micrometer's context propagation and Spring Security both install one), and using it
 * also means LangChain4j honours the pool sizing the application already declares under
 * {@code spring.task.execution.*} rather than creating threads of its own.
 * <p>
 * Off by default, because {@link ExecutorProvider#set(ExecutorProvider)} is process-wide rather than scoped to an
 * application context: switching it on changes the executor for every LangChain4j usage in the JVM. Enable it
 * with:
 *
 * <pre>
 * langchain4j.executor.use-spring-task-executor=true
 * </pre>
 *
 * A programmatically registered provider is restored when the context shuts down, so a test that starts and stops
 * a context does not leak its executor into the next one.
 */
@Configuration
@ConditionalOnProperty(name = "langchain4j.executor.use-spring-task-executor", havingValue = "true")
public class ExecutorProviderAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ExecutorProviderAutoConfiguration.class);

    private final ObjectProvider<AsyncTaskExecutor> applicationTaskExecutor;
    private final ObjectProvider<AsyncTaskExecutor> taskExecutors;
    private ExecutorProvider installed;
    private ExecutorProvider previous;

    public ExecutorProviderAutoConfiguration(
            @Qualifier(TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME)
            ObjectProvider<AsyncTaskExecutor> applicationTaskExecutor,
            ObjectProvider<AsyncTaskExecutor> taskExecutors) {
        this.applicationTaskExecutor = applicationTaskExecutor;
        this.taskExecutors = taskExecutors;
    }

    @PostConstruct
    void useSpringTaskExecutor() {
        // other AsyncTaskExecutor beans may exist, for example the ones the provider starters create for streaming
        Executor executor = applicationTaskExecutor.getIfAvailable(taskExecutors::getIfUnique);
        if (executor == null) {
            log.warn("langchain4j.executor.use-spring-task-executor is enabled, but neither an AsyncTaskExecutor bean"
                    + " named '" + TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME + "' nor a single"
                    + " other AsyncTaskExecutor bean was found. LangChain4j keeps using its own default executor.");
            return;
        }
        previous = ExecutorProvider.get();
        installed = () -> executor;
        ExecutorProvider.set(installed);
        log.debug("LangChain4j will run offloaded work on the Spring task executor: {}", executor);
    }

    @PreDestroy
    void restorePreviousProvider() {
        if (installed != null && ExecutorProvider.get() == installed) {
            ExecutorProvider.set(previous);
        }
    }
}
