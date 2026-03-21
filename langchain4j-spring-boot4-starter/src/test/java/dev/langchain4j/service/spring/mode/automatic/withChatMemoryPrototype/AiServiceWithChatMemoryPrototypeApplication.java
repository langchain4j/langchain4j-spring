package dev.langchain4j.service.spring.mode.automatic.withChatMemoryPrototype;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

@SpringBootApplication
class AiServiceWithChatMemoryPrototypeApplication {

    @Bean
    @Scope("prototype")
    ChatMemory chatMemory() {
        return MessageWindowChatMemory.withMaxMessages(10);
    }

    public static void main(String[] args) {
        SpringApplication.run(AiServiceWithChatMemoryPrototypeApplication.class, args);
    }
}
