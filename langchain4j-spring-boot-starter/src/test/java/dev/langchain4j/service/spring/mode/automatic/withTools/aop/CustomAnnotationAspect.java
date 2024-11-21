package dev.langchain4j.service.spring.mode.automatic.withTools.aop;

import dev.langchain4j.agent.tool.Tool;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Aspect
@Component
public class CustomAnnotationAspect {

    private boolean aspectEnabled = false;

    private final List<String> toolsDescription = new ArrayList<>();

    @Around("@annotation(customAnnotation)")
    public Object around(ProceedingJoinPoint joinPoint, CustomAnnotation customAnnotation) throws Throwable {
        aspectEnabled = true;
        var signature = (MethodSignature) joinPoint.getSignature();
        var method = signature.getMethod();
        if (method.isAnnotationPresent(Tool.class)) {
            Tool toolAnnotation = method.getAnnotation(Tool.class);
            toolsDescription.addAll(Arrays.asList(toolAnnotation.value()));
            System.out.printf("Found @Tool %s for method: %s\n", Arrays.toString(toolAnnotation.value()), method);
            System.out.println();
        }
        String customKey = customAnnotation.customKey();
        Object result = joinPoint.proceed();
        System.out.printf("Custom key: %s | Method name: %s | Method arguments: %s | Return type: %s | Method return value: %s%n",
                customKey,
                method.getName(),
                Arrays.toString(joinPoint.getArgs()),
                method.getReturnType().getName(),
                result);
        return result;
    }

    public boolean isAspectEnabled() {
        return aspectEnabled;
    }

    public List<String> getToolsDescription() {
        return toolsDescription;
    }
}
