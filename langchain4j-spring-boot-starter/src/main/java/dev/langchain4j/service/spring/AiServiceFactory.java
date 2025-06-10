package dev.langchain4j.service.spring;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.moderation.ModerationModel;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.tool.DefaultToolExecutor;
import dev.langchain4j.service.tool.ToolExecutor;
import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.langchain4j.agent.tool.ToolSpecifications.toolSpecificationFrom;
import static dev.langchain4j.internal.Utils.isNullOrEmpty;
import static org.springframework.aop.framework.AopProxyUtils.ultimateTargetClass;
import static org.springframework.aop.support.AopUtils.isAopProxy;

class AiServiceFactory implements FactoryBean<Object> {

    private final Class<Object> aiServiceClass;
    private ChatModel chatModel;
    private StreamingChatModel streamingChatModel;
    private ChatMemory chatMemory;
    private ChatMemoryProvider chatMemoryProvider;
    private ContentRetriever contentRetriever;
    private RetrievalAugmentor retrievalAugmentor;
    private ModerationModel moderationModel;
    private List<Object> tools;

    public AiServiceFactory(Class<Object> aiServiceClass) {
        this.aiServiceClass = aiServiceClass;
    }

    public void setChatModel(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    public void setStreamingChatModel(StreamingChatModel streamingChatModel) {
        this.streamingChatModel = streamingChatModel;
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

        if (chatModel != null) {
            builder = builder.chatModel(chatModel);
        }

        if (streamingChatModel != null) {
            builder = builder.streamingChatModel(streamingChatModel);
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
            for (Object tool : tools) {
                if (isAopProxy(tool)) {
                    builder = builder.tools(aopEnhancedTools(tool));
                } else {
                    builder = builder.tools(tool);
                }
            }
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

    private Map<ToolSpecification, ToolExecutor> aopEnhancedTools(Object enhancedTool) {
        Map<ToolSpecification, ToolExecutor> toolExecutors = new HashMap<>();
        Class<?> originalToolClass = ultimateTargetClass(enhancedTool);
        for (Method originalToolMethod : originalToolClass.getDeclaredMethods()) {
            if (originalToolMethod.isAnnotationPresent(Tool.class)) {
                Arrays.stream(enhancedTool.getClass().getDeclaredMethods())
                        // TODO match by complete method signature, not only by name (there can be multiple methods with the same name)
                        .filter(m -> m.getName().equals(originalToolMethod.getName()))
                        .findFirst()
                        .ifPresent(enhancedToolMethod -> {
                            ToolSpecification toolSpecification = toolSpecificationFrom(originalToolMethod);
                            ToolExecutor executor = new DefaultToolExecutor(enhancedTool, originalToolMethod, enhancedToolMethod);
                            toolExecutors.put(toolSpecification, executor);
                        });
            }
        }
        return toolExecutors;
    }
}
