package dev.langchain4j.service.spring.mode.explicit.placeholder;

import dev.langchain4j.service.spring.AiServicesAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class AiServiceWithPlaceholderTest {

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AiServicesAutoConfiguration.class));

    @Test
    void should_resolve_model_name_from_placeholder() {
        contextRunner
                .withUserConfiguration(AiServiceWithPlaceholderApplication.class)
                .withPropertyValues(
                        "my.chat-model.name=" + AiServiceWithPlaceholderApplication.CONFIGURED_CHAT_MODEL_BEAN_NAME,
                        "my.tool.name=" + AiServiceWithPlaceholderApplication.CONFIGURED_TOOL_BEAN_NAME
                )
                .run(context -> {
                    AiServiceWithPlaceholderChatModel aiService = context.getBean(AiServiceWithPlaceholderChatModel.class);
                    String response = aiService.chat("Hello");
                    assertThat(response).isEqualTo("ConfiguredModelResponse");
                });
    }

    @Test
    void should_resolve_model_name_from_placeholder_with_default_value() {
        contextRunner
                .withUserConfiguration(AiServiceWithPlaceholderApplication.class)
                .withPropertyValues(
                        "my.chat-model.name=" + AiServiceWithPlaceholderApplication.CONFIGURED_CHAT_MODEL_BEAN_NAME,
                        "my.tool.name=" + AiServiceWithPlaceholderApplication.CONFIGURED_TOOL_BEAN_NAME
                )
                .run(context -> {
                    AiServiceWithPlaceholderFallbackModel aiService = context.getBean(AiServiceWithPlaceholderFallbackModel.class);
                    String response = aiService.chat("Hello");
                    assertThat(response).isEqualTo("FallbackModelResponse");
                });
    }

    @Test
    void should_override_default_value_when_property_is_specified() {
        contextRunner
                .withUserConfiguration(AiServiceWithPlaceholderApplication.class)
                .withPropertyValues(
                        "my.chat-model.name=" + AiServiceWithPlaceholderApplication.CONFIGURED_CHAT_MODEL_BEAN_NAME,
                        "my.missing.chat-model=" + AiServiceWithPlaceholderApplication.CONFIGURED_CHAT_MODEL_BEAN_NAME,
                        "my.tool.name=" + AiServiceWithPlaceholderApplication.CONFIGURED_TOOL_BEAN_NAME
                )
                .run(context -> {
                    AiServiceWithPlaceholderFallbackModel aiService = context.getBean(AiServiceWithPlaceholderFallbackModel.class);
                    String response = aiService.chat("Hello");
                    assertThat(response).isEqualTo("ConfiguredModelResponse");
                });
    }

    @Test
    void should_resolve_tool_name_from_placeholder() {
        contextRunner
                .withUserConfiguration(AiServiceWithPlaceholderApplication.class)
                .withPropertyValues(
                        "my.chat-model.name=" + AiServiceWithPlaceholderApplication.CONFIGURED_CHAT_MODEL_BEAN_NAME,
                        "my.tool.name=" + AiServiceWithPlaceholderApplication.CONFIGURED_TOOL_BEAN_NAME
                )
                .run(context -> {
                    AiServiceWithPlaceholderTools aiService = context.getBean(AiServiceWithPlaceholderTools.class);
                    assertThat(aiService).isNotNull();
                    String response = aiService.chat("Hello");
                    assertThat(response).isEqualTo("ConfiguredModelResponse");
                });
    }

    @Test
    void should_resolve_multiple_tool_names_from_comma_separated_placeholder() {
        contextRunner
                .withUserConfiguration(AiServiceWithPlaceholderApplication.class)
                .withPropertyValues(
                        "my.chat-model.name=" + AiServiceWithPlaceholderApplication.CONFIGURED_CHAT_MODEL_BEAN_NAME,
                        "my.tool.name=" + AiServiceWithPlaceholderApplication.CONFIGURED_TOOL_BEAN_NAME + ", "
                                + AiServiceWithPlaceholderApplication.SECOND_TOOL_BEAN_NAME
                )
                .run(context -> {
                    AiServiceWithPlaceholderTools aiService = context.getBean(AiServiceWithPlaceholderTools.class);
                    assertThat(aiService).isNotNull();
                    String response = aiService.chat("Hello");
                    assertThat(response).isEqualTo("ConfiguredModelResponse");
                });
    }

    @Test
    void should_fail_when_placeholder_cannot_be_resolved() {
        contextRunner
                .withUserConfiguration(AiServiceWithPlaceholderApplication.class)
                .withPropertyValues(
                        "my.tool.name=" + AiServiceWithPlaceholderApplication.CONFIGURED_TOOL_BEAN_NAME
                )
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .isInstanceOf(IllegalArgumentException.class)
                            .hasMessageContaining("Could not resolve placeholder 'my.chat-model.name'");
                });
    }
}
