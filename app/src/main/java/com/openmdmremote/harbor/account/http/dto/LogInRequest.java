package com.openmdmremote.harbor.account.http.dto;

import org.json.JSONException;
import org.json.JSONObject;

public class LogInRequest {
    private final String FIELD_NICK = "nick";
    private final String FIELD_PWD = "pwd";

    JSONObject obj = new JSONObject();

    public LogInRequest(String email, String pwd) throws JSONException {
        obj.put(FIELD_NICK, email);
        obj.put(FIELD_PWD, pwd);
    }

    public String toString() {
        return obj.toString();
    }
}