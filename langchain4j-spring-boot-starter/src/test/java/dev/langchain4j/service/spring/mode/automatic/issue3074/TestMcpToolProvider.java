package dev.langchain4j.service.spring.mode.automatic.issue3074;

import dev.langchain4j.service.tool.ToolProvider;
import dev.langchain4j.service.tool.ToolProviderRequest;
import dev.langchain4j.service.tool.ToolProviderResult;

public class TestMcpToolProvider implements ToolProvider {
    @Override
    public ToolProviderResult provideTools(ToolProviderRequest toolProviderRequest) {
        return null;
    }
}
