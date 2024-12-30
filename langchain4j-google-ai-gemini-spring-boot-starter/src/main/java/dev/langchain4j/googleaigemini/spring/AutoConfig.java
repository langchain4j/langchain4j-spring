package dev.langchain4j.googleaigemini.spring;

import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiStreamingChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiTokenizer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import static dev.langchain4j.googleaigemini.spring.Properties.PREFIX;

@AutoConfiguration
@EnableConfigurationProperties(Properties.class)
public class AutoConfig {

    /**
     * Creates a bean for the {@link GoogleAiGeminiChatModel}.
     * <p>
     * This method configures and initializes a chat model using the provided properties.
     * The bean is only created if the property {@code langchain4j.google-ai-gemini.chat-model.api-key} is defined.
     * </p>
     *
     * @param properties the configuration properties containing the chat model settings
     * @return a configured instance of {@link GoogleAiGeminiChatModel}
     */
    @Bean
    @ConditionalOnProperty(name = PREFIX + ".chat-model.api-key")
    GoogleAiGeminiChatModel googleAiGeminiChatModel(Properties properties) {
        ChatModelProperties chatModelProperties = properties.getChatModel();
        return GoogleAiGeminiChatModel.builder()
                .apiKey(chatModelProperties.apiKey())
                .modelName(chatModelProperties.modelName())
                .temperature(chatModelProperties.temperature())
                .maxOutputTokens(chatModelProperties.maxOutputTokens())
                .topK(chatModelProperties.topK())
                .topP(chatModelProperties.topP())
                .maxRetries(chatModelProperties.maxRetries())
                .logRequestsAndResponses(chatModelProperties.logRequestsAndResponses())
                .allowCodeExecution(chatModelProperties.allowCodeExecution())
                .includeCodeExecutionOutput(chatModelProperties.includeCodeExecutionOutput())
                .timeout(chatModelProperties.timeout())
                .build();
    }

    /**
     * Creates a bean for the {@link GoogleAiGeminiStreamingChatModel}.
     * <p>
     * This method configures and initializes a streaming chat model using the provided properties.
     * The bean is only created if the property {@code langchain4j.google-ai-gemini.streaming-chat-model.api-key} is defined.
     * </p>
     *
     * @param properties the configuration properties containing the streaming chat model settings
     * @return a configured instance of {@link GoogleAiGeminiStreamingChatModel}
     */
    @Bean
    @ConditionalOnProperty(name = PREFIX + ".streaming-chat-model.api-key")
    GoogleAiGeminiStreamingChatModel googleAiGeminiStreamingChatModel(Properties properties) {
        ChatModelProperties streamingChatModelProperties = properties.getStreamingChatModel();
        return GoogleAiGeminiStreamingChatModel.builder()
                .apiKey(streamingChatModelProperties.apiKey())
                .modelName(streamingChatModelProperties.modelName())
                .temperature(streamingChatModelProperties.temperature())
                .maxOutputTokens(streamingChatModelProperties.maxOutputTokens())
                .topK(streamingChatModelProperties.topK())
                .topP(streamingChatModelProperties.topP())
                .maxRetries(streamingChatModelProperties.maxRetries())
                .logRequestsAndResponses(streamingChatModelProperties.logRequestsAndResponses())
                .allowCodeExecution(streamingChatModelProperties.allowCodeExecution())
                .includeCodeExecutionOutput(streamingChatModelProperties.includeCodeExecutionOutput())
                .timeout(streamingChatModelProperties.timeout())
                .build();
    }

    /**
     * Creates a bean for the {@link GoogleAiGeminiTokenizer}.
     * <p>
     * This method configures and initializes a tokenizer using the provided properties.
     * The bean is only created if the property {@code langchain4j.google-ai-gemini.tokenizer.api-key} is defined.
     * </p>
     *
     * @param properties the configuration properties containing the tokenizer settings
     * @return a configured instance of {@link GoogleAiGeminiTokenizer}
     */
    @Bean
    @ConditionalOnProperty(name = PREFIX + ".tokenizer.api-key")
    GoogleAiGeminiTokenizer googleAiGeminiTokenizer(Properties properties) {
        TokenizerProperties tokenizerProperties = properties.getTokenizer();
        return GoogleAiGeminiTokenizer.builder()
                .apiKey(tokenizerProperties.apiKey())
                .modelName(tokenizerProperties.modelName())
                .maxRetries(tokenizerProperties.maxRetries())
                .logRequestsAndResponses(tokenizerProperties.logRequestsAndResponses())
                .timeout(tokenizerProperties.timeout())
                .build();
    }

}
