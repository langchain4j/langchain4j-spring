package dev.langchain4j.service.spring.mode.explicit.tools;

import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

@Component
class Tools2 {

    public static final String TOOL_2_TEMPERATURE = "9";

    @Tool
    String getCurrentTemperature() {
        return TOOL_2_TEMPERATURE;
    }
}
