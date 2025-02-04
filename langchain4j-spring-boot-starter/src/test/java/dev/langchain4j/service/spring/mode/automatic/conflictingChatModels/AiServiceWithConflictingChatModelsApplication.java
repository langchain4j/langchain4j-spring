package dev.langchain4j.service.spring.mode.automatic.conflictingChatModels;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
class AiServiceWithConflictingChatModelsApplication {

    @Bean
    ChatLanguageModel chatLanguageModel() {
        return OpenAiChatModel.withApiKey(System.getenv("OPENAI_API_KEY"));
    }

    @Bean
    ChatLanguageModel chatLanguageModel2() {
        return OpenAiChatModel.withApiKey(System.getenv("OPENAI_API_KEY"));
    }

    public static void main(String[] args) {
        SpringApplication.run(AiServiceWithConflictingChatModelsApplication.class, args);
    }
}
