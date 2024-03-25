package dev.langchain4j.service.spring.mode.automatic.withChatMemoryProvider;

import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
class AiServiceWithChatMemoryProviderApplication {

    @Bean
    ChatMemoryProvider chatMemoryProvider() {
        return memoryId -> MessageWindowChatMemory.withMaxMessages(10);
    }

    public static void main(String[] args) {
        SpringApplication.run(AiServiceWithChatMemoryProviderApplication.class, args);
    }
}
