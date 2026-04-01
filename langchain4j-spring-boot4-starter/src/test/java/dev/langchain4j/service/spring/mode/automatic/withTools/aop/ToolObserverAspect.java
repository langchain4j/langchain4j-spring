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
public class ToolObserverAspect {

    private final List<String> observedTools = new ArrayList<>();

    @Around("@annotation(toolObserver)")
    public Object around(ProceedingJoinPoint joinPoint, ToolObserver toolObserver) throws Throwable {
        var signature = (MethodSignature) joinPoint.getSignature();
        var method = signature.getMethod();
        String methodName = method.getName();
        if (method.isAnnotationPresent(Tool.class)) {
            Tool toolAnnotation = method.getAnnotation(Tool.class);
            observedTools.addAll(Arrays.asList(toolAnnotation.value()));
            System.out.printf("Found @Tool %s for method: %s%n%n", Arrays.toString(toolAnnotation.value()), methodName);
        }
        Object result = joinPoint.proceed();
        System.out.printf(" | key: %s%n | Method name: %s%n | Method arguments: %s%n | Return type: %s%n | Method return value: %s%n%n",
                toolObserver.key(),
                methodName,
                Arrays.toString(joinPoint.getArgs()),
                method.getReturnType().getName(),
                result);
        return result;
    }

    public boolean aspectHasBeenCalled() {
        return !observedTools.isEmpty();
    }

    public List<String> getObservedTools() {
        return observedTools;
    }
}
