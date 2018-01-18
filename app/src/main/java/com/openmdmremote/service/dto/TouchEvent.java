package com.openmdmremote.service.dto;

public class TouchEvent {
    public enum TouchEventType {
        UP, DOWN, MOVE
    }

    public TouchEventType type;
    public long timestamp;
    public double x;
    public double y;
    public boolean flip = false;
    public boolean mirror = false;

}
