package dev.langchain4j.service.spring.mode.automatic.withRetrievalAugmentor;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.rag.RetrievalAugmentor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
class AiServiceWithRetrievalAugmentorApplication {

    @Bean
    RetrievalAugmentor retrievalAugmentor() {
        return (userMessage, metadata) -> UserMessage.from("My name is Klaus." + userMessage);
    }

    public static void main(String[] args) {
        SpringApplication.run(AiServiceWithRetrievalAugmentorApplication.class, args);
    }
}
