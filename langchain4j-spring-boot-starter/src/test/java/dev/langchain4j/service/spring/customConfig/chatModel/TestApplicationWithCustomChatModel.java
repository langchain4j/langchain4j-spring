package dev.langchain4j.service.spring.customConfig.chatModel;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class TestApplicationWithCustomChatModel {

    static final String CUSTOM_CHAT_MODEL_BEAN_NAME = "customChatModel";

    @Bean(CUSTOM_CHAT_MODEL_BEAN_NAME)
    ChatLanguageModel chatLanguageModel() {
        return OpenAiChatModel.withApiKey(System.getenv("OPENAI_API_KEY"));
    }

    @Bean(CUSTOM_CHAT_MODEL_BEAN_NAME + 2)
    ChatLanguageModel chatLanguageModel2() {
        return OpenAiChatModel.withApiKey(System.getenv("OPENAI_API_KEY"));
    }

    public static void main(String[] args) {
        SpringApplication.run(TestApplicationWithCustomChatModel.class, args);
    }
}
