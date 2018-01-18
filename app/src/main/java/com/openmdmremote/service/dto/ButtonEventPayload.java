package com.openmdmremote.service.dto;

public class ButtonEventPayload {
    public enum EventType {
        DOWN,UP,LONGPRESS
    }

    public int code;
    public EventType type;

    @Override
    public String toString() {
        return "event: " + type + " code(i): " + code;
    }
}
