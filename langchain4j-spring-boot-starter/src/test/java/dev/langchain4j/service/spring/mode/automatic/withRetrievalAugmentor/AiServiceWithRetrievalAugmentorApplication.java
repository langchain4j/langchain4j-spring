package dev.langchain4j.service.spring.mode.automatic.withRetrievalAugmentor;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.rag.AugmentationResult;
import dev.langchain4j.rag.RetrievalAugmentor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
class AiServiceWithRetrievalAugmentorApplication {

    @Bean
    RetrievalAugmentor retrievalAugmentor() {
        return augmentationRequest -> AugmentationResult.builder()
                .chatMessage(UserMessage.from("My name is Klaus." + augmentationRequest.chatMessage()))
                .build();
    }

    public static void main(String[] args) {
        SpringApplication.run(AiServiceWithRetrievalAugmentorApplication.class, args);
    }
}
