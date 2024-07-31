package dev.langchain4j.spring.mistralai;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.mistralai.MistralAiEmbeddingModel;
import dev.langchain4j.model.mistralai.MistralAiEmbeddingModelName;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.output.TokenUsage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link MistralAiEmbeddingModel}.
 * Adapted from MistralAiEmbeddingModelIT in the LangChain4j project.
 */
@EnabledIfEnvironmentVariable(named = "MISTRAL_AI_API_KEY", matches = ".*")
class MistralAiEmbeddingModelIT {

    private static final String apiKey = System.getenv("MISTRAL_AI_API_KEY");

    @Test
    void generateSingleEmbedding() {
        MistralAiEmbeddingModel embeddingModel = MistralAiEmbeddingModel.builder()
                .apiKey(apiKey)
                .modelName(MistralAiEmbeddingModelName.MISTRAL_EMBED)
                .logRequests(true)
                .build();

        String text = "Welcome to the jungle";

        Response<Embedding> response = embeddingModel.embed(text);

        assertThat(response.content().vector()).hasSize(1024);

        TokenUsage tokenUsage = response.tokenUsage();
        assertThat(tokenUsage.inputTokenCount()).isEqualTo(7);
        assertThat(tokenUsage.outputTokenCount()).isEqualTo(0);
        assertThat(tokenUsage.totalTokenCount()).isEqualTo(7);

        assertThat(response.finishReason()).isNull();
    }

    @Test
    void generateMultipleEmbeddings() {
        MistralAiEmbeddingModel embeddingModel = MistralAiEmbeddingModel.builder()
                .apiKey(apiKey)
                .modelName(MistralAiEmbeddingModelName.MISTRAL_EMBED)
                .logRequests(true)
                .build();

        TextSegment textSegment1 = TextSegment.from("Welcome to the jungle");
        TextSegment textSegment2 = TextSegment.from("Welcome to Jumanji");

        Response<List<Embedding>> response = embeddingModel.embedAll(List.of(textSegment1, textSegment2));

        assertThat(response.content()).hasSize(2);
        assertThat(response.content().get(0).dimension()).isEqualTo(1024);
        assertThat(response.content().get(1).dimension()).isEqualTo(1024);

        TokenUsage tokenUsage = response.tokenUsage();
        assertThat(tokenUsage.inputTokenCount()).isEqualTo(14);
        assertThat(tokenUsage.outputTokenCount()).isEqualTo(0);
        assertThat(tokenUsage.totalTokenCount()).isEqualTo(14);

        assertThat(response.finishReason()).isNull();
    }

}
