package dev.langchain4j.googleaigemini;


import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.chat.request.ResponseFormatType;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;


import static dev.langchain4j.googleaigemini.Properties.PREFIX;

@AutoConfiguration
@EnableConfigurationProperties(Properties.class)
public class AutoConfig {

    @Bean
    @ConditionalOnProperty(name = PREFIX + ".chatModel.enabled", havingValue = "true")
    ChatLanguageModel googleAiGeminiChatModel(Properties properties) {
        ChatModelProperties chatModelProperties = properties.getChatModel();
        return GoogleAiGeminiChatModel.builder()
                .apiKey(chatModelProperties.getAPI_KEY())
                .modelName(chatModelProperties.getModelName())
                .temperature(chatModelProperties.getTemperature())
                .topP(chatModelProperties.getTopP())
                .topK(chatModelProperties.getTopK())
                .maxOutputTokens(chatModelProperties.getMaxOutputTokens())
                .responseFormat(ResponseFormat.builder().type(ResponseFormatType.JSON).build())
                .build();
    }
}