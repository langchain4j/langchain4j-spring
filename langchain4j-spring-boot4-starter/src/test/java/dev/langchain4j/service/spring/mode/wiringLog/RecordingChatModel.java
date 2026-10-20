package dev.langchain4j.service.spring.mode.wiringLog;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Calls the {@code currentStatus} tool when tools are available, then answers with the tool result.
 */
class RecordingChatModel implements ChatModel {

    private final List<ChatRequest> requests = new CopyOnWriteArrayList<>();

    @Override
    public ChatResponse doChat(ChatRequest chatRequest) {
        requests.add(chatRequest);
        List<ChatMessage> messages = chatRequest.messages();
        AiMessage aiMessage;
        if (messages.get(messages.size() - 1) instanceof ToolExecutionResultMessage toolResult) {
            aiMessage = AiMessage.from("Tool said: " + toolResult.text());
        } else if (chatRequest.toolSpecifications() != null && !chatRequest.toolSpecifications().isEmpty()) {
            aiMessage = AiMessage.from(ToolExecutionRequest.builder()
                    .id("1")
                    .name("currentStatus")
                    .arguments("{}")
                    .build());
        } else {
            aiMessage = AiMessage.from("No tools available");
        }
        return ChatResponse.builder().aiMessage(aiMessage).build();
    }

    List<ChatRequest> requests() {
        return List.copyOf(requests);
    }
}
