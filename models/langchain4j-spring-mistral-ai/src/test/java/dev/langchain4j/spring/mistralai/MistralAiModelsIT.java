package dev.langchain4j.spring.mistralai;

import dev.langchain4j.model.mistralai.MistralAiChatModelName;
import dev.langchain4j.model.mistralai.MistralAiModels;
import dev.langchain4j.model.mistralai.internal.api.MistralAiModelCard;
import dev.langchain4j.model.output.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link MistralAiModels}.
 * Adapted from MistralAiModelsIT in the LangChain4j project.
 */
@EnabledIfEnvironmentVariable(named = "MISTRAL_AI_API_KEY", matches = ".*")
class MistralAiModelsIT {

    private static final String apiKey = System.getenv("MISTRAL_AI_API_KEY");

    @Test
    void getAllModels() {
        MistralAiModels models = MistralAiModels.builder()
                .apiKey(apiKey)
                .logRequests(true)
                .build();

        Response<List<MistralAiModelCard>> response = models.availableModels();

        List<MistralAiModelCard> modelCards = response.content();
        assertThat(modelCards.size()).isGreaterThan(0);
        assertThat(modelCards).extracting("id").contains(MistralAiChatModelName.OPEN_MISTRAL_7B.toString());
        assertThat(modelCards).extracting("object").contains("model");
        assertThat(modelCards).extracting("permission").isNotNull();
    }

}
