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
import dev.langchain4j.service.tool.ToolArgumentsErrorHandler;
import dev.langchain4j.service.tool.ToolExecutionErrorHandler;
import dev.langchain4j.service.tool.ToolProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.annotation.Bean;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static dev.langchain4j.internal.Exceptions.illegalArgument;
import static dev.langchain4j.internal.Utils.isNotNullOrBlank;
import static dev.langchain4j.internal.Utils.isNullOrBlank;
import static dev.langchain4j.service.IllegalConfigurationException.illegalConfiguration;
import static dev.langchain4j.service.spring.AiServiceWiringMode.AUTOMATIC;
import static dev.langchain4j.service.spring.AiServiceWiringMode.EXPLICIT;
import static java.util.stream.Collectors.toSet;

public class AiServicesAutoConfiguration implements ApplicationEventPublisherAware {

    private static final Logger log = LoggerFactory.getLogger(AiServicesAutoConfiguration.class);

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
            String[] toolProviders = beanFactory.getBeanNamesForType(ToolProvider.class);
            String[] toolExecutionErrorHandlers = beanFactory.getBeanNamesForType(ToolExecutionErrorHandler.class);
            String[] toolArgumentsErrorHandlers = beanFactory.getBeanNamesForType(ToolArgumentsErrorHandler.class);

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
            // AI services are re-registered under lowercased names one by one in the loop below,
            // so track the names that will exist afterwards and the original names that will be gone
            Set<String> aiServiceBeanNames = Arrays.stream(aiServices)
                    .map(AiServicesAutoConfiguration::lowercaseFirstLetter)
                    .collect(toSet());
            Set<String> renamedAiServiceBeanNames = Arrays.stream(aiServices)
                    .filter(aiService -> !aiService.equals(lowercaseFirstLetter(aiService)))
                    .collect(toSet());
            for (String aiService : aiServices) {
                Class<?> aiServiceClass = beanFactory.getType(aiService);

                GenericBeanDefinition aiServiceBeanDefinition = new GenericBeanDefinition();
                aiServiceBeanDefinition.setBeanClass(AiServiceFactory.class);
                aiServiceBeanDefinition.getConstructorArgumentValues().addGenericArgumentValue(aiServiceClass);
                MutablePropertyValues propertyValues = aiServiceBeanDefinition.getPropertyValues();

                AiService aiServiceAnnotation = aiServiceClass.getAnnotation(AiService.class);
                Map<String, String> wiredBeanNames = new LinkedHashMap<>();

                addBeanReference(
                        beanFactory,
                        ChatModel.class,
                        aiServiceAnnotation,
                        aiServiceAnnotation.chatModel(),
                        chatModels,
                        "chatModel",
                        "chatModel",
                        propertyValues,
                        wiredBeanNames
                );

                addBeanReference(
                        beanFactory,
                        StreamingChatModel.class,
                        aiServiceAnnotation,
                        aiServiceAnnotation.streamingChatModel(),
                        streamingChatModels,
                        "streamingChatModel",
                        "streamingChatModel",
                        propertyValues,
                        wiredBeanNames
                );

                addBeanReference(
                        beanFactory,
                        ChatMemory.class,
                        aiServiceAnnotation,
                        aiServiceAnnotation.chatMemory(),
                        chatMemories,
                        "chatMemory",
                        "chatMemory",
                        propertyValues,
                        wiredBeanNames
                );

                addBeanReference(
                        beanFactory,
                        ChatMemoryProvider.class,
                        aiServiceAnnotation,
                        aiServiceAnnotation.chatMemoryProvider(),
                        chatMemoryProviders,
                        "chatMemoryProvider",
                        "chatMemoryProvider",
                        propertyValues,
                        wiredBeanNames
                );

                addBeanReference(
                        beanFactory,
                        ContentRetriever.class,
                        aiServiceAnnotation,
                        aiServiceAnnotation.contentRetriever(),
                        contentRetrievers,
                        "contentRetriever",
                        "contentRetriever",
                        propertyValues,
                        wiredBeanNames
                );

                addBeanReference(
                        beanFactory,
                        RetrievalAugmentor.class,
                        aiServiceAnnotation,
                        aiServiceAnnotation.retrievalAugmentor(),
                        retrievalAugmentors,
                        "retrievalAugmentor",
                        "retrievalAugmentor",
                        propertyValues,
                        wiredBeanNames
                );

                addBeanReference(
                        beanFactory,
                        ModerationModel.class,
                        aiServiceAnnotation,
                        aiServiceAnnotation.moderationModel(),
                        moderationModels,
                        "moderationModel",
                        "moderationModel",
                        propertyValues,
                        wiredBeanNames
                );

                addBeanReference(
                        beanFactory,
                        ToolProvider.class,
                        aiServiceAnnotation,
                        aiServiceAnnotation.toolProvider(),
                        toolProviders,
                        "toolProvider",
                        "toolProvider",
                        propertyValues,
                        wiredBeanNames
                );

                addBeanReference(
                        beanFactory,
                        ToolExecutionErrorHandler.class,
                        aiServiceAnnotation,
                        aiServiceAnnotation.toolExecutionErrorHandler(),
                        toolExecutionErrorHandlers,
                        "toolExecutionErrorHandler",
                        "toolExecutionErrorHandler",
                        propertyValues,
                        wiredBeanNames
                );

                addBeanReference(
                        beanFactory,
                        ToolArgumentsErrorHandler.class,
                        aiServiceAnnotation,
                        aiServiceAnnotation.toolArgumentsErrorHandler(),
                        toolArgumentsErrorHandlers,
                        "toolArgumentsErrorHandler",
                        "toolArgumentsErrorHandler",
                        propertyValues,
                        wiredBeanNames
                );

                Collection<String> wiredTools;
                if (aiServiceAnnotation.wiringMode() == EXPLICIT) {
                    List<String> resolvedTools = new ArrayList<>();
                    for (String toolBeanName : aiServiceAnnotation.tools()) {
                        String resolved = resolve(beanFactory, toolBeanName);
                        if (isNotNullOrBlank(resolved)) {
                            for (String singleBeanName : resolved.split(",")) {
                                String trimmed = singleBeanName.trim();
                                if (isNotNullOrBlank(trimmed)) {
                                    resolvedTools.add(trimmed);
                                }
                            }
                        }
                    }
                    propertyValues.add("tools", toManagedList(resolvedTools));
                    wiredTools = resolvedTools;
                } else if (aiServiceAnnotation.wiringMode() == AUTOMATIC) {
                    propertyValues.add("tools", toManagedList(toolBeanNames));
                    wiredTools = toolBeanNames;
                } else {
                    throw illegalArgument("Unknown wiring mode: " + aiServiceAnnotation.wiringMode());
                }

                BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
                registry.removeBeanDefinition(aiService);
                registry.registerBeanDefinition(lowercaseFirstLetter(aiService), aiServiceBeanDefinition);

                if (log.isDebugEnabled()) {
                    log.debug("Registered @AiService bean '{}' for {} using {} wiring mode, wired components: {}",
                            lowercaseFirstLetter(aiService), aiServiceClass.getName(),
                            aiServiceAnnotation.wiringMode(),
                            describeWiring(beanFactory, aiServiceAnnotation, wiredBeanNames, wiredTools,
                                    aiServiceBeanNames, renamedAiServiceBeanNames));
                }

                if (eventPublisher != null) {
                    eventPublisher.publishEvent(new AiServiceRegisteredEvent(this, aiServiceClass, toolSpecifications));
                }
            }
        };
    }

    private static void addBeanReference(ConfigurableListableBeanFactory beanFactory,
                                         Class<?> beanType,
                                         AiService aiServiceAnnotation,
                                         String customBeanName,
                                         String[] beanNames,
                                         String annotationAttributeName,
                                         String factoryPropertyName,
                                         MutablePropertyValues propertyValues,
                                         Map<String, String> wiredBeanNames) {
        if (aiServiceAnnotation.wiringMode() == EXPLICIT) {
            String resolvedBeanName = resolve(beanFactory, customBeanName);
            if (isNotNullOrBlank(resolvedBeanName)) {
                propertyValues.add(factoryPropertyName, new RuntimeBeanReference(resolvedBeanName));
                wiredBeanNames.put(factoryPropertyName, resolvedBeanName);
            }
        } else if (aiServiceAnnotation.wiringMode() == AUTOMATIC) {
            if (beanNames.length == 1) {
                propertyValues.add(factoryPropertyName, new RuntimeBeanReference(beanNames[0]));
                wiredBeanNames.put(factoryPropertyName, beanNames[0]);
            } else if (beanNames.length > 1) {
                throw conflict(beanType, beanNames, annotationAttributeName);
            }
        } else {
            throw illegalArgument("Unknown wiring mode: " + aiServiceAnnotation.wiringMode());
        }
    }

    private static Map<String, Object> describeWiring(ConfigurableListableBeanFactory beanFactory,
                                                      AiService aiServiceAnnotation,
                                                      Map<String, String> wiredBeanNames,
                                                      Collection<String> wiredTools,
                                                      Set<String> aiServiceBeanNames,
                                                      Set<String> renamedAiServiceBeanNames) {
        boolean explicit = aiServiceAnnotation.wiringMode() == EXPLICIT;
        UnaryOperator<String> describe = explicit
                ? beanName -> describeExplicitBean(beanFactory, beanName, aiServiceBeanNames, renamedAiServiceBeanNames)
                : UnaryOperator.identity();
        Map<String, Object> wiring = new LinkedHashMap<>();
        wiredBeanNames.forEach((component, beanName) -> wiring.put(component, describe.apply(beanName)));
        // automatically wired tools come from a HashSet, sort them for a stable log line
        Stream<String> tools = wiredTools.stream().map(describe);
        wiring.put("tools", (explicit ? tools : tools.sorted()).toList());
        return wiring;
    }

    /**
     * Explicitly named beans are not validated until the AI service is instantiated,
     * so flag missing ones in the log instead of reporting them as wired.
     */
    private static String describeExplicitBean(ConfigurableListableBeanFactory beanFactory,
                                               String beanName,
                                               Set<String> aiServiceBeanNames,
                                               Set<String> renamedAiServiceBeanNames) {
        // The registry is mid-way through re-registering AI services: a renamed AI service may still
        // be found under its original name (which the RuntimeBeanReference cannot resolve later),
        // and one not yet processed may not be found under its final name.
        boolean exists = aiServiceBeanNames.contains(beanName)
                || (!renamedAiServiceBeanNames.contains(beanName) && beanFactory.containsBean(beanName));
        return exists ? beanName : beanName + " (no such bean)";
    }

    private static String resolve(ConfigurableListableBeanFactory beanFactory, String value) {
        if (isNullOrBlank(value)) {
            return value;
        }
        return beanFactory.resolveEmbeddedValue(value);
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
