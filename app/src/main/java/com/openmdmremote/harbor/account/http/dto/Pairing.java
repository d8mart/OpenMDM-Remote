package com.openmdmremote.harbor.account.http.dto;

import org.json.JSONException;
import org.json.JSONObject;

public class Pairing {
    public class Request {
        private final String FIELD_PIN = "pin";
        private final String FIELD_DEVICENICK = "devicenick";
        private final String FIELD_DEVICETYPE = "devicetype";
        private final String FIELD_ANDROIDID = "androidid";

        JSONObject obj = new JSONObject();

        public Request(String devicenick, String type, String androidid, String pin) throws JSONException {
            obj.put(FIELD_DEVICENICK, devicenick);
            obj.put(FIELD_DEVICETYPE, type);
            obj.put(FIELD_ANDROIDID, androidid);
            obj.put(FIELD_PIN, pin);
        }

        public String toString() {
            return obj.toString();
        }
    }

    public class Response {
        public String Uuid;
        public String Pwd;
    }

    public class ErrorResponse {
        public int ErrorCode;
        public String ErrorMessage;
    }
}
