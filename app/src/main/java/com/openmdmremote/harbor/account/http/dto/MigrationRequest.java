package com.openmdmremote.harbor.account.http.dto;

import org.json.JSONException;
import org.json.JSONObject;

public class MigrationRequest {
    private final String FIELD_DEVICEID = "deviceid";

    JSONObject obj = new JSONObject();

    public MigrationRequest(String deviceID) throws JSONException {
        obj.put(FIELD_DEVICEID, deviceID);
    }

    public String toString() {
        return obj.toString();
    }
}
