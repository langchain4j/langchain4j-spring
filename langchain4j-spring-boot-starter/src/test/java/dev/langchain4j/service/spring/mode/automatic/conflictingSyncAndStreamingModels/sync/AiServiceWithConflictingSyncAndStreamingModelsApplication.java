package dev.langchain4j.service.spring.mode.automatic.conflictingSyncAndStreamingModels.sync;

import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
class AiServiceWithConflictingSyncAndStreamingModelsApplication {

    @Bean
    StreamingChatLanguageModel streamingChatLanguageModel() {
        return new StreamingChatLanguageModel() {

            @Override
            public void chat(ChatRequest chatRequest, StreamingChatResponseHandler handler) {
                throw new RuntimeException("should never be invoked");
            }
        };
    }

    public static void main(String[] args) {
        SpringApplication.run(AiServiceWithConflictingSyncAndStreamingModelsApplication.class, args);
    }
}
