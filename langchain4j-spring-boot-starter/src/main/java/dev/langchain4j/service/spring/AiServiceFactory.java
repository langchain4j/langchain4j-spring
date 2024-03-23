package dev.langchain4j.service.spring;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.FactoryBean;

import java.util.ArrayList;
import java.util.List;

import static dev.langchain4j.internal.Utils.isNullOrEmpty;

class AiServiceFactory implements FactoryBean<Object> {

    private final Class<Object> aiServiceClass;
    private ChatLanguageModel chatLanguageModel;
    private StreamingChatLanguageModel streamingChatLanguageModel;
    private ChatMemory chatMemory;
    private ChatMemoryProvider chatMemoryProvider;
    private ContentRetriever contentRetriever;
    private RetrievalAugmentor retrievalAugmentor;
    private final List<Object> beansWithTools = new ArrayList<>();

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

    public void setBeanWithTools(Object beanWithTools) {
        this.beansWithTools.add(beanWithTools);
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

        if (contentRetriever != null) {
            builder = builder.contentRetriever(contentRetriever);
        }

        if (retrievalAugmentor != null) {
            builder = builder.retrievalAugmentor(retrievalAugmentor);
        }

        if (!isNullOrEmpty(beansWithTools)) {
            builder = builder.tools(beansWithTools);
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
}
