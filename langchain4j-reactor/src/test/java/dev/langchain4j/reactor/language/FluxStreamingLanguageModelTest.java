package dev.langchain4j.reactor.language;

import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.output.Response;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class FluxStreamingLanguageModelTest {

    public static class EchoStreamingLanguageModel implements FluxStreamingLanguageModel {
        @Override
        public void generate(String prompt, StreamingResponseHandler<String> handler) {
            handler.onComplete(new Response<>(prompt));
        }
    }

    @Test
    public void test_generate_flux() {
        FluxStreamingLanguageModel model = new EchoStreamingLanguageModel();
        StepVerifier.create(model.generate(new Prompt("text")))
                .expectNext("text")
                .verifyComplete();
    }
}