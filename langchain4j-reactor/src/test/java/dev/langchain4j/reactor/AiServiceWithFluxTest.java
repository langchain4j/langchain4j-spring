package dev.langchain4j.reactor;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.mock.StreamingChatModelMock;
import dev.langchain4j.service.AiServices;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;

public class AiServiceWithFluxTest {

    interface Assistant {

        Flux<String> stream(String userMessage);
    }

    @Test
    void should_stream() {

        // given
        List<String> tokens = List.of("H", "e", "l", "l", "o");

        StreamingChatModel model = StreamingChatModelMock.thatAlwaysStreams(tokens);

        Assistant assistant = AiServices.builder(Assistant.class)
                .streamingChatModel(model)
                .build();

        // when
        Flux<String> flux = assistant.stream("Hi");

        // then
        StepVerifier.create(flux)
                .expectNextSequence(tokens)
                .verifyComplete();
    }
}
