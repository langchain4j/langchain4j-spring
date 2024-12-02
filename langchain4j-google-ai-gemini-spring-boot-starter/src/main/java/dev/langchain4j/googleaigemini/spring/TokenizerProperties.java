package dev.langchain4j.googleaigemini.spring;

import java.time.Duration;

/**
 * Configuration properties for the Google AI Gemini Tokenizer.
 * <p>
 * This class defines the necessary properties for configuring
 * and using the tokenizer.
 * </p>
 *
 * @param apiKey                    The API key for authenticating requests to the Google AI Gemini service.
 * @param modelName                 The name of the model to use.
 * @param maxRetries                The maximum number of retries for failed requests.
 * @param logRequestsAndResponses   Flag to enable or disable logging of requests and responses.
 * @param timeout                   The timeout duration for tokenizer requests.
 */
record TokenizerProperties(
        String apiKey,
        String modelName,
        Integer maxRetries,
        boolean logRequestsAndResponses,
        Duration timeout
) {
}
