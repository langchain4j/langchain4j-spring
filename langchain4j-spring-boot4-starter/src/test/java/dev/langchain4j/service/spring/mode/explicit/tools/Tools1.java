package dev.langchain4j.service.spring.mode.explicit.tools;

import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

@Component
class Tools1 {

    public static final String TOOL_1_TEMPERATURE = "6";

    @Tool
    String getCurrentTemperature() {
        return TOOL_1_TEMPERATURE;
    }
}
