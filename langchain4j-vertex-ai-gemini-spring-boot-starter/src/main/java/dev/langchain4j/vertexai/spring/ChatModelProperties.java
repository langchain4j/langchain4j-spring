package dev.langchain4j.vertexai.spring;

public record ChatModelProperties(
    String project,
    String location,
    String modelName,
    Float temperature,
    Integer maxOutputTokens,
    Integer topK,
    Float topP,
    Integer maxRetries) {

}
