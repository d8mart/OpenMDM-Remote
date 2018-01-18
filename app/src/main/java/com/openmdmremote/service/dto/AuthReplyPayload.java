package com.openmdmremote.service.dto;

public class AuthReplyPayload extends Payload {
    boolean error = false;
    String deviceNick = "";
    String errorMsg = "";

    public AuthReplyPayload(boolean error, String deviceNickName, String errorMsg) {
        this.error = error;
        this.deviceNick = deviceNickName;
        this.errorMsg = errorMsg;
    }
}
