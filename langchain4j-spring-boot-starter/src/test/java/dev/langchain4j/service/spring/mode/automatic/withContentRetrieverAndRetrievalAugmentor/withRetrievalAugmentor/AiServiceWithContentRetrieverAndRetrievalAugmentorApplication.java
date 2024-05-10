package dev.langchain4j.service.spring.mode.automatic.withContentRetrieverAndRetrievalAugmentor.withRetrievalAugmentor;

import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import static java.util.Collections.singletonList;

@SpringBootApplication
class AiServiceWithContentRetrieverAndRetrievalAugmentorApplication {

    @Bean
    ContentRetriever contentRetriever() {
        return query -> singletonList(Content.from("My name is Klaus."));
    }

    @Bean
    RetrievalAugmentor retrievalAugmentor(ContentRetriever contentRetriever) {
        return DefaultRetrievalAugmentor.builder()
                .contentRetriever(contentRetriever)
                .build();
    }

    public static void main(String[] args) {
        SpringApplication.run(AiServiceWithContentRetrieverAndRetrievalAugmentorApplication.class, args);
    }
}
