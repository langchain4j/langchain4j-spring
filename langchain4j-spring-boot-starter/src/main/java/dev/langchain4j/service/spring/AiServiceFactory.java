package dev.langchain4j.service.spring;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.moderation.ModerationModel;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.tool.DefaultToolExecutor;
import dev.langchain4j.service.tool.ToolExecutor;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.langchain4j.agent.tool.ToolSpecifications.toolSpecificationFrom;
import static dev.langchain4j.internal.Utils.isNullOrEmpty;

class AiServiceFactory implements FactoryBean<Object> {

    private final Class<Object> aiServiceClass;
    private ChatLanguageModel chatLanguageModel;
    private StreamingChatLanguageModel streamingChatLanguageModel;
    private ChatMemory chatMemory;
    private ChatMemoryProvider chatMemoryProvider;
    private ContentRetriever contentRetriever;
    private RetrievalAugmentor retrievalAugmentor;
    private ModerationModel moderationModel;
    private List<Object> tools;

    public AiServiceFactory(Class<Object> aiServiceClass) {
        this.aiServiceClass = aiServiceClass;
    }

    public void setChatLanguageModel(ChatLanguageModel chatLanguageModel) {
        this.chatLanguageModel = chatLanguageModel;
    }

    public void setStreamingChatLanguageModel(StreamingChatLanguageModel streamingChatLanguageModel) {
        this.streamingChatLanguageModel = streamingChatLanguageModel;
    }

    public void setChatMemory(ChatMemory chatMemory) {
        this.chatMemory = chatMemory;
    }

    public void setChatMemoryProvider(ChatMemoryProvider chatMemoryProvider) {
        this.chatMemoryProvider = chatMemoryProvider;
    }

    public void setContentRetriever(ContentRetriever contentRetriever) {
        this.contentRetriever = contentRetriever;
    }

    public void setRetrievalAugmentor(RetrievalAugmentor retrievalAugmentor) {
        this.retrievalAugmentor = retrievalAugmentor;
    }

    public void setModerationModel(ModerationModel moderationModel) {
        this.moderationModel = moderationModel;
    }

    public void setTools(List<Object> tools) {
        this.tools = tools;
    }

    @Override
    public Object getObject() {

        AiServices<Object> builder = AiServices.builder(aiServiceClass);

        if (chatLanguageModel != null) {
            builder = builder.chatLanguageModel(chatLanguageModel);
        }

        if (streamingChatLanguageModel != null) {
            builder = builder.streamingChatLanguageModel(streamingChatLanguageModel);
        }

        if (chatMemory != null) {
            builder.chatMemory(chatMemory);
        }

        if (chatMemoryProvider != null) {
            builder.chatMemoryProvider(chatMemoryProvider);
        }

        if (retrievalAugmentor != null) {
            builder = builder.retrievalAugmentor(retrievalAugmentor);
        } else if (contentRetriever != null) {
            builder = builder.contentRetriever(contentRetriever);
        }

        if (moderationModel != null) {
            builder = builder.moderationModel(moderationModel);
        }

        if (!isNullOrEmpty(tools)) {
            builder = builder.tools(tools);
            builder = builder.tools(aopEnhancedTools());
        }
        
        return builder.build();
    }

    @Override
    public Class<?> getObjectType() {
        return aiServiceClass;
    }

    @Override
    public boolean isSingleton() {
        return true; // TODO
    }

    /**
     * TODO
     *  getObjectType() getObject() invocations may arrive early in the bootstrap process, even ahead of any post-processor setup.
     * <p>
     * TODO
     * The container is only responsible for managing the lifecycle of the FactoryBean instance, not the lifecycle
     * of the objects created by the FactoryBean. Therefore, a destroy method on an exposed bean object
     * (such as java.io.Closeable.close()) will not be called automatically.
     * Instead, a FactoryBean should implement DisposableBean and delegate any such close call to the underlying object.
     */

    private Map<ToolSpecification, ToolExecutor> aopEnhancedTools() {
        Map<ToolSpecification, ToolExecutor> toolExecutors = new HashMap<>();
        tools.stream().filter(AopUtils::isAopProxy).forEach(enhancedTool -> {
            Class<?> originalToolClass = AopProxyUtils.ultimateTargetClass(enhancedTool);
            for (Method method : originalToolClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Tool.class)) {
                    Arrays.stream(enhancedTool.getClass().getDeclaredMethods())
                          .filter(m -> m.getName().equals(method.getName()))
                          .findFirst()
                          .ifPresent(enhancedMethod -> toolExecutors.put(toolSpecificationFrom(method),
                                  new DefaultToolExecutor(enhancedTool, enhancedMethod)));
                }
            }
        });
        return toolExecutors;
    }
}
