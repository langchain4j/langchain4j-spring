package dev.langchain4j.service.spring;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import org.springframework.stereotype.Service;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static dev.langchain4j.service.spring.AiServiceWiringMode.AUTOMATIC;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * An interface annotated with {@code @AiService} will be automatically registered as a bean
 * and wired with all the following components (beans) available in the context:
 * <pre>
 * - {@link ChatLanguageModel}
 * - {@link StreamingChatLanguageModel}
 * - {@link ChatMemory}
 * - {@link ChatMemoryProvider}
 * - {@link ContentRetriever}
 * - {@link RetrievalAugmentor}
 * - All beans containing methods annotated with {@code @}{@link Tool}
 * </pre>
 * You can also explicitly specify which components (beans) should be wired into this AI Service
 * by setting {@link #wiringMode()} to {@link AiServiceWiringMode#EXPLICIT}
 * and specifying bean names using the following attributes:
 * <pre>
 * - {@link #chatModel()}
 * - {@link #streamingChatModel()}
 * - {@link #chatMemory()}
 * - {@link #chatMemoryProvider()}
 * - {@link #contentRetriever()}
 * - {@link #retrievalAugmentor()}
 * </pre>
 * See more information about AI Services <a href="https://docs.langchain4j.dev/tutorials/ai-services">here</a>
 * and in the Javadoc of {@link AiServices}.
 *
 * @see AiServices
 */
@Service
@Target(TYPE)
@Retention(RUNTIME)
public @interface AiService {

    /**
     * Specifies how LangChain4j components (beans) are wired (injected) into this AI Service.
     */
    AiServiceWiringMode wiringMode() default AUTOMATIC;

    /**
     * When the {@link #wiringMode()} is set to {@link AiServiceWiringMode#EXPLICIT},
     * this attribute specifies the name of a {@link ChatLanguageModel} bean that should be used by this AI Service.
     */
    String chatModel() default "";

    /**
     * When the {@link #wiringMode()} is set to {@link AiServiceWiringMode#EXPLICIT},
     * this attribute specifies the name of a {@link StreamingChatLanguageModel} bean that should be used by this AI Service.
     */
    String streamingChatModel() default "";

    /**
     * When the {@link #wiringMode()} is set to {@link AiServiceWiringMode#EXPLICIT},
     * this attribute specifies the name of a {@link ChatMemory} bean that should be used by this AI Service.
     */
    String chatMemory() default "";

    /**
     * When the {@link #wiringMode()} is set to {@link AiServiceWiringMode#EXPLICIT},
     * this attribute specifies the name of a {@link ChatMemoryProvider} bean that should be used by this AI Service.
     */
    String chatMemoryProvider() default "";

    /**
     * When the {@link #wiringMode()} is set to {@link AiServiceWiringMode#EXPLICIT},
     * this attribute specifies the name of a {@link ContentRetriever} bean that should be used by this AI Service.
     */
    String contentRetriever() default "";

    /**
     * When the {@link #wiringMode()} is set to {@link AiServiceWiringMode#EXPLICIT},
     * this attribute specifies the name of a {@link RetrievalAugmentor} bean that should be used by this AI Service.
     */
    String retrievalAugmentor() default "";

    /**
     * When the {@link #wiringMode()} is set to {@link AiServiceWiringMode#EXPLICIT},
     * this attribute specifies the names of beans containing methods annotated with {@link Tool} that should be used by this AI Service.
     */
    String[] tools() default {};
}