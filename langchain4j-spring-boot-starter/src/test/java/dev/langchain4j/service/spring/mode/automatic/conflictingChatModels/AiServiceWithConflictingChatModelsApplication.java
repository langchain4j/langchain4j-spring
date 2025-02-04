package dev.langchain4j.service.spring.mode.automatic.conflictingChatModels;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI;

@SpringBootApplication
class AiServiceWithConflictingChatModelsApplication {

    @Bean
    ChatLanguageModel chatLanguageModel() {
        return OpenAiChatModel.builder()
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .modelName(GPT_4_O_MINI)
                .build();
    }

    @Bean
    ChatLanguageModel chatLanguageModel2() {
        return OpenAiChatModel.builder()
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .modelName(GPT_4_O_MINI)
                .build();
    }

    public static void main(String[] args) {
        SpringApplication.run(AiServiceWithConflictingChatModelsApplication.class, args);
    }
}
