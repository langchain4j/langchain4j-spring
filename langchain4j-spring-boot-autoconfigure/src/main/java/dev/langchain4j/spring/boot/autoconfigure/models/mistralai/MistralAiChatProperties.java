package dev.langchain4j.spring.boot.autoconfigure.models.mistralai;

import dev.langchain4j.model.mistralai.MistralAiChatModelName;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Mistral AI chat models.
 */
@ConfigurationProperties(prefix = MistralAiChatProperties.CONFIG_PREFIX)
public class MistralAiChatProperties {

    public static final String CONFIG_PREFIX = "langchain4j.mistralai.chat";

    /**
     * Whether to enable the Mistral AI chat models.
     */
    private boolean enabled = true;

    /**
     * ID of the model to use.
     */
    private String model = MistralAiChatModelName.OPEN_MISTRAL_7B.toString();
    /**
     * What sampling temperature to use, between 0.0 and 1.0. Higher values like 0.8 will make the output more random, while lower values like 0.2 will make it more focused and deterministic. We generally recommend altering this or "top_p" but not both.
     */
    private Double temperature = 0.7;
    /**
     * Nucleus sampling, where the model considers the results of the tokens with top_p probability mass. So 0.1 means only the tokens comprising the top 10% probability mass are considered. We generally recommend altering this or "temperature" but not both.
     */
    private Double topP = 1.0;
    /**
     * The maximum number of tokens to generate in the completion. The token count of your prompt plus "max_tokens" cannot exceed the model's context length.
     */
    private Integer maxTokens;
    /**
     * Whether to inject a safety prompt before all conversations.
     */
    private boolean safePrompt = false;
    /**
     * The seed to use for random sampling. If set, different calls will generate deterministic results.
     */
    private Integer randomSeed;

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

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Double getTopP() {
        return topP;
    }

    public void setTopP(Double topP) {
        this.topP = topP;
    }

    public Integer getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }

    public boolean isSafePrompt() {
        return safePrompt;
    }

    public void setSafePrompt(boolean safePrompt) {
        this.safePrompt = safePrompt;
    }

    public Integer getRandomSeed() {
        return randomSeed;
    }

    public void setRandomSeed(Integer randomSeed) {
        this.randomSeed = randomSeed;
    }

}
