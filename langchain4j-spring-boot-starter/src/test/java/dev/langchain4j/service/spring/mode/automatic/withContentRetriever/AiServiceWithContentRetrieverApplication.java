package dev.langchain4j.service.spring.mode.automatic.withContentRetriever;

import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import static java.util.Collections.singletonList;

@SpringBootApplication
class AiServiceWithContentRetrieverApplication {

    @Bean
    ContentRetriever contentRetriever() {
        return query -> singletonList(Content.from("My name is Klaus"));
    }

    public static void main(String[] args) {
        SpringApplication.run(AiServiceWithContentRetrieverApplication.class, args);
    }
}
