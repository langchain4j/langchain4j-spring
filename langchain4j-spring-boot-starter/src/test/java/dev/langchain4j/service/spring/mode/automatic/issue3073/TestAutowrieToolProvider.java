package dev.langchain4j.service.spring.mode.automatic.issue3073;

import dev.langchain4j.service.AiServiceContext;
import dev.langchain4j.service.spring.AiServicesAutoConfig;
import dev.langchain4j.service.tool.ToolProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

public class TestAutowrieToolProvider {

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AiServicesAutoConfig.class));

    @Test
    void should_fail_to_create_AI_service_when_conflicting_chat_models_are_found() {
        contextRunner
                .withUserConfiguration(TestAutowireAiServiceToolProviderApplication.class)
                .run(context -> {
                    ToolProvider toolProviderBean = context.getBean(ToolProvider.class);
                    Assertions.assertNotNull(toolProviderBean, "ToolProvider bean should be present in the context.");

                    AiServiceWithToolProvider aiServiceProxy = context.getBean(AiServiceWithToolProvider.class);
                    Assertions.assertNotNull(aiServiceProxy, "AiServiceWithToolProvider should be created successfully.");

                    InvocationHandler handler = Proxy.getInvocationHandler(aiServiceProxy);
                    Assertions.assertNotNull(handler, "InvocationHandler should be present.");
                    Field this$0 = handler.getClass().getDeclaredField("this$0");
                    this$0.setAccessible(true);
                    Object aiServices = this$0.get(handler);

                    Field contextField = aiServices.getClass().getSuperclass().getDeclaredField("context");
                    contextField.setAccessible(true);
                    AiServiceContext aiServiceContext = (AiServiceContext) contextField.get(aiServices);
                    Assertions.assertNotNull(aiServiceContext, "AiServiceContext should be present.");

                    Field aiServiceField = aiServiceContext.getClass().getDeclaredField("toolService");
                    aiServiceField.setAccessible(true);
                    Object toolService = aiServiceField.get(aiServiceContext);

                    Field toolProvider = toolService.getClass().getDeclaredField("toolProvider");
                    toolProvider.setAccessible(true);
                    Object toolProviderObj = toolProvider.get(toolService);
                    Assertions.assertInstanceOf(TestMcpToolProvider.class, toolProviderObj, "ToolProvider should be TestMcpToolProvider.");


                });
    }
}
