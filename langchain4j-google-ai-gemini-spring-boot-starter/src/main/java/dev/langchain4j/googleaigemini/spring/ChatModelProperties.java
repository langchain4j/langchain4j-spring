package dev.langchain4j.googleaigemini.spring;

import java.time.Duration;

/**
 * Configuration properties for the Google AI Gemini Chat Model.
 * <p>
 * This class defines the necessary properties for configuring
 * and using the chat model.
 * </p>
 *
 * @param apiKey                        The API key for authenticating requests to the Google AI Gemini service.
 * @param modelName                     The name of the model to use.
 * @param temperature                   The temperature setting to control response randomness.
 * @param maxOutputTokens               The maximum number of tokens to include in the model's output.
 * @param topK                          The top-K sampling parameter to refine the response.
 * @param topP                          The top-P (nucleus sampling) parameter for controlling diversity.
 * @param maxRetries                    The maximum number of retries for failed requests.
 * @param timeout                       The timeout duration for chat model requests.
 * @param logRequestsAndResponses       Flag to enable or disable logging of requests and responses.
 * @param allowCodeExecution            Flag to allow or disallow the execution of code.
 * @param includeCodeExecutionOutput    Flag to include or exclude code execution output in the response.
 */
record ChatModelProperties(
        String apiKey,
        String modelName,
        Double temperature,
        Integer maxOutputTokens,
        Integer topK,
        Double topP,
        Integer maxRetries,
        Duration timeout,
        boolean logRequestsAndResponses,
        boolean allowCodeExecution,
        boolean includeCodeExecutionOutput
) {
}
