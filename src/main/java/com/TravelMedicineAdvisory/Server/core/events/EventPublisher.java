package com.TravelMedicineAdvisory.Server.core.events;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class EventPublisher {

    private final ApplicationEventPublisher publisher;

    public EventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void emit(String eventName, Object data) {
        publisher.publishEvent(new GenericEvent(this, eventName, data));
    }
}
