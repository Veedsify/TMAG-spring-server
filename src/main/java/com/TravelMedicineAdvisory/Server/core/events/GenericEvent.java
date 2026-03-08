package com.TravelMedicineAdvisory.Server.core.events;

import org.springframework.context.ApplicationEvent;

public class GenericEvent extends ApplicationEvent {
    private final String eventName;
    private final Object data;

    public GenericEvent(Object source, String eventName, Object data) {
        super(source);
        this.eventName = eventName;
        this.data = data;
    }

    public String getEventName() {
        return eventName;
    }

    public Object getData() {
        return data;
    }
}
