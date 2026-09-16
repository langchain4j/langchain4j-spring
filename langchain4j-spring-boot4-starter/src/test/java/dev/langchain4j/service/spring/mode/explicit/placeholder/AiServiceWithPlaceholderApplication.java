package dev.langchain4j.service.spring.mode.explicit.placeholder;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.mock.ChatModelMock;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@SpringBootApplication
class AiServiceWithPlaceholderApplication {

    static final String CONFIGURED_CHAT_MODEL_BEAN_NAME = "configuredChatModel";
    static final String FALLBACK_CHAT_MODEL_BEAN_NAME = "fallbackChatModel";
    static final String CONFIGURED_TOOL_BEAN_NAME = "configuredTool";
    static final String SECOND_TOOL_BEAN_NAME = "secondTool";

    @Bean(CONFIGURED_CHAT_MODEL_BEAN_NAME)
    ChatModel configuredChatModel() {
        return ChatModelMock.thatAlwaysResponds("ConfiguredModelResponse");
    }

    @Bean(FALLBACK_CHAT_MODEL_BEAN_NAME)
    ChatModel fallbackChatModel() {
        return ChatModelMock.thatAlwaysResponds("FallbackModelResponse");
    }

    @Bean("otherChatModel")
    ChatModel otherChatModel() {
        return new ChatModel() {
            @Override
            public ChatResponse chat(ChatRequest chatRequest) {
                throw new RuntimeException("should never be invoked");
            }
        };
    }

    @Component(CONFIGURED_TOOL_BEAN_NAME)
    static class MyTool {
        @Tool
        String helperTool() {
            return "toolResult";
        }
    }

    @Component(SECOND_TOOL_BEAN_NAME)
    static class SecondTool {
        @Tool
        String secondHelperTool() {
            return "secondToolResult";
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(AiServiceWithPlaceholderApplication.class, args);
    }
}
