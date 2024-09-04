package dev.langchain4j.dashscope.spring;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
class ChatModelProperties {

    private String baseUrl;
    private String apiKey;
    private String modelName;
    private Double topP;
    private Integer topK;
    private Boolean enableSearch;
    private Integer seed;
    private Float repetitionPenalty;
    private Float temperature;
    private List<String> stops;
    private Integer maxTokens;
}