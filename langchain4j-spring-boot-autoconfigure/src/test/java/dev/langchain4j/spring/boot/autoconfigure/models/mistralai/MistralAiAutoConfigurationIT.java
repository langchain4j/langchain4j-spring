package dev.langchain4j.spring.boot.autoconfigure.models.mistralai;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.mistralai.MistralAiChatModel;
import dev.langchain4j.model.mistralai.MistralAiEmbeddingModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link MistralAiAutoConfiguration}.
 */
@EnabledIfEnvironmentVariable(named = "MISTRAL_AI_API_KEY", matches = ".*")
class MistralAiAutoConfigurationIT {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withPropertyValues("langchain4j.mistralai.client.apiKey=" + System.getenv("MISTRAL_AI_API_KEY"))
        .withPropertyValues("langchain4j.mistralai.client.logRequests=true")
        .withConfiguration(AutoConfigurations.of(RestClientAutoConfiguration.class, MistralAiAutoConfiguration.class));

    @Test
    void chat() {
        contextRunner.run(context -> {
            MistralAiChatModel model = context.getBean(MistralAiChatModel.class);
            String response = model.generate("What is the capital of Italy?");
            assertThat(response).containsIgnoringCase("Rome");
        });
    }

    @Test
    void embedding() {
        contextRunner.run(context -> {
            MistralAiEmbeddingModel model = context.getBean(MistralAiEmbeddingModel.class);
            Embedding embedding = model.embed("hi").content();
            assertThat(embedding.dimension()).isEqualTo(1024);
        });
    }

}
