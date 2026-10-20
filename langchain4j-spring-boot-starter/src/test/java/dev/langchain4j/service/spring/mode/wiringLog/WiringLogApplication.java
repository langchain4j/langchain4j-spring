package dev.langchain4j.service.spring.mode.wiringLog;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;

import java.util.Map;
import java.util.TreeMap;

/**
 * All components are lazy and count their initializations, so that tests can detect
 * beans initialized earlier (during bean factory post-processing) or more often than expected.
 */
@SpringBootApplication
class WiringLogApplication {

    static final String CHAT_MODEL_BEAN_NAME = "chatModel";
    static final String CHAT_MEMORY_BEAN_NAME = "chatMemory";
    static final String UNUSED_BEAN_NAME = "unusedComponent";

    private static final Map<String, Integer> initializations = new TreeMap<>();
    private static Map<String, Integer> initializationsBeforeBeanPostProcessors = Map.of();

    static synchronized void recordInitialization(String beanName) {
        initializations.merge(beanName, 1, Integer::sum);
    }

    static synchronized Map<String, Integer> initializations() {
        return Map.copyOf(initializations);
    }

    static synchronized Map<String, Integer> initializationsBeforeBeanPostProcessors() {
        return initializationsBeforeBeanPostProcessors;
    }

    static synchronized void resetInitializations() {
        initializations.clear();
        initializationsBeforeBeanPostProcessors = Map.of();
    }

    @Bean(CHAT_MODEL_BEAN_NAME)
    @Lazy
    ChatModel chatModel() {
        recordInitialization(CHAT_MODEL_BEAN_NAME);
        return new RecordingChatModel();
    }

    @Bean(CHAT_MEMORY_BEAN_NAME)
    @Lazy
    ChatMemory chatMemory() {
        recordInitialization(CHAT_MEMORY_BEAN_NAME);
        return MessageWindowChatMemory.withMaxMessages(10);
    }

    @Bean(UNUSED_BEAN_NAME)
    @Lazy
    UnusedComponent unusedComponent() {
        recordInitialization(UNUSED_BEAN_NAME);
        return new UnusedComponent();
    }

    /**
     * Bean post processors are instantiated after all bean factory post processors (including the one
     * registering AI services) have run, so anything initialized before this point was initialized prematurely.
     */
    @Bean
    static BeanPostProcessor initializationSnapshot() {
        synchronized (WiringLogApplication.class) {
            initializationsBeforeBeanPostProcessors = Map.copyOf(initializations);
        }
        return new BeanPostProcessor() {
        };
    }

    static class UnusedComponent {
    }
}
