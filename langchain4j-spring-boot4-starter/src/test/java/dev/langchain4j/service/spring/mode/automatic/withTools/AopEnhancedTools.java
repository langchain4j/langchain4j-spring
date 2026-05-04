package dev.langchain4j.service.spring.mode.automatic.withTools;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.service.spring.mode.automatic.withTools.aop.ToolObserver;
import org.springframework.stereotype.Component;

@Component
public class AopEnhancedTools {

    public static final String TOOL_OBSERVER_PACKAGE_NAME_DESCRIPTION =
            "Find the package directory where @ToolObserver is located.";
    public static final String TOOL_OBSERVER_PACKAGE_NAME = ToolObserver.class.getPackageName();

    public static final String TOOL_OBSERVER_KEY_NAME_DESCRIPTION =
            "Find the key name of @ToolObserver";
    public static final String TOOL_OBSERVER_KEY = "AOP_ENHANCED_TOOLS_SUPPORT_@_1122";

    @Tool(TOOL_OBSERVER_PACKAGE_NAME_DESCRIPTION)
    public String getToolObserverPackageName() {
        return TOOL_OBSERVER_PACKAGE_NAME;
    }

    @ToolObserver(key = TOOL_OBSERVER_KEY)
    @Tool(TOOL_OBSERVER_KEY_NAME_DESCRIPTION)
    public String getToolObserverKey() {
        return TOOL_OBSERVER_KEY;
    }
}
