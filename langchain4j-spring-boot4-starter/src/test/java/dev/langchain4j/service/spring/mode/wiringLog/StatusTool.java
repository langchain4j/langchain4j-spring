package dev.langchain4j.service.spring.mode.wiringLog;

import dev.langchain4j.agent.tool.Tool;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Lazy
@Component(StatusTool.BEAN_NAME)
class StatusTool {

    static final String BEAN_NAME = "statusTool";

    private final AtomicInteger executions = new AtomicInteger();

    StatusTool() {
        WiringLogApplication.recordInitialization(BEAN_NAME);
    }

    @Tool("Returns the current status")
    String currentStatus() {
        return "all systems go #" + executions.incrementAndGet();
    }

    int executions() {
        return executions.get();
    }
}
