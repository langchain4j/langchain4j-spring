package dev.langchain4j.service.spring.mode.automatic.withToolProvider;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.service.tool.ToolExecutor;
import dev.langchain4j.service.tool.ToolProvider;
import dev.langchain4j.service.tool.ToolProviderResult;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;


@SpringBootApplication
class AiServiceWithToolProviderApplication {

    @Bean
    ToolProvider toolProvider() {
        return toolProviderRequest -> {
            ToolSpecification toolSpecification = ToolSpecification.builder()
                                                                   .name("getName")
                                                                   .description("get the name")
                                                                   .build();
            ToolExecutor toolExecutor = (toolExecutionRequest, memoryId) -> "I am Shrink";
            HashMap<ToolSpecification, ToolExecutor> toolSpecification2ToolExecutorMap = new HashMap<>();
            toolSpecification2ToolExecutorMap.put(toolSpecification, toolExecutor);
            return new ToolProviderResult(toolSpecification2ToolExecutorMap);
        };
    }

    public static void main(String[] args) {
        SpringApplication.run(AiServiceWithToolProviderApplication.class, args);
    }
}
