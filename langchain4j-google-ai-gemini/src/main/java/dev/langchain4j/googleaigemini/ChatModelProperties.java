package dev.langchain4j.googleaigemini;


import lombok.Getter;
import lombok.Setter;

import java.time.Duration;

@Getter
@Setter
public class ChatModelProperties {

    private String API_KEY;
    private String modelName;
    private double temperature;
    private double topP;
    private Integer topK;
    private Integer maxOutputTokens;

}
