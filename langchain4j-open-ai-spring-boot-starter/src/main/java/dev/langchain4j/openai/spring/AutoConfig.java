package dev.langchain4j.openai.spring;

import dev.langchain4j.http.client.HttpClientBuilder;
import dev.langchain4j.http.client.spring.restclient.SpringRestClient;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.openai.*;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.ContextPropagatingTaskDecorator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;

import static dev.langchain4j.openai.spring.Properties.PREFIX;

@AutoConfiguration(after = RestClientAutoConfiguration.class)
@EnableConfigurationProperties(Properties.class)
public class AutoConfig {

    private static final String TASK_EXECUTOR_THREAD_NAME_PREFIX = "LangChain4j-OpenAI-";

    private static final String CHAT_MODEL_HTTP_CLIENT_BUILDER = "openAiChatModelHttpClientBuilder";

    private static final String STREAMING_CHAT_MODEL_HTTP_CLIENT_BUILDER = "openAiStreamingChatModelHttpClientBuilder";
    private static final String STREAMING_CHAT_MODEL_TASK_EXECUTOR = "openAiStreamingChatModelTaskExecutor";

    private static final String LANGUAGE_MODEL_HTTP_CLIENT_BUILDER = "openAiLanguageModelHttpClientBuilder";

    private static final String STREAMING_LANGUAGE_MODEL_HTTP_CLIENT_BUILDER = "openAiStreamingLanguageModelHttpClientBuilder";
    private static final String STREAMING_LANGUAGE_MODEL_TASK_EXECUTOR = "openAiStreamingLanguageModelTaskExecutor";

    private static final String EMBEDDING_MODEL_HTTP_CLIENT_BUILDER = "openAiEmbeddingModelHttpClientBuilder";

    private static final String MODERATION_MODEL_HTTP_CLIENT_BUILDER = "openAiModerationModelHttpClientBuilder";

    private static final String IMAGE_MODEL_HTTP_CLIENT_BUILDER = "openAiImageModelHttpClientBuilder";

    @Bean
    @ConditionalOnProperty(PREFIX + ".chat-model.api-key")
    OpenAiChatModel openAiChatModel(
            @Qualifier(CHAT_MODEL_HTTP_CLIENT_BUILDER) HttpClientBuilder httpClientBuilder,
            Properties properties,
            ObjectProvider<ChatModelListener> listeners
    ) {
        ChatModelProperties chatModelProperties = properties.chatModel();
        return OpenAiChatModel.builder()
                .httpClientBuilder(httpClientBuilder)
                .baseUrl(chatModelProperties.baseUrl())
                .apiKey(chatModelProperties.apiKey())
                .organizationId(chatModelProperties.organizationId())
                .projectId(chatModelProperties.projectId())
                .modelName(chatModelProperties.modelName())
                .temperature(chatModelProperties.temperature())
                .topP(chatModelProperties.topP())
                .stop(chatModelProperties.stop())
                .maxTokens(chatModelProperties.maxTokens())
                .maxCompletionTokens(chatModelProperties.maxCompletionTokens())
                .presencePenalty(chatModelProperties.presencePenalty())
                .frequencyPenalty(chatModelProperties.frequencyPenalty())
                .logitBias(chatModelProperties.logitBias())
                .responseFormat(chatModelProperties.responseFormat())
                .supportedCapabilities(chatModelProperties.supportedCapabilities())
                .strictJsonSchema(chatModelProperties.strictJsonSchema())
                .seed(chatModelProperties.seed())
                .user(chatModelProperties.user())
                .strictTools(chatModelProperties.strictTools())
                .parallelToolCalls(chatModelProperties.parallelToolCalls())
                .store(chatModelProperties.store())
                .metadata(chatModelProperties.metadata())
                .serviceTier(chatModelProperties.serviceTier())
                .defaultRequestParameters(OpenAiChatRequestParameters.builder()
                        .reasoningEffort(chatModelProperties.reasoningEffort())
                        .customParameters(chatModelProperties.customParameters())
                        .build())
                .returnThinking(chatModelProperties.returnThinking())
                .timeout(chatModelProperties.timeout())
                .maxRetries(chatModelProperties.maxRetries())
                .logRequests(chatModelProperties.logRequests())
                .logResponses(chatModelProperties.logResponses())
                .customHeaders(chatModelProperties.customHeaders())
                .customQueryParams(chatModelProperties.customQueryParams())
                .listeners(listeners.orderedStream().toList())
                .build();
    }

    @Bean(CHAT_MODEL_HTTP_CLIENT_BUILDER)
    @ConditionalOnProperty(PREFIX + ".chat-model.api-key")
    @ConditionalOnMissingBean(name = CHAT_MODEL_HTTP_CLIENT_BUILDER)
    HttpClientBuilder openAiChatModelHttpClientBuilder(ObjectProvider<RestClient.Builder> restClientBuilder) {
        return SpringRestClient.builder()
                .restClientBuilder(restClientBuilder.getIfAvailable(RestClient::builder))
                // executor is not needed for no-streaming OpenAiChatModel
                .createDefaultStreamingRequestExecutor(false);
    }

    @Bean
    @ConditionalOnProperty(PREFIX + ".streaming-chat-model.api-key")
    OpenAiStreamingChatModel openAiStreamingChatModel(
            @Qualifier(STREAMING_CHAT_MODEL_HTTP_CLIENT_BUILDER) HttpClientBuilder httpClientBuilder,
            Properties properties,
            ObjectProvider<ChatModelListener> listeners
    ) {
        ChatModelProperties chatModelProperties = properties.streamingChatModel();
        return OpenAiStreamingChatModel.builder()
                .httpClientBuilder(httpClientBuilder)
                .baseUrl(chatModelProperties.baseUrl())
                .apiKey(chatModelProperties.apiKey())
                .organizationId(chatModelProperties.organizationId())
                .projectId(chatModelProperties.projectId())
                .modelName(chatModelProperties.modelName())
                .temperature(chatModelProperties.temperature())
                .topP(chatModelProperties.topP())
                .stop(chatModelProperties.stop())
                .maxTokens(chatModelProperties.maxTokens())
                .maxCompletionTokens(chatModelProperties.maxCompletionTokens())
                .presencePenalty(chatModelProperties.presencePenalty())
                .frequencyPenalty(chatModelProperties.frequencyPenalty())
                .logitBias(chatModelProperties.logitBias())
                .responseFormat(chatModelProperties.responseFormat())
                .seed(chatModelProperties.seed())
                .user(chatModelProperties.user())
                .strictTools(chatModelProperties.strictTools())
                .parallelToolCalls(chatModelProperties.parallelToolCalls())
                .store(chatModelProperties.store())
                .metadata(chatModelProperties.metadata())
                .serviceTier(chatModelProperties.serviceTier())
                .defaultRequestParameters(OpenAiChatRequestParameters.builder()
                        .reasoningEffort(chatModelProperties.reasoningEffort())
                        .customParameters(chatModelProperties.customParameters())
                        .build())
                .returnThinking(chatModelProperties.returnThinking())
                .timeout(chatModelProperties.timeout())
                .logRequests(chatModelProperties.logRequests())
                .logResponses(chatModelProperties.logResponses())
                .customHeaders(chatModelProperties.customHeaders())
                .customQueryParams(chatModelProperties.customQueryParams())
                .listeners(listeners.orderedStream().toList())
                .build();
    }

    @Bean(STREAMING_CHAT_MODEL_HTTP_CLIENT_BUILDER)
    @ConditionalOnProperty(PREFIX + ".streaming-chat-model.api-key")
    @ConditionalOnMissingBean(name = STREAMING_CHAT_MODEL_HTTP_CLIENT_BUILDER)
    HttpClientBuilder openAiStreamingChatModelHttpClientBuilder(
            ObjectProvider<RestClient.Builder> restClientBuilder,
            @Qualifier(STREAMING_CHAT_MODEL_TASK_EXECUTOR) AsyncTaskExecutor executor) {
        return SpringRestClient.builder()
                .restClientBuilder(restClientBuilder.getIfAvailable(RestClient::builder))
                .streamingRequestExecutor(executor);
    }

    @Bean(STREAMING_CHAT_MODEL_TASK_EXECUTOR)
    @ConditionalOnProperty(PREFIX + ".streaming-chat-model.api-key")
    @ConditionalOnMissingBean(name = STREAMING_CHAT_MODEL_TASK_EXECUTOR)
    @ConditionalOnClass(name = "io.micrometer.context.ContextSnapshotFactory")
    AsyncTaskExecutor openAiStreamingChatModelTaskExecutorWithContextPropagation() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setQueueCapacity(0);
        taskExecutor.setThreadNamePrefix(TASK_EXECUTOR_THREAD_NAME_PREFIX);
        taskExecutor.setTaskDecorator(new ContextPropagatingTaskDecorator());
        return taskExecutor;
    }

    @Bean(STREAMING_CHAT_MODEL_TASK_EXECUTOR)
    @ConditionalOnProperty(PREFIX + ".streaming-chat-model.api-key")
    @ConditionalOnMissingBean(name = STREAMING_CHAT_MODEL_TASK_EXECUTOR)
    @ConditionalOnMissingClass("io.micrometer.context.ContextSnapshotFactory")
    AsyncTaskExecutor openAiStreamingChatModelTaskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setQueueCapacity(0);
        taskExecutor.setThreadNamePrefix(TASK_EXECUTOR_THREAD_NAME_PREFIX);
        return taskExecutor;
    }

    @Bean
    @ConditionalOnProperty(PREFIX + ".language-model.api-key")
    OpenAiLanguageModel openAiLanguageModel(
            @Qualifier(LANGUAGE_MODEL_HTTP_CLIENT_BUILDER) HttpClientBuilder httpClientBuilder,
            Properties properties
    ) {
        LanguageModelProperties languageModelProperties = properties.languageModel();
        return OpenAiLanguageModel.builder()
                .httpClientBuilder(httpClientBuilder)
                .baseUrl(languageModelProperties.baseUrl())
                .apiKey(languageModelProperties.apiKey())
                .organizationId(languageModelProperties.organizationId())
                .projectId(languageModelProperties.projectId())
                .modelName(languageModelProperties.modelName())
                .temperature(languageModelProperties.temperature())
                .timeout(languageModelProperties.timeout())
                .maxRetries(languageModelProperties.maxRetries())
                .logRequests(languageModelProperties.logRequests())
                .logResponses(languageModelProperties.logResponses())
                .customHeaders(languageModelProperties.customHeaders())
                .customQueryParams(languageModelProperties.customQueryParams())
                .build();
    }

    @Bean(LANGUAGE_MODEL_HTTP_CLIENT_BUILDER)
    @ConditionalOnProperty(PREFIX + ".language-model.api-key")
    @ConditionalOnMissingBean(name = LANGUAGE_MODEL_HTTP_CLIENT_BUILDER)
    HttpClientBuilder openAiLanguageModelHttpClientBuilder(ObjectProvider<RestClient.Builder> restClientBuilder) {
        return SpringRestClient.builder()
                .restClientBuilder(restClientBuilder.getIfAvailable(RestClient::builder))
                // executor is not needed for no-streaming OpenAiLanguageModel
                .createDefaultStreamingRequestExecutor(false);
    }

    @Bean
    @ConditionalOnProperty(PREFIX + ".streaming-language-model.api-key")
    OpenAiStreamingLanguageModel openAiStreamingLanguageModel(
            @Qualifier(STREAMING_LANGUAGE_MODEL_HTTP_CLIENT_BUILDER) HttpClientBuilder httpClientBuilder,
            Properties properties
    ) {
        LanguageModelProperties languageModelProperties = properties.streamingLanguageModel();
        return OpenAiStreamingLanguageModel.builder()
                .httpClientBuilder(httpClientBuilder)
                .baseUrl(languageModelProperties.baseUrl())
                .apiKey(languageModelProperties.apiKey())
                .organizationId(languageModelProperties.organizationId())
                .projectId(languageModelProperties.projectId())
                .modelName(languageModelProperties.modelName())
                .temperature(languageModelProperties.temperature())
                .timeout(languageModelProperties.timeout())
                .logRequests(languageModelProperties.logRequests())
                .logResponses(languageModelProperties.logResponses())
                .customHeaders(languageModelProperties.customHeaders())
                .customQueryParams(languageModelProperties.customQueryParams())
                .build();
    }

    @Bean(STREAMING_LANGUAGE_MODEL_HTTP_CLIENT_BUILDER)
    @ConditionalOnProperty(PREFIX + ".streaming-language-model.api-key")
    @ConditionalOnMissingBean(name = STREAMING_LANGUAGE_MODEL_HTTP_CLIENT_BUILDER)
    HttpClientBuilder openAiStreamingLanguageModelHttpClientBuilder(
            @Qualifier(STREAMING_LANGUAGE_MODEL_TASK_EXECUTOR) AsyncTaskExecutor executor,
            ObjectProvider<RestClient.Builder> restClientBuilder
    ) {
        return SpringRestClient.builder()
                .restClientBuilder(restClientBuilder.getIfAvailable(RestClient::builder))
                .streamingRequestExecutor(executor);
    }

    @Bean(STREAMING_LANGUAGE_MODEL_TASK_EXECUTOR)
    @ConditionalOnProperty(PREFIX + ".streaming-language-model.api-key")
    @ConditionalOnMissingBean(name = STREAMING_LANGUAGE_MODEL_TASK_EXECUTOR)
    @ConditionalOnClass(name = "io.micrometer.context.ContextSnapshotFactory")
    AsyncTaskExecutor openAiStreamingLanguageModelTaskExecutorWithContextPropagation() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setQueueCapacity(0);
        taskExecutor.setThreadNamePrefix(TASK_EXECUTOR_THREAD_NAME_PREFIX);
        taskExecutor.setTaskDecorator(new ContextPropagatingTaskDecorator());
        return taskExecutor;
    }

    @Bean(STREAMING_LANGUAGE_MODEL_TASK_EXECUTOR)
    @ConditionalOnProperty(PREFIX + ".streaming-language-model.api-key")
    @ConditionalOnMissingBean(name = STREAMING_LANGUAGE_MODEL_TASK_EXECUTOR)
    @ConditionalOnMissingClass("io.micrometer.context.ContextSnapshotFactory")
    AsyncTaskExecutor openAiStreamingLanguageModelTaskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setQueueCapacity(0);
        taskExecutor.setThreadNamePrefix(TASK_EXECUTOR_THREAD_NAME_PREFIX);
        return taskExecutor;
    }

    @Bean
    @ConditionalOnProperty(PREFIX + ".embedding-model.api-key")
    OpenAiEmbeddingModel openAiEmbeddingModel(
            @Qualifier(EMBEDDING_MODEL_HTTP_CLIENT_BUILDER) HttpClientBuilder httpClientBuilder,
            Properties properties
    ) {
        EmbeddingModelProperties embeddingModelProperties = properties.embeddingModel();
        return OpenAiEmbeddingModel.builder()
                .httpClientBuilder(httpClientBuilder)
                .baseUrl(embeddingModelProperties.baseUrl())
                .apiKey(embeddingModelProperties.apiKey())
                .organizationId(embeddingModelProperties.organizationId())
                .projectId(embeddingModelProperties.projectId())
                .modelName(embeddingModelProperties.modelName())
                .dimensions(embeddingModelProperties.dimensions())
                .maxSegmentsPerBatch(embeddingModelProperties.maxSegmentsPerBatch())
                .user(embeddingModelProperties.user())
                .timeout(embeddingModelProperties.timeout())
                .maxRetries(embeddingModelProperties.maxRetries())
                .logRequests(embeddingModelProperties.logRequests())
                .logResponses(embeddingModelProperties.logResponses())
                .customHeaders(embeddingModelProperties.customHeaders())
                .customQueryParams(embeddingModelProperties.customQueryParams())
                .build();
    }

    @Bean(EMBEDDING_MODEL_HTTP_CLIENT_BUILDER)
    @ConditionalOnProperty(PREFIX + ".embedding-model.api-key")
    @ConditionalOnMissingBean(name = EMBEDDING_MODEL_HTTP_CLIENT_BUILDER)
    HttpClientBuilder openAiEmbeddingModelHttpClientBuilder(ObjectProvider<RestClient.Builder> restClientBuilder) {
        return SpringRestClient.builder()
                .restClientBuilder(restClientBuilder.getIfAvailable(RestClient::builder))
                // executor is not needed for no-streaming OpenAiEmbeddingModel
                .createDefaultStreamingRequestExecutor(false);
    }

    @Bean
    @ConditionalOnProperty(PREFIX + ".moderation-model.api-key")
    OpenAiModerationModel openAiModerationModel(
            @Qualifier(MODERATION_MODEL_HTTP_CLIENT_BUILDER) HttpClientBuilder httpClientBuilder,
            Properties properties
    ) {
        ModerationModelProperties moderationModelProperties = properties.moderationModel();
        return OpenAiModerationModel.builder()
                .httpClientBuilder(httpClientBuilder)
                .baseUrl(moderationModelProperties.baseUrl())
                .apiKey(moderationModelProperties.apiKey())
                .organizationId(moderationModelProperties.organizationId())
                .projectId(moderationModelProperties.projectId())
                .modelName(moderationModelProperties.modelName())
                .timeout(moderationModelProperties.timeout())
                .maxRetries(moderationModelProperties.maxRetries())
                .logRequests(moderationModelProperties.logRequests())
                .logResponses(moderationModelProperties.logResponses())
                .customHeaders(moderationModelProperties.customHeaders())
                .customQueryParams(moderationModelProperties.customQueryParams())
                .build();
    }

    @Bean(MODERATION_MODEL_HTTP_CLIENT_BUILDER)
    @ConditionalOnProperty(PREFIX + ".moderation-model.api-key")
    @ConditionalOnMissingBean(name = MODERATION_MODEL_HTTP_CLIENT_BUILDER)
    HttpClientBuilder openAiModerationModelHttpClientBuilder(ObjectProvider<RestClient.Builder> restClientBuilder) {
        return SpringRestClient.builder()
                .restClientBuilder(restClientBuilder.getIfAvailable(RestClient::builder))
                // executor is not needed for no-streaming OpenAiModerationModel
                .createDefaultStreamingRequestExecutor(false);
    }

    @Bean
    @ConditionalOnProperty(PREFIX + ".image-model.api-key")
    OpenAiImageModel openAiImageModel(
            @Qualifier(IMAGE_MODEL_HTTP_CLIENT_BUILDER) HttpClientBuilder httpClientBuilder,
            Properties properties
    ) {
        ImageModelProperties imageModelProperties = properties.imageModel();
        return OpenAiImageModel.builder()
                .httpClientBuilder(httpClientBuilder)
                .baseUrl(imageModelProperties.baseUrl())
                .apiKey(imageModelProperties.apiKey())
                .organizationId(imageModelProperties.organizationId())
                .projectId(imageModelProperties.projectId())
                .modelName(imageModelProperties.modelName())
                .size(imageModelProperties.size())
                .quality(imageModelProperties.quality())
                .style(imageModelProperties.style())
                .user(imageModelProperties.user())
                .responseFormat(imageModelProperties.responseFormat())
                .timeout(imageModelProperties.timeout())
                .maxRetries(imageModelProperties.maxRetries())
                .logRequests(imageModelProperties.logRequests())
                .logResponses(imageModelProperties.logResponses())
                .customHeaders(imageModelProperties.customHeaders())
                .customQueryParams(imageModelProperties.customQueryParams())
                .build();
    }

    @Bean(IMAGE_MODEL_HTTP_CLIENT_BUILDER)
    @ConditionalOnProperty(PREFIX + ".image-model.api-key")
    @ConditionalOnMissingBean(name = IMAGE_MODEL_HTTP_CLIENT_BUILDER)
    HttpClientBuilder openAiImageModelHttpClientBuilder(ObjectProvider<RestClient.Builder> restClientBuilder) {
        return SpringRestClient.builder()
                .restClientBuilder(restClientBuilder.getIfAvailable(RestClient::builder))
                // executor is not needed for no-streaming OpenAiImageModel
                .createDefaultStreamingRequestExecutor(false);
    }

    @Bean
    static BeanDefinitionRegistryPostProcessor namedOpenAiChatModelBeanRegistrar(Environment environment) {
        return new NamedModelBeanRegistrar(environment);
    }

    static class NamedModelBeanRegistrar implements BeanDefinitionRegistryPostProcessor, ApplicationContextAware {

        private static final String CHAT_MODEL_PREFIX = PREFIX + ".chat-model";
        private static final String STREAMING_CHAT_MODEL_PREFIX = PREFIX + ".streaming-chat-model";

        private static final Set<String> KNOWN_PROPERTIES = Set.of(
                "base-url", "api-key", "organization-id", "project-id", "model-name",
                "temperature", "top-p", "stop", "max-tokens", "max-completion-tokens",
                "presence-penalty", "frequency-penalty", "logit-bias", "response-format",
                "supported-capabilities", "strict-json-schema", "seed", "user",
                "strict-tools", "parallel-tool-calls", "store", "metadata", "service-tier",
                "reasoning-effort", "return-thinking", "timeout", "max-retries",
                "log-requests", "log-responses", "custom-headers", "custom-query-params",
                "custom-parameters"
        );

        private final Environment environment;
        private ApplicationContext applicationContext;

        NamedModelBeanRegistrar(Environment environment) {
            this.environment = environment;
        }

        @Override
        public void setApplicationContext(ApplicationContext applicationContext) {
            this.applicationContext = applicationContext;
        }

        @Override
        public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
            registerNamedChatModels(registry, CHAT_MODEL_PREFIX, "openAiChatModel", false);
            registerNamedChatModels(registry, STREAMING_CHAT_MODEL_PREFIX, "openAiStreamingChatModel", true);
        }

        private void registerNamedChatModels(BeanDefinitionRegistry registry, String prefix,
                                              String beanNamePrefix, boolean streaming) {
            ChatModelProperties globalProps = Binder.get(environment)
                    .bind(prefix, ChatModelProperties.class)
                    .orElse(null);

            Set<String> namedModelKeys = findNamedModelKeys(prefix);

            for (String modelName : namedModelKeys) {
                String namedPrefix = prefix + "." + modelName;
                ChatModelProperties namedProps = Binder.get(environment)
                        .bind(namedPrefix, ChatModelProperties.class)
                        .orElse(null);

                if (namedProps == null) {
                    continue;
                }

                ChatModelProperties mergedProps = mergeWithGlobal(globalProps, namedProps);

                if (mergedProps.apiKey() == null) {
                    continue;
                }

                String beanName = beanNamePrefix + toPascalCase(modelName);

                GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
                beanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);

                if (streaming) {
                    beanDefinition.setBeanClass(OpenAiStreamingChatModel.class);
                    beanDefinition.setInstanceSupplier(() -> {
                        HttpClientBuilder httpClientBuilder = createHttpClientBuilder(true);
                        List<ChatModelListener> listeners = getListeners();
                        return createStreamingChatModel(mergedProps, httpClientBuilder, listeners);
                    });
                } else {
                    beanDefinition.setBeanClass(OpenAiChatModel.class);
                    beanDefinition.setInstanceSupplier(() -> {
                        HttpClientBuilder httpClientBuilder = createHttpClientBuilder(false);
                        List<ChatModelListener> listeners = getListeners();
                        return createChatModel(mergedProps, httpClientBuilder, listeners);
                    });
                }

                registry.registerBeanDefinition(beanName, beanDefinition);
            }
        }

        private Set<String> findNamedModelKeys(String prefix) {
            Set<String> namedKeys = new HashSet<>();
            String searchPrefix = prefix + ".";

            if (environment instanceof ConfigurableEnvironment configurableEnv) {
                for (PropertySource<?> propertySource : configurableEnv.getPropertySources()) {
                    if (propertySource instanceof EnumerablePropertySource<?> enumerable) {
                        for (String propertyName : enumerable.getPropertyNames()) {
                            if (propertyName.startsWith(searchPrefix)) {
                                String remainder = propertyName.substring(searchPrefix.length());
                                int dotIndex = remainder.indexOf('.');
                                String firstSegment = dotIndex > 0 ? remainder.substring(0, dotIndex) : remainder;

                                if (!KNOWN_PROPERTIES.contains(firstSegment)) {
                                    namedKeys.add(firstSegment);
                                }
                            }
                        }
                    }
                }
            }

            return namedKeys;
        }

        private HttpClientBuilder createHttpClientBuilder(boolean streaming) {
            ObjectProvider<RestClient.Builder> restClientBuilderProvider =
                    applicationContext.getBeanProvider(RestClient.Builder.class);
            RestClient.Builder restClientBuilder = restClientBuilderProvider.getIfAvailable(RestClient::builder);

            if (streaming) {
                ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
                executor.setQueueCapacity(0);
                executor.setThreadNamePrefix(TASK_EXECUTOR_THREAD_NAME_PREFIX);
                executor.initialize();
                return SpringRestClient.builder()
                        .restClientBuilder(restClientBuilder)
                        .streamingRequestExecutor(executor);
            } else {
                return SpringRestClient.builder()
                        .restClientBuilder(restClientBuilder)
                        .createDefaultStreamingRequestExecutor(false);
            }
        }

        private List<ChatModelListener> getListeners() {
            return applicationContext.getBeanProvider(ChatModelListener.class)
                    .orderedStream()
                    .toList();
        }

        private ChatModelProperties mergeWithGlobal(ChatModelProperties global, ChatModelProperties named) {
            if (global == null) {
                return named;
            }
            return new ChatModelProperties(
                    named.baseUrl() != null ? named.baseUrl() : global.baseUrl(),
                    named.apiKey() != null ? named.apiKey() : global.apiKey(),
                    named.organizationId() != null ? named.organizationId() : global.organizationId(),
                    named.projectId() != null ? named.projectId() : global.projectId(),
                    named.modelName() != null ? named.modelName() : global.modelName(),
                    named.temperature() != null ? named.temperature() : global.temperature(),
                    named.topP() != null ? named.topP() : global.topP(),
                    named.stop() != null ? named.stop() : global.stop(),
                    named.maxTokens() != null ? named.maxTokens() : global.maxTokens(),
                    named.maxCompletionTokens() != null ? named.maxCompletionTokens() : global.maxCompletionTokens(),
                    named.presencePenalty() != null ? named.presencePenalty() : global.presencePenalty(),
                    named.frequencyPenalty() != null ? named.frequencyPenalty() : global.frequencyPenalty(),
                    named.logitBias() != null ? named.logitBias() : global.logitBias(),
                    named.responseFormat() != null ? named.responseFormat() : global.responseFormat(),
                    named.supportedCapabilities() != null ? named.supportedCapabilities() : global.supportedCapabilities(),
                    named.strictJsonSchema() != null ? named.strictJsonSchema() : global.strictJsonSchema(),
                    named.seed() != null ? named.seed() : global.seed(),
                    named.user() != null ? named.user() : global.user(),
                    named.strictTools() != null ? named.strictTools() : global.strictTools(),
                    named.parallelToolCalls() != null ? named.parallelToolCalls() : global.parallelToolCalls(),
                    named.store() != null ? named.store() : global.store(),
                    named.metadata() != null ? named.metadata() : global.metadata(),
                    named.serviceTier() != null ? named.serviceTier() : global.serviceTier(),
                    named.reasoningEffort() != null ? named.reasoningEffort() : global.reasoningEffort(),
                    named.returnThinking() != null ? named.returnThinking() : global.returnThinking(),
                    named.timeout() != null ? named.timeout() : global.timeout(),
                    named.maxRetries() != null ? named.maxRetries() : global.maxRetries(),
                    named.logRequests() != null ? named.logRequests() : global.logRequests(),
                    named.logResponses() != null ? named.logResponses() : global.logResponses(),
                    named.customHeaders() != null ? named.customHeaders() : global.customHeaders(),
                    named.customQueryParams() != null ? named.customQueryParams() : global.customQueryParams(),
                    named.customParameters() != null ? named.customParameters() : global.customParameters()
            );
        }

        private OpenAiChatModel createChatModel(ChatModelProperties props,
                                                HttpClientBuilder httpClientBuilder,
                                                List<ChatModelListener> listeners) {
            return OpenAiChatModel.builder()
                    .httpClientBuilder(httpClientBuilder)
                    .baseUrl(props.baseUrl())
                    .apiKey(props.apiKey())
                    .organizationId(props.organizationId())
                    .projectId(props.projectId())
                    .modelName(props.modelName())
                    .temperature(props.temperature())
                    .topP(props.topP())
                    .stop(props.stop())
                    .maxTokens(props.maxTokens())
                    .maxCompletionTokens(props.maxCompletionTokens())
                    .presencePenalty(props.presencePenalty())
                    .frequencyPenalty(props.frequencyPenalty())
                    .logitBias(props.logitBias())
                    .responseFormat(props.responseFormat())
                    .supportedCapabilities(props.supportedCapabilities())
                    .strictJsonSchema(props.strictJsonSchema())
                    .seed(props.seed())
                    .user(props.user())
                    .strictTools(props.strictTools())
                    .parallelToolCalls(props.parallelToolCalls())
                    .store(props.store())
                    .metadata(props.metadata())
                    .serviceTier(props.serviceTier())
                    .defaultRequestParameters(OpenAiChatRequestParameters.builder()
                            .reasoningEffort(props.reasoningEffort())
                            .customParameters(props.customParameters())
                            .build())
                    .returnThinking(props.returnThinking())
                    .timeout(props.timeout())
                    .maxRetries(props.maxRetries())
                    .logRequests(props.logRequests())
                    .logResponses(props.logResponses())
                    .customHeaders(props.customHeaders())
                    .customQueryParams(props.customQueryParams())
                    .listeners(listeners)
                    .build();
        }

        private OpenAiStreamingChatModel createStreamingChatModel(ChatModelProperties props,
                                                                  HttpClientBuilder httpClientBuilder,
                                                                  List<ChatModelListener> listeners) {
            return OpenAiStreamingChatModel.builder()
                    .httpClientBuilder(httpClientBuilder)
                    .baseUrl(props.baseUrl())
                    .apiKey(props.apiKey())
                    .organizationId(props.organizationId())
                    .projectId(props.projectId())
                    .modelName(props.modelName())
                    .temperature(props.temperature())
                    .topP(props.topP())
                    .stop(props.stop())
                    .maxTokens(props.maxTokens())
                    .maxCompletionTokens(props.maxCompletionTokens())
                    .presencePenalty(props.presencePenalty())
                    .frequencyPenalty(props.frequencyPenalty())
                    .logitBias(props.logitBias())
                    .responseFormat(props.responseFormat())
                    .seed(props.seed())
                    .user(props.user())
                    .strictTools(props.strictTools())
                    .parallelToolCalls(props.parallelToolCalls())
                    .store(props.store())
                    .metadata(props.metadata())
                    .serviceTier(props.serviceTier())
                    .defaultRequestParameters(OpenAiChatRequestParameters.builder()
                            .reasoningEffort(props.reasoningEffort())
                            .customParameters(props.customParameters())
                            .build())
                    .returnThinking(props.returnThinking())
                    .timeout(props.timeout())
                    .logRequests(props.logRequests())
                    .logResponses(props.logResponses())
                    .customHeaders(props.customHeaders())
                    .customQueryParams(props.customQueryParams())
                    .listeners(listeners)
                    .build();
        }

        private String toPascalCase(String input) {
            if (input == null || input.isEmpty()) {
                return input;
            }
            return Arrays.stream(input.split("[-_]"))
                    .map(segment -> {
                        if (segment.isEmpty()) {
                            return "";
                        }
                        return Character.toUpperCase(segment.charAt(0)) + segment.substring(1).toLowerCase();
                    })
                    .collect(Collectors.joining());
        }
    }
}