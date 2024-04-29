package dev.langchain4j.reactor.chat;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.output.Response;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

class FluxStreamingChatLanguageModelTest {

    public static class StreamingUpperCaseEchoModel implements FluxStreamingChatLanguageModel {
        @Override
        public void generate(List<ChatMessage> messages, StreamingResponseHandler<AiMessage> handler) {
            ChatMessage lastMessage = messages.get(messages.size() - 1);
            Response<AiMessage> response = new Response<>(new AiMessage(lastMessage.text().toUpperCase(Locale.ROOT)));
            handler.onComplete(response);
        }
    }

    @Test
    public void test_generate() {
        FluxStreamingChatLanguageModel model = new StreamingUpperCaseEchoModel();
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new UserMessage("Hello"));
        messages.add(new AiMessage("Hi"));
        messages.add(new UserMessage("How are you?"));

        StepVerifier.create(model.generate(messages))
                .expectNext(new AiMessage("HOW ARE YOU?"))
                .verifyComplete();
    }
}