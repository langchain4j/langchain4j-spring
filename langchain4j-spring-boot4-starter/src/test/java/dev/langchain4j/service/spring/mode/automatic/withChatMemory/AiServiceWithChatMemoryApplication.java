package dev.langchain4j.service.spring.mode.automatic.withChatMemory;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
class AiServiceWithChatMemoryApplication {

    @Bean
    ChatMemory chatMemory() {
        return MessageWindowChatMemory.withMaxMessages(10);
    }

    public static void main(String[] args) {
        SpringApplication.run(AiServiceWithChatMemoryApplication.class, args);
    }
}
