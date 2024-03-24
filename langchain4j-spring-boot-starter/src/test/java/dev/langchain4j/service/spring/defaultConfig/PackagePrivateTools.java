package dev.langchain4j.service.spring.defaultConfig;

import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component("packagePrivateTools")
class PackagePrivateTools {

    @Tool
    int getCurrentMinute() {
        return LocalDateTime.now().getMinute();
    }
}
