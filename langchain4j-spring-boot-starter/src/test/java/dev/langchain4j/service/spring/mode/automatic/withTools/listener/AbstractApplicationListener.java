package dev.langchain4j.service.spring.mode.automatic.withTools.listener;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import java.util.ArrayList;
import java.util.List;

public class AbstractApplicationListener<E extends ApplicationEvent> implements ApplicationListener<E> {
    private final List<E> receivedEvents = new ArrayList<>();

    @Override
    public void onApplicationEvent(E event) {
        receivedEvents.add(event);
    }

    public List<E> getReceivedEvents() {
        return receivedEvents;
    }

    public boolean isEventReceived() {
        return !receivedEvents.isEmpty();
    }
}