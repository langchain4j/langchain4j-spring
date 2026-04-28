package dev.langchain4j.service.spring.mode.automatic.conflictingChatModels;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI;

@SpringBootApplication
class AiServiceWithConflictingChatModelsApplication {

    @Bean
    ChatModel chatModel() {
        return OpenAiChatModel.builder()
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .modelName(GPT_4_O_MINI)
                .build();
    }

    @Bean
    ChatModel chatModel2() {
        return OpenAiChatModel.builder()
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .modelName(GPT_4_O_MINI)
                .build();
    }

    public static void main(String[] args) {
        SpringApplication.run(AiServiceWithConflictingChatModelsApplication.class, args);
    }
}
