package dev.langchain4j.spring.boot.autoconfigure.models.mistralai;

import dev.langchain4j.model.mistralai.MistralAiChatModel;
import dev.langchain4j.model.mistralai.MistralAiEmbeddingModel;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link MistralAiAutoConfiguration}.
 */
class MistralAiAutoConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withPropertyValues("langchain4j.mistralai.client.api-key=demo")
            .withConfiguration(AutoConfigurations.of(RestClientAutoConfiguration.class, MistralAiAutoConfiguration.class));

    @Test
    void chat() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(MistralAiChatModel.class);
        });
    }

    @Test
    void chatDisabled() {
        contextRunner.withPropertyValues("langchain4j.mistralai.chat.enabled=false").run(context -> {
            assertThat(context).doesNotHaveBean(MistralAiChatModel.class);
        });
    }

    @Test
    void embedding() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(MistralAiEmbeddingModel.class);
        });
    }

    @Test
    void embeddingDisabled() {
        contextRunner.withPropertyValues("langchain4j.mistralai.embedding.enabled=false").run(context -> {
            assertThat(context).doesNotHaveBean(MistralAiEmbeddingModel.class);
        });
    }

    @Test
    void disabled() {
        contextRunner.withPropertyValues("langchain4j.mistralai.enabled=false").run(context -> {
            assertThat(context).doesNotHaveBean(MistralAiChatModel.class);
            assertThat(context).doesNotHaveBean(MistralAiEmbeddingModel.class);
        });
    }

}