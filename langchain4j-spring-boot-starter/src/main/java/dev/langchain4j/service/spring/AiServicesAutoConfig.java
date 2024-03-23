package dev.langchain4j.service.spring;

import dev.langchain4j.exception.IllegalConfigurationException;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import org.reflections.Reflections;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;

import static dev.langchain4j.exception.IllegalConfigurationException.illegalConfiguration;
import static dev.langchain4j.internal.Utils.isNotNullOrBlank;
import static dev.langchain4j.internal.Utils.isNullOrBlank;

public class AiServicesAutoConfig {

    @Bean
    BeanFactoryPostProcessor aiServicesRegisteringBeanFactoryPostProcessor() {
        return new BeanFactoryPostProcessor() {

            @Override
            public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

//            Set<Class<?>> allToolBeans = new HashSet<>();
//
//            for (String beanName : registry.getBeanDefinitionNames()) {
//                BeanDefinition beanDefinition = registry.getBeanDefinition(beanName);
//                try {
//                    Class<?> beanClass = Class.forName(beanDefinition.getBeanClassName());
//                    for (Method beanMethod : beanClass.getDeclaredMethods()) {
//                        if (beanMethod.isAnnotationPresent(Tool.class)) {
//                            allToolBeans.add(beanClass);
//                        }
//                    }
//                } catch (Exception e) {
//                    // TODO
//                }
//            }

                String[] app = beanFactory.getBeanNamesForAnnotation(SpringBootApplication.class);
                String basePackage = beanFactory.getBeanDefinition(app[0]).getResolvableType().resolve().getPackage().getName();

                String[] chatLanguageModels = beanFactory.getBeanNamesForType(ChatLanguageModel.class);
                String[] streamingChatLanguageModels = beanFactory.getBeanNamesForType(StreamingChatLanguageModel.class);
                String[] chatMemories = beanFactory.getBeanNamesForType(ChatMemory.class);
                String[] chatMemoryProviders = beanFactory.getBeanNamesForType(ChatMemoryProvider.class);
                String[] contentRetrievers = beanFactory.getBeanNamesForType(ContentRetriever.class);
                String[] retrievalAugmentors = beanFactory.getBeanNamesForType(RetrievalAugmentor.class);

                Reflections reflections = new Reflections(basePackage);
                reflections.getTypesAnnotatedWith(AiService.class).forEach(aiService -> {

                    if (beanFactory.getBeanNamesForType(aiService).length > 0) {
                        // User probably wants to configure AI Service bean manually
                        // TODO or better fail because he should not annotate it with @AiService then?
                        return;
                    }

                    AiService annotation = aiService.getAnnotation(AiService.class);

                    GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
                    beanDefinition.setBeanClass(AiServiceFactory.class);
                    beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(aiService);
                    MutablePropertyValues propertyValues = beanDefinition.getPropertyValues();

                    if (isNotNullOrBlank(annotation.chatModel())) {
                        propertyValues.add("chatLanguageModel", new RuntimeBeanReference(annotation.chatModel()));
                    } else {
                        if (chatLanguageModels.length == 1) {
                            propertyValues.add("chatLanguageModel", new RuntimeBeanReference(chatLanguageModels[0]));
                        } else if (chatLanguageModels.length > 1) {
                            throw conflict(ChatLanguageModel.class, chatLanguageModels);
                        }
                    }

                    // TODO handle conflicts for all other components

                    if (streamingChatLanguageModels.length == 1) {
                        propertyValues.add("streamingChatLanguageModel", new RuntimeBeanReference(streamingChatLanguageModels[0]));
                    }

                    if (chatMemories.length == 1) {
                        propertyValues.add("chatMemory", new RuntimeBeanReference(chatMemories[0]));
                    }

                    if (chatMemoryProviders.length == 1) {
                        propertyValues.add("chatMemoryProvider", new RuntimeBeanReference(chatMemoryProviders[0]));
                    }

                    if (contentRetrievers.length == 1) {
                        propertyValues.add("contentRetriever", new RuntimeBeanReference(contentRetrievers[0]));
                    }

                    if (retrievalAugmentors.length == 1) {
                        propertyValues.add("retrievalAugmentor", new RuntimeBeanReference(retrievalAugmentors[0]));
                    }

                    for (Class<?> classWithTools : annotation.tools()) {
                        for (String beanWithTools : beanFactory.getBeanNamesForType(classWithTools)) {
                            propertyValues.add("beanWithTools", new RuntimeBeanReference(beanWithTools));
                        }
                    }

                    BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
                    registry.registerBeanDefinition(lowercaseFirstLetter(aiService.getSimpleName()), beanDefinition);
                });
            }
        };
    }

    private static IllegalConfigurationException conflict(Class<?> beanType, Object[] beanNames) {
        return illegalConfiguration("Conflict: multiple beans of type %s are found: %s. " +
                "Please specify which one you wish to use in the @AiService annotation like this: " +
                "@AiService(chatModel = \"<beanName>\").", beanType.getName(), Arrays.toString(beanNames));
    }

    private static String lowercaseFirstLetter(String text) {
        if (isNullOrBlank(text)) {
            return text;
        }
        return text.substring(0, 1).toLowerCase() + text.substring(1);
    }
}
