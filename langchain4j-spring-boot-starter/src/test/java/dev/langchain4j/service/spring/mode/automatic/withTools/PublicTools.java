package dev.langchain4j.service.spring.mode.automatic.withTools;

import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

@Component
public class PublicTools {

    static int CURRENT_TEMPERATURE = 42;

    @Tool
    public int getCurrentTemperature() {
        return CURRENT_TEMPERATURE;
    }
}
