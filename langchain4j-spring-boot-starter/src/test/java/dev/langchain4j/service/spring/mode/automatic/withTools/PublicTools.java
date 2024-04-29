package dev.langchain4j.service.spring.mode.automatic.withTools;

import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class PublicTools {

    public static final LocalDate CURRENT_DATE = LocalDate.of(2024, 4, 29);

    @Tool
    public LocalDate getCurrentDate() {
        return CURRENT_DATE;
    }
}
