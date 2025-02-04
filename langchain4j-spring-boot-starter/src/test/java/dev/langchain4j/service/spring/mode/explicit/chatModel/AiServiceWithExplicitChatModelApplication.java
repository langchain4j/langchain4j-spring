package dev.langchain4j.service.spring.mode.explicit.chatModel;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
class AiServiceWithExplicitChatModelApplication {

    static final String CHAT_MODEL_BEAN_NAME = "myChatModel";

    @Bean(CHAT_MODEL_BEAN_NAME)
    ChatLanguageModel chatLanguageModel() {
        return OpenAiChatModel.withApiKey(System.getenv("OPENAI_API_KEY"));
    }

    @Bean(CHAT_MODEL_BEAN_NAME + 2)
    ChatLanguageModel chatLanguageModel2() {
        return messages -> {
            throw new RuntimeException("should never be invoked");
        };
    }

    public static void main(String[] args) {
        SpringApplication.run(AiServiceWithExplicitChatModelApplication.class, args);
    }
}
