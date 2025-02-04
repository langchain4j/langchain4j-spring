package dev.langchain4j.service.spring.mode.explicit.multiple;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
class MultipleAiServicesApplication {

    static final String CHAT_MODEL_BEAN_NAME = "myChatModel";

    @Bean(CHAT_MODEL_BEAN_NAME)
    ChatLanguageModel chatLanguageModel() {
        return OpenAiChatModel.withApiKey(System.getenv("OPENAI_API_KEY"));
    }

    @Bean
    ChatMemory chatMemory() {
        return MessageWindowChatMemory.withMaxMessages(10);
    }

    public static void main(String[] args) {
        SpringApplication.run(MultipleAiServicesApplication.class, args);
    }
}
