package com.openmdmremote.service.dto;

public class ToastPayload extends Payload{

    public enum ToastType {
        INFO, WARNING, ERROR
    }

    ToastType type;
    String text;
    Boolean sticky;

    public ToastPayload(ToastType type, String text, Boolean sticky) {
        this.type = type;
        this.text = text;
        this.sticky = sticky;
    }
}
