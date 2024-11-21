package dev.langchain4j.service.spring.mode.automatic.withTools;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.service.spring.mode.automatic.withTools.aop.CustomAnnotation;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Component
public class AopEnhancedTools {

    public static final String ASPECT_PACKAGE = Aspect.class.getPackageName();

    public static final String TOOL_DESCRIPTION = "Find the package directory where @Aspect is located.";

    @Tool(TOOL_DESCRIPTION)
    @CustomAnnotation(customKey = "lock_key_1121")
    public String getAspectPackage() {
        return ASPECT_PACKAGE;
    }
}
