package com.openmdmremote.harbor.account.http.dto;

import org.json.JSONException;
import org.json.JSONObject;

public class ForgotPasswordRequest {
    private final String FIELD_ADDRESS = "address";

    JSONObject obj = new JSONObject();

    public ForgotPasswordRequest(String address) throws JSONException {
        obj.put(FIELD_ADDRESS, address);
    }

    public String toString() {
        return obj.toString();
    }
}
