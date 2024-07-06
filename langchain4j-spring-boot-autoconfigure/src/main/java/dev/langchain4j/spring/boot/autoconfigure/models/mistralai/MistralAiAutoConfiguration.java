package dev.langchain4j.spring.boot.autoconfigure.models.mistralai;

import dev.langchain4j.model.mistralai.MistralAiChatModel;
import dev.langchain4j.model.mistralai.MistralAiEmbeddingModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for Mistral AI clients and models.
 */
@AutoConfiguration(after = {RestClientAutoConfiguration.class})
@ConditionalOnClass({ MistralAiChatModel.class })
@ConditionalOnProperty(prefix = MistralAiProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties({ MistralAiProperties.class, MistralAiChatProperties.class, MistralAiEmbeddingProperties.class })
public class MistralAiAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(MistralAiAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = MistralAiChatProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
    MistralAiChatModel mistralAiChatModel(MistralAiProperties properties, MistralAiChatProperties chatProperties) {
        var chatModel = MistralAiChatModel.builder()
                // Client Config
                .apiKey(properties.getClient().getApiKey())
                .baseUrl(properties.getClient().getBaseUrl().toString())
                .timeout(properties.getClient().getReadTimeout())
                .maxRetries(properties.getClient().getMaxRetries())
                .logRequests(properties.getClient().isLogRequests())
                .logResponses(properties.getClient().isLogResponses())
                // Model Options
                .modelName(chatProperties.getModel())
                .temperature(chatProperties.getTemperature())
                .topP(chatProperties.getTopP())
                .maxTokens(chatProperties.getMaxTokens())
                .safePrompt(chatProperties.isSafePrompt())
                .randomSeed(chatProperties.getRandomSeed())
                .build();

        warnAboutSensitiveInformationExposure(properties.getClient(), MistralAiChatModel.class.getTypeName());

        return chatModel;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = MistralAiEmbeddingProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
    MistralAiEmbeddingModel mistralAiEmbeddingModel(MistralAiProperties properties, MistralAiEmbeddingProperties embeddingProperties) {
        var embeddingModel = MistralAiEmbeddingModel.builder()
                // Client Config
                .apiKey(properties.getClient().getApiKey())
                .baseUrl(properties.getClient().getBaseUrl().toString())
                .timeout(properties.getClient().getReadTimeout())
                .maxRetries(properties.getClient().getMaxRetries())
                .logRequests(properties.getClient().isLogRequests())
                .logResponses(properties.getClient().isLogResponses())
                // Model Options
                .modelName(embeddingProperties.getModel())
                .build();

        warnAboutSensitiveInformationExposure(properties.getClient(), MistralAiEmbeddingModel.class.getTypeName());

        return embeddingModel;
    }

    private static void warnAboutSensitiveInformationExposure(MistralAiProperties.Client client, String modelClassName) {
        if (client.isLogRequests()) {
            logger.warn("You have enabled logging for the entire model request in {}, with the risk of exposing sensitive or private information. Please, be careful!", modelClassName);
        }

        if (client.isLogResponses()) {
            logger.warn("You have enabled logging for the entire model response in {}, with the risk of exposing sensitive or private information. Please, be careful!", modelClassName);
        }
    }

}
