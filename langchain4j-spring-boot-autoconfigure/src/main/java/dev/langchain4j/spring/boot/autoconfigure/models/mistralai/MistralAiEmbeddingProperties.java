package dev.langchain4j.spring.boot.autoconfigure.models.mistralai;

import dev.langchain4j.model.mistralai.MistralAiEmbeddingModelName;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Mistral AI chat models.
 */
@ConfigurationProperties(prefix = MistralAiEmbeddingProperties.CONFIG_PREFIX)
public class MistralAiEmbeddingProperties {

    public static final String CONFIG_PREFIX = "langchain4j.mistralai.embedding";

    /**
     * Whether to enable the Mistral AI embedding models.
     */
    private boolean enabled = true;

    /**
     * ID of the model to use.
     */
    private String model = MistralAiEmbeddingModelName.MISTRAL_EMBED.toString();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

}
