package dev.langchain4j.spring.it;

import dev.langchain4j.agent.tool.Tool;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A blocking tool that records the MDC value it sees, to check whether context set by the caller reaches the thread
 * the tool runs on.
 */
@Component
class CapitalTool {

    static final String MDC_KEY = "requestId";

    final List<String> requestIdsSeen = new CopyOnWriteArrayList<>();

    @Tool("Returns the capital of Germany")
    String capitalOfGermany() {
        requestIdsSeen.add(String.valueOf(MDC.get(MDC_KEY)));
        return "Berlin";
    }
}
