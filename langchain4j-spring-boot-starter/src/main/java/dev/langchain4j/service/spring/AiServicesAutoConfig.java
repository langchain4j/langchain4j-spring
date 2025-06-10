package dev.langchain4j.service.spring;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.agent.tool.ToolSpecifications;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.moderation.ModerationModel;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.IllegalConfigurationException;
import dev.langchain4j.service.spring.event.AiServiceRegisteredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.*;

import static dev.langchain4j.internal.Exceptions.illegalArgument;
import static dev.langchain4j.internal.Utils.isNotNullOrBlank;
import static dev.langchain4j.internal.Utils.isNullOrBlank;
import static dev.langchain4j.service.IllegalConfigurationException.illegalConfiguration;
import static dev.langchain4j.service.spring.AiServiceWiringMode.AUTOMATIC;
import static dev.langchain4j.service.spring.AiServiceWiringMode.EXPLICIT;
import static java.util.Arrays.asList;

public class AiServicesAutoConfig implements ApplicationEventPublisherAware {

    private static final Logger log = LoggerFactory.getLogger(AiServicesAutoConfig.class);

    private ApplicationEventPublisher eventPublisher;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Bean
    BeanFactoryPostProcessor aiServicesRegisteringBeanFactoryPostProcessor() {
        return beanFactory -> {

            // all components available in the application context
            String[] chatModels = beanFactory.getBeanNamesForType(ChatModel.class);
            String[] streamingChatModels = beanFactory.getBeanNamesForType(StreamingChatModel.class);
            String[] chatMemories = beanFactory.getBeanNamesForType(ChatMemory.class);
            String[] chatMemoryProviders = beanFactory.getBeanNamesForType(ChatMemoryProvider.class);
            String[] contentRetrievers = beanFactory.getBeanNamesForType(ContentRetriever.class);
            String[] retrievalAugmentors = beanFactory.getBeanNamesForType(RetrievalAugmentor.class);
            String[] moderationModels = beanFactory.getBeanNamesForType(ModerationModel.class);

            Set<String> toolBeanNames = new HashSet<>();
            List<ToolSpecification> toolSpecifications = new ArrayList<>();
            for (String beanName : beanFactory.getBeanDefinitionNames()) {
                try {
                    String beanClassName = beanFactory.getBeanDefinition(beanName).getBeanClassName();
                    if (beanClassName == null) {
                        continue;
                    }
                    Class<?> beanClass = Class.forName(beanClassName);
                    for (Method beanMethod : beanClass.getDeclaredMethods()) {
                        if (beanMethod.isAnnotationPresent(Tool.class)) {
                            toolBeanNames.add(beanName);
                            try {
                                toolSpecifications.add(ToolSpecifications.toolSpecificationFrom(beanMethod));
                            } catch (Exception e) {
                                log.warn("Cannot convert %s.%s method annotated with @Tool into ToolSpecification"
                                        .formatted(beanClass.getName(), beanMethod.getName()), e);
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
                        ChatModel.class,
                        aiServiceAnnotation,
                        aiServiceAnnotation.chatModel(),
                        chatModels,
                        "chatModel",
                        "chatModel",
                        propertyValues
                );

                addBeanReference(
                        StreamingChatModel.class,
                        aiServiceAnnotation,
                        aiServiceAnnotation.streamingChatModel(),
                        streamingChatModels,
                        "streamingChatModel",
                        "streamingChatModel",
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

                if (aiServiceAnnotation.wiringMode() == EXPLICIT) {
                    propertyValues.add("tools", toManagedList(asList(aiServiceAnnotation.tools())));
                } else if (aiServiceAnnotation.wiringMode() == AUTOMATIC) {
                    propertyValues.add("tools", toManagedList(toolBeanNames));
                } else {
                    throw illegalArgument("Unknown wiring mode: " + aiServiceAnnotation.wiringMode());
                }

                BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
                registry.removeBeanDefinition(aiService);
                registry.registerBeanDefinition(lowercaseFirstLetter(aiService), aiServiceBeanDefinition);

                if (eventPublisher != null) {
                    eventPublisher.publishEvent(new AiServiceRegisteredEvent(this, aiServiceClass, toolSpecifications));
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
}
