package com.openmdmremote.service.dto;

public class KeyEventPayload {
    public enum EventType {
        PRESS,DOWN,UP
    }

    public EventType type;
    public String code;

    @Override
    public String toString() {
        return "event: " + type + " code(s): " + code;
    }
}
