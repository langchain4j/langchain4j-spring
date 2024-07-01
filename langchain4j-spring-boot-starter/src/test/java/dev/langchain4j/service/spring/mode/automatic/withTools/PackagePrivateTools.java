package dev.langchain4j.service.spring.mode.automatic.withTools;

import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component
class PackagePrivateTools {

    static final LocalTime CURRENT_TIME = LocalTime.of(17, 49);

    @Tool
    String getCurrentTime() {
        return CURRENT_TIME.toString();
    }
}
