package com.openmdmremote.harbor.account.http.dto;

import org.json.JSONException;
import org.json.JSONObject;

public class DeviceRegRequest {
    private final String FIELD_DEVICENAME = "devicename";
    private final String FIELD_DEVICETYPE = "devicetype";
    private final String FIELD_ANDROIDID = "androidid";

    JSONObject obj = new JSONObject();

    public DeviceRegRequest(String devicenick, String type, String androidid) throws JSONException {
        obj.put(FIELD_DEVICENAME, devicenick);
        obj.put(FIELD_DEVICETYPE, type);
        obj.put(FIELD_ANDROIDID, androidid);
    }

    public String toString() {
        return obj.toString();
    }
}
