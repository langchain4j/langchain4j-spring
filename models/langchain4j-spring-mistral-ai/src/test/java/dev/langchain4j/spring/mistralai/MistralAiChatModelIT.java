package dev.langchain4j.spring.mistralai;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.*;
import dev.langchain4j.model.mistralai.MistralAiChatModel;
import dev.langchain4j.model.mistralai.MistralAiChatModelName;
import dev.langchain4j.model.mistralai.internal.api.MistralAiResponseFormatType;
import dev.langchain4j.model.output.FinishReason;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.output.TokenUsage;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.util.List;

import static dev.langchain4j.agent.tool.JsonSchemaProperty.INTEGER;
import static dev.langchain4j.data.message.UserMessage.userMessage;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link MistralAiChatModel}.
 * Adapted from MistralAiChatModelIT in the LangChain4j project.
 */
@EnabledIfEnvironmentVariable(named = "MISTRAL_AI_API_KEY", matches = ".*")
class MistralAiChatModelIT {

    private static final String apiKey = System.getenv("MISTRAL_AI_API_KEY");

    private final ToolSpecification calculator = ToolSpecification.builder()
            .name("calculator")
            .description("returns a sum of two numbers")
            .addParameter("first", INTEGER)
            .addParameter("second", INTEGER)
            .build();

    @Test
    void generateText() {
        MistralAiChatModel chatModel = MistralAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(MistralAiChatModelName.OPEN_MISTRAL_7B)
                .logRequests(true)
                .build();

        UserMessage userMessage = UserMessage.from("What is the capital of Italy?");

        Response<AiMessage> response = chatModel.generate(userMessage);

        AiMessage aiMessage = response.content();
        assertThat(aiMessage.text()).contains("Rome");
        assertThat(aiMessage.toolExecutionRequests()).isNull();

        TokenUsage tokenUsage = response.tokenUsage();
        assertThat(tokenUsage.inputTokenCount()).isGreaterThan(0);
        assertThat(tokenUsage.outputTokenCount()).isGreaterThan(0);
        assertThat(tokenUsage.totalTokenCount())
                .isEqualTo(tokenUsage.inputTokenCount() + tokenUsage.outputTokenCount());

        assertThat(response.finishReason()).isEqualTo(FinishReason.STOP);
    }

    @Test
    void generateTextTooLong() {
        MistralAiChatModel chatModel = MistralAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(MistralAiChatModelName.OPEN_MISTRAL_7B)
                .maxTokens(1)
                .logRequests(true)
                .build();

        UserMessage userMessage = UserMessage.from("What is the capital of Denmark?");

        Response<AiMessage> response = chatModel.generate(userMessage);

        AiMessage aiMessage = response.content();
        assertThat(aiMessage.text()).isNotBlank();

        TokenUsage tokenUsage = response.tokenUsage();
        assertThat(tokenUsage.inputTokenCount()).isGreaterThan(0);
        assertThat(tokenUsage.outputTokenCount()).isEqualTo(1);
        assertThat(tokenUsage.totalTokenCount())
                .isEqualTo(tokenUsage.inputTokenCount() + tokenUsage.outputTokenCount());

        assertThat(response.finishReason()).isEqualTo(FinishReason.LENGTH);
    }

    @Test
    void generateTextWithFewShots() {
        MistralAiChatModel chatModel = MistralAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(MistralAiChatModelName.OPEN_MISTRAL_7B)
                .logRequests(true)
                .build();

        List<ChatMessage> messages = List.of(
                UserMessage.from("1 + 1 ="), AiMessage.from(">>> 2"),
                UserMessage.from("2 + 2 ="), AiMessage.from(">>> 4"),
                UserMessage.from("4 + 4 ="));

        Response<AiMessage> response = chatModel.generate(messages);

        assertThat(response.content().text()).startsWith(">>> 8");
    }

    @Test
    void generateTextWithSystemMessage() {
        MistralAiChatModel chatModel = MistralAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(MistralAiChatModelName.OPEN_MISTRAL_7B)
                .logRequests(true)
                .build();

        SystemMessage systemMessage = SystemMessage.from("Start every answer with Ahoy");
        UserMessage userMessage = UserMessage.from("Hello, captain!");

        Response<AiMessage> response = chatModel.generate(systemMessage, userMessage);

        assertThat(response.content().text()).containsIgnoringCase("Ahoy");
    }

    @Test
    void generateTextAsJson() throws JSONException {
        MistralAiChatModel chatModel = MistralAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(MistralAiChatModelName.OPEN_MISTRAL_7B)
                .temperature(0.1)
                .responseFormat(MistralAiResponseFormatType.JSON_OBJECT)
                .logRequests(true)
                .build();

        String response = chatModel
                .generate("Return a JSON object with two fields: location is Jungle and name is Jumanji.");

        JSONAssert.assertEquals("""
                {
                  "name": "Jumanji",
                  "location": "Jungle"
                }
                """, response, JSONCompareMode.STRICT);
    }

    @Test
    void executeToolExplicitlyAndThenGenerateAnswer() throws JSONException {
        MistralAiChatModel chatModel = MistralAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(MistralAiChatModelName.OPEN_MIXTRAL_8X22B)
                .temperature(0.1)
                .logRequests(true)
                .build();

        // Execute tool

        UserMessage userMessage = userMessage("2+2=?");
        List<ToolSpecification> toolSpecifications = List.of(calculator);

        Response<AiMessage> response = chatModel.generate(List.of(userMessage), toolSpecifications);

        AiMessage aiMessage = response.content();
        assertThat(aiMessage.text()).isNull();
        assertThat(aiMessage.toolExecutionRequests()).hasSize(1);

        ToolExecutionRequest toolExecutionRequest = aiMessage.toolExecutionRequests().get(0);
        assertThat(toolExecutionRequest.id()).isNotBlank();
        assertThat(toolExecutionRequest.name()).isEqualTo("calculator");
        JSONAssert.assertEquals("""
                {
                  "first": 2,
                  "second": 2
                }
                """, toolExecutionRequest.arguments(), JSONCompareMode.STRICT);

        TokenUsage tokenUsage = response.tokenUsage();
        assertThat(tokenUsage.inputTokenCount()).isGreaterThan(0);
        assertThat(tokenUsage.outputTokenCount()).isGreaterThan(0);
        assertThat(tokenUsage.totalTokenCount())
                .isEqualTo(tokenUsage.inputTokenCount() + tokenUsage.outputTokenCount());

        assertThat(response.finishReason()).isEqualTo(FinishReason.TOOL_EXECUTION);

        // Then answer

        ToolExecutionResultMessage toolExecutionResultMessage = ToolExecutionResultMessage.from(toolExecutionRequest, "4");
        List<ChatMessage> messages = List.of(userMessage, aiMessage, toolExecutionResultMessage);

        Response<AiMessage> secondResponse = chatModel.generate(messages);

        AiMessage secondAiMessage = secondResponse.content();
        assertThat(secondAiMessage.text()).contains("4");
        assertThat(secondAiMessage.toolExecutionRequests()).isNull();

        TokenUsage secondTokenUsage = secondResponse.tokenUsage();
        assertThat(secondTokenUsage.inputTokenCount()).isGreaterThan(0);
        assertThat(secondTokenUsage.outputTokenCount()).isGreaterThan(0);
        assertThat(secondTokenUsage.totalTokenCount())
                .isEqualTo(secondTokenUsage.inputTokenCount() + secondTokenUsage.outputTokenCount());

        assertThat(secondResponse.finishReason()).isEqualTo(FinishReason.STOP);
    }

    @Test
    void executeToolImplicitlyAndThenGenerateAnswer() throws JSONException {
        MistralAiChatModel chatModel = MistralAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(MistralAiChatModelName.OPEN_MIXTRAL_8X22B)
                .temperature(0.1)
                .logRequests(true)
                .build();

        // Execute tool

        UserMessage userMessage = userMessage("2+2=?");

        Response<AiMessage> response = chatModel.generate(List.of(userMessage), calculator);

        AiMessage aiMessage = response.content();
        assertThat(aiMessage.text()).isNull();
        assertThat(aiMessage.toolExecutionRequests()).hasSize(1);

        ToolExecutionRequest toolExecutionRequest = aiMessage.toolExecutionRequests().get(0);
        assertThat(toolExecutionRequest.id()).isNotBlank();
        assertThat(toolExecutionRequest.name()).isEqualTo("calculator");
        JSONAssert.assertEquals("""
                {
                  "first": 2,
                  "second": 2
                }
                """, toolExecutionRequest.arguments(), JSONCompareMode.STRICT);

        TokenUsage tokenUsage = response.tokenUsage();
        assertThat(tokenUsage.inputTokenCount()).isGreaterThan(0);
        assertThat(tokenUsage.outputTokenCount()).isGreaterThan(0);
        assertThat(tokenUsage.totalTokenCount())
                .isEqualTo(tokenUsage.inputTokenCount() + tokenUsage.outputTokenCount());

        assertThat(response.finishReason()).isEqualTo(FinishReason.TOOL_EXECUTION);

        // Then answer

        ToolExecutionResultMessage toolExecutionResultMessage = ToolExecutionResultMessage.from(toolExecutionRequest, "4");
        List<ChatMessage> messages = List.of(userMessage, aiMessage, toolExecutionResultMessage);

        Response<AiMessage> secondResponse = chatModel.generate(messages);

        AiMessage secondAiMessage = secondResponse.content();
        assertThat(secondAiMessage.text()).contains("4");
        assertThat(secondAiMessage.toolExecutionRequests()).isNull();

        TokenUsage secondTokenUsage = secondResponse.tokenUsage();
        assertThat(secondTokenUsage.inputTokenCount()).isGreaterThan(0);
        assertThat(secondTokenUsage.outputTokenCount()).isGreaterThan(0);
        assertThat(secondTokenUsage.totalTokenCount())
                .isEqualTo(secondTokenUsage.inputTokenCount() + secondTokenUsage.outputTokenCount());

        assertThat(secondResponse.finishReason()).isEqualTo(FinishReason.STOP);
    }

}
