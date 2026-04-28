package dev.langchain4j.service.spring.mode.automatic.conflictingSyncAndStreamingModels.streaming;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
class AiServiceWithConflictingSyncAndStreamingModelsApplication {

    @Bean
    ChatModel chatModel() {
        return new ChatModel() {

            @Override
            public ChatResponse chat(ChatRequest chatRequest) {
                throw new RuntimeException("should never be invoked");
            }
        };
    }

    public static void main(String[] args) {
        SpringApplication.run(AiServiceWithConflictingSyncAndStreamingModelsApplication.class, args);
    }
}
