package dev.langchain4j.reactor.chat;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.output.Response;
import reactor.core.publisher.Flux;

import java.util.List;

public interface FluxStreamingChatLanguageModel extends StreamingChatLanguageModel {
    default Flux<AiMessage> generate(List<ChatMessage> messages) {
        return Flux.create(sink -> generate(messages, new StreamingResponseHandler<AiMessage>() {
            @Override
            public void onNext(String token) {
                sink.next(AiMessage.from(token));
            }

            @Override
            public void onError(Throwable error) {
                sink.error(error);
            }

            @Override
            public void onComplete(Response<AiMessage> response) {
                sink.next(response.content());
                sink.complete();
            }
        }));
    }
}
