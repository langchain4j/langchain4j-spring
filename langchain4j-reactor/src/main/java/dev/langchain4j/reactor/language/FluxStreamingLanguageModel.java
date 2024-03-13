package dev.langchain4j.reactor.language;

import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.language.StreamingLanguageModel;
import dev.langchain4j.model.output.Response;
import reactor.core.publisher.Flux;

public interface FluxStreamingLanguageModel extends StreamingLanguageModel {
    default Flux<String> generate(Prompt prompt) {
        return Flux.create(sink -> generate(prompt, new StreamingResponseHandler<String>() {
            @Override
            public void onNext(String token) {
                sink.next(token);
            }

            @Override
            public void onComplete(Response<String> response) {
                sink.next(response.content());
                sink.complete();
            }

            @Override
            public void onError(Throwable error) {
                sink.error(error);
            }
        }));
    }
}
