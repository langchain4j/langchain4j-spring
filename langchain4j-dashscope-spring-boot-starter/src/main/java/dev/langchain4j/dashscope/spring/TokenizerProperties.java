package dev.langchain4j.dashscope.spring;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenizerProperties {
    private String apiKey;
    private String modelName;
}
