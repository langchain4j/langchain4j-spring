package dev.langchain4j.service.spring;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.agent.tool.ToolSpecifications;
import dev.langchain4j.exception.IllegalConfigurationException;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.moderation.ModerationModel;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.spring.event.AiServiceRegisteredEvent;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.annotation.Bean;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static dev.langchain4j.exception.IllegalConfigurationException.illegalConfiguration;
import static dev.langchain4j.internal.Exceptions.illegalArgument;
import static dev.langchain4j.internal.Utils.isNotNullOrBlank;
import static dev.langchain4j.internal.Utils.isNullOrBlank;
import static dev.langchain4j.service.spring.AiServiceWiringMode.AUTOMATIC;
import static dev.langchain4j.service.spring.AiServiceWiringMode.EXPLICIT;
import static java.util.Arrays.asList;

public class AiServicesAutoConfig implements ApplicationEventPublisherAware {

    private ApplicationEventPublisher eventPublisher;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.eventPublisher = applicationEventPublisher;
    }

    @Bean
    BeanFactoryPostProcessor aiServicesRegisteringBeanFactoryPostProcessor() {
        return beanFactory -> {

            // all components available in the application context
            String[] chatLanguageModels = beanFactory.getBeanNamesForType(ChatLanguageModel.class);
            String[] streamingChatLanguageModels = beanFactory.getBeanNamesForType(StreamingChatLanguageModel.class);
            String[] chatMemories = beanFactory.getBeanNamesForType(ChatMemory.class);
            String[] chatMemoryProviders = beanFactory.getBeanNamesForType(ChatMemoryProvider.class);
            String[] contentRetrievers = beanFactory.getBeanNamesForType(ContentRetriever.class);
            String[] retrievalAugmentors = beanFactory.getBeanNamesForType(RetrievalAugmentor.class);
            String[] moderationModels = beanFactory.getBeanNamesForType(ModerationModel.class);

            Set<String> tools = new HashSet<>();
            Map<String, ToolSpecification> toolSpecifications = new HashMap<>();
            for (String beanName : beanFactory.getBeanDefinitionNames()) {
                try {
                    Class<?> beanClass = Class.forName(beanFactory.getBeanDefinition(beanName).getBeanClassName());
                    for (Method beanMethod : beanClass.getDeclaredMethods()) {
                        if (beanMethod.isAnnotationPresent(Tool.class)) {
                            if (tools.add(beanName)) {
                                toolSpecifications.put(beanName, ToolSpecifications.toolSpecificationFrom(beanMethod));
                            }
                        }
                    }
                } catch (Exception e) {
                    // TODO
                }
            }

            String[] aiServices = beanFactory.getBeanNamesForAnnotation(AiService.class);
            for (String aiService : aiServices) {
                Class<?> aiServiceClass = beanFactory.getType(aiService);

                GenericBeanDefinition aiServiceBeanDefinition = new GenericBeanDefinition();
                aiServiceBeanDefinition.setBeanClass(AiServiceFactory.class);
                aiServiceBeanDefinition.getConstructorArgumentValues().addGenericArgumentValue(aiServiceClass);
                MutablePropertyValues propertyValues = aiServiceBeanDefinition.getPropertyValues();

                AiService aiServiceAnnotation = aiServiceClass.getAnnotation(AiService.class);
                addBeanReference(
                        ChatLanguageModel.class,
                        aiServiceAnnotation,
                        aiServiceAnnotation.chatModel(),
                        chatLanguageModels,
                        "chatModel",
                        "chatLanguageModel",
                        propertyValues
                );

                addBeanReference(
                        StreamingChatLanguageModel.class,
                        aiServiceAnnotation,
                        aiServiceAnnotation.streamingChatModel(),
                        streamingChatLanguageModels,
                        "streamingChatModel",
                        "streamingChatLanguageModel",
                        propertyValues
                );

                addBeanReference(
                        ChatMemory.class,
                        aiServiceAnnotation,
                        aiServiceAnnotation.chatMemory(),
                        chatMemories,
                        "chatMemory",
                        "chatMemory",
                        propertyValues
                );

                addBeanReference(
                        ChatMemoryProvider.class,
                        aiServiceAnnotation,
                        aiServiceAnnotation.chatMemoryProvider(),
                        chatMemoryProviders,
                        "chatMemoryProvider",
                        "chatMemoryProvider",
                        propertyValues
                );

                addBeanReference(
                        ContentRetriever.class,
                        aiServiceAnnotation,
                        aiServiceAnnotation.contentRetriever(),
                        contentRetrievers,
                        "contentRetriever",
                        "contentRetriever",
                        propertyValues
                );

                addBeanReference(
                        RetrievalAugmentor.class,
                        aiServiceAnnotation,
                        aiServiceAnnotation.retrievalAugmentor(),
                        retrievalAugmentors,
                        "retrievalAugmentor",
                        "retrievalAugmentor",
                        propertyValues
                );

                addBeanReference(
                        ModerationModel.class,
                        aiServiceAnnotation,
                        aiServiceAnnotation.moderationModel(),
                        moderationModels,
                        "moderationModel",
                        "moderationModel",
                        propertyValues
                );
                AiServiceRegisteredEvent registeredEvent;
                if (aiServiceAnnotation.wiringMode() == EXPLICIT) {
                    propertyValues.add("tools", toManagedList(asList(aiServiceAnnotation.tools())));
                    registeredEvent = buildEvent(aiServiceClass, toolSpecifications, asList(aiServiceAnnotation.tools()));
                } else if (aiServiceAnnotation.wiringMode() == AUTOMATIC) {
                    propertyValues.add("tools", toManagedList(tools));
                    registeredEvent = buildEvent(aiServiceClass, toolSpecifications, tools);
                } else {
                    throw illegalArgument("Unknown wiring mode: " + aiServiceAnnotation.wiringMode());
                }

                BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
                registry.removeBeanDefinition(aiService);
                registry.registerBeanDefinition(lowercaseFirstLetter(aiService), aiServiceBeanDefinition);

                if (eventPublisher != null) {
                    eventPublisher.publishEvent(registeredEvent);
                }
            }
        };
    }

    private static void addBeanReference(Class<?> beanType,
                                         AiService aiServiceAnnotation,
                                         String customBeanName,
                                         String[] beanNames,
                                         String annotationAttributeName,
                                         String factoryPropertyName,
                                         MutablePropertyValues propertyValues) {
        if (aiServiceAnnotation.wiringMode() == EXPLICIT) {
            if (isNotNullOrBlank(customBeanName)) {
                propertyValues.add(factoryPropertyName, new RuntimeBeanReference(customBeanName));
            }
        } else if (aiServiceAnnotation.wiringMode() == AUTOMATIC) {
            if (beanNames.length == 1) {
                propertyValues.add(factoryPropertyName, new RuntimeBeanReference(beanNames[0]));
            } else if (beanNames.length > 1) {
                throw conflict(beanType, beanNames, annotationAttributeName);
            }
        } else {
            throw illegalArgument("Unknown wiring mode: " + aiServiceAnnotation.wiringMode());
        }
    }

    private static IllegalConfigurationException conflict(Class<?> beanType, Object[] beanNames, String attributeName) {
        return illegalConfiguration("Conflict: multiple beans of type %s are found: %s. " +
                        "Please specify which one you wish to wire in the @AiService annotation like this: " +
                        "@AiService(wiringMode = EXPLICIT, %s = \"<beanName>\").",
                beanType.getName(), Arrays.toString(beanNames), attributeName);
    }

    private static String lowercaseFirstLetter(String text) {
        if (isNullOrBlank(text)) {
            return text;
        }
        return text.substring(0, 1).toLowerCase() + text.substring(1);
    }

    private static ManagedList<RuntimeBeanReference> toManagedList(Collection<String> beanNames) {
        ManagedList<RuntimeBeanReference> managedList = new ManagedList<>();
        for (String beanName : beanNames) {
            managedList.add(new RuntimeBeanReference(beanName));
        }
        return managedList;
    }

    private static AiServiceRegisteredEvent buildEvent(Class<?> aiServiceClass,
                                                       Map<String, ToolSpecification> toolSpecifications,
                                                       Collection<String> tools) {
        return new AiServiceRegisteredEvent(aiServiceClass, aiServiceClass, tools.stream()
                                                                                 .filter(toolSpecifications::containsKey)
                                                                                 .map(toolSpecifications::get)
                                                                                 .collect(Collectors.toList()));
    }
}
