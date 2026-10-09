package dev.langchain4j.spring.it;

import dev.langchain4j.service.AiServiceStreamingEvent;
import dev.langchain4j.service.spring.AiService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

@AiService
interface Assistant {

    String chat(String userMessage);

    Flux<String> chatFlux(String userMessage);

    CompletableFuture<String> chatAsync(String userMessage);

    Mono<String> chatMono(String userMessage);

    Flux<AiServiceStreamingEvent> chatEvents(String userMessage);
}
