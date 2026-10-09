package dev.langchain4j.service.spring;

import dev.langchain4j.spi.CapturedContext;
import dev.langchain4j.spi.ExecutorProvider;
import org.springframework.util.ClassUtils;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.function.Supplier;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Lock;
import io.micrometer.context.ContextSnapshotFactory;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.concurrent.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.TaskDecorator;

/**
 * Routes the work LangChain4j runs off the caller thread - blocking tools, offloaded retrieval, retry backoff -
 * through Spring's application {@link AsyncTaskExecutor} instead of LangChain4j's own default executor. That is the
 * executor Spring Boot auto-configures under the name {@code applicationTaskExecutor}; if the application replaced
 * it, its own {@link AsyncTaskExecutor} is used, provided there is only one.
 * <p>
 * This is what makes ambient context follow an asynchronous or reactive AI Service invocation. LangChain4j does
 * not copy {@code ThreadLocal} state across its thread hops, and most offloaded work (for example the tools called
 * after the model has answered) is submitted from a thread that does not carry the caller's context. So the
 * context is captured on the caller's thread when an AI Service method is called, and restored in every task that
 * the invocation offloads. It is captured with the application's {@link TaskDecorator} bean, the one Spring Boot
 * applies to its task executor (for example a {@code ContextPropagatingTaskDecorator}, with Micrometer context
 * propagation). Like Spring Boot, LangChain4j ignores the {@code TaskDecorator} beans when there are several of
 * them. Without a {@code TaskDecorator}, context is captured with Micrometer context propagation if it is on the
 * classpath. Using the application's executor also means LangChain4j honours the pool sizing the application
 * already declares under {@code spring.task.execution.*} rather than creating threads of its own.
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
public class ExecutorProviderAutoConfig implements BeanClassLoaderAware {

    private static final Logger log = LoggerFactory.getLogger(ExecutorProviderAutoConfig.class);

    private final ObjectProvider<AsyncTaskExecutor> applicationTaskExecutor;
    private final ObjectProvider<AsyncTaskExecutor> taskExecutors;
    private final ObjectProvider<TaskDecorator> taskDecorators;
    private ClassLoader beanClassLoader;
    private ExecutorProvider installed;
    private ExecutorProvider previous;

    @Autowired
    public ExecutorProviderAutoConfig(
            @Qualifier(TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME)
            ObjectProvider<AsyncTaskExecutor> applicationTaskExecutor,
            ObjectProvider<AsyncTaskExecutor> taskExecutors,
            ObjectProvider<TaskDecorator> taskDecorators) {
        this.applicationTaskExecutor = applicationTaskExecutor;
        this.taskExecutors = taskExecutors;
        this.taskDecorators = taskDecorators;
    }

    /**
     * @deprecated use {@link #ExecutorProviderAutoConfig(ObjectProvider, ObjectProvider, ObjectProvider)} instead;
     * this constructor captures context only with Micrometer context propagation, never with a {@link TaskDecorator}
     */
    @Deprecated
    public ExecutorProviderAutoConfig(
            @Qualifier(TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME)
            ObjectProvider<AsyncTaskExecutor> applicationTaskExecutor,
            ObjectProvider<AsyncTaskExecutor> taskExecutors) {
        this(applicationTaskExecutor, taskExecutors, new StaticListableBeanFactory().getBeanProvider(TaskDecorator.class));
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
        installed = new SpringTaskExecutorProvider(executor, contextCapture());
        ExecutorProvider.set(installed);
        log.debug("LangChain4j will run offloaded work on the Spring task executor: {}", executor);
    }

    @PreDestroy
    void restorePreviousProvider() {
        if (installed != null && ExecutorProvider.get() == installed) {
            ExecutorProvider.set(previous);
        }
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.beanClassLoader = classLoader;
    }

    /**
     * Captures context with the application's {@link TaskDecorator}, or, without one, with Micrometer context
     * propagation if it is on the classpath.
     */
    private Supplier<CapturedContext> contextCapture() {
        TaskDecorator decorator = taskDecorators.getIfUnique();
        if (decorator != null) {
            return () -> captureWith(decorator);
        }
        boolean micrometerPresent = ClassUtils.isPresent(MICROMETER_CONTEXT_SNAPSHOT_FACTORY, beanClassLoader);
        return micrometerPresent ? MicrometerContextCapture::captureAll : null;
    }

    /**
     * A {@link TaskDecorator} captures the context of the thread that calls {@link TaskDecorator#decorate(Runnable)},
     * and a decorated {@code Runnable} must not run concurrently with itself: some keep state while running, as
     * Spring Security's {@code DelegatingSecurityContextRunnable} does. So a placeholder is decorated on the caller's
     * thread, and each task briefly runs it, one at a time, to restore the captured context and decorate the task
     * within it. The task then runs with a decorated {@code Runnable} of its own, concurrently with the other tasks.
     */
    private static CapturedContext captureWith(TaskDecorator decorator) {
        Runnable inCapturedContext = decorator.decorate(() -> IN_CAPTURED_CONTEXT.get().run());
        Lock lock = new ReentrantLock();
        return task -> () -> {
            Runnable[] decoratedTask = new Runnable[1];
            lock.lock();
            try {
                IN_CAPTURED_CONTEXT.set(() -> decoratedTask[0] = decorator.decorate(task));
                inCapturedContext.run();
            } finally {
                IN_CAPTURED_CONTEXT.remove();
                lock.unlock();
            }
            decoratedTask[0].run();
        };
    }

    private static final ThreadLocal<Runnable> IN_CAPTURED_CONTEXT = new ThreadLocal<>();

    private static final String MICROMETER_CONTEXT_SNAPSHOT_FACTORY = "io.micrometer.context.ContextSnapshotFactory";

    /**
     * Micrometer context propagation is optional: this class is only loaded when it is on the classpath.
     */
    private static final class MicrometerContextCapture {

        private static final ContextSnapshotFactory CONTEXT_SNAPSHOT_FACTORY = ContextSnapshotFactory.builder().build();

        static CapturedContext captureAll() {
            return CONTEXT_SNAPSHOT_FACTORY.captureAll()::wrap;
        }
    }

    private record SpringTaskExecutorProvider(Executor executor, Supplier<CapturedContext> contextCapture)
            implements ExecutorProvider {

        @Override
        public CapturedContext captureContext() {
            return contextCapture == null ? ExecutorProvider.super.captureContext() : contextCapture.get();
        }
    }
}
