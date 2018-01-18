package com.openmdmremote.service.dto;

// Message base to be sent over websocket
public class Message {
    public enum Type {
        AUTH,
        ADMINAUTH,
        SIGNUP,
        SCREEN_START,
        SCREEN_STOP,
        SCREEN_OPTIONS,
        SCREEN_ACK,
        ERROR,
        BACKENDHALTED,
        SESSIONKEY,
        PING,
        TOUCH,
        KEY,
        BUTTON,
        LOCATION_START,
        LOCATION_STOP,
        LOCATION,
        TOAST,
        RESTART,
        OPENURL
    }

    public String id;
    public Type type;

    public String payload;
    public Payload jsonPayload;

    
    @Override
    public String toString() {
    	return id + " - " + type + " - " + payload;
    }


	public Message(String id, Type type, String payload) {
		super();
		this.id = id;
		this.type = type;
		this.payload = payload;
	}


    public Message(String id, Type type, Payload payload) {
        super();
        this.id = id;
        this.type = type;
        this.jsonPayload = payload;
    }
}
