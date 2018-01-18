package com.openmdmremote.harbor.client;

public class AuthCredentials {
    private String deviceToken;

    public AuthCredentials(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public AuthCredentials() {
        this.deviceToken = null;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public String getSerial() {
        HardwareID hwID = new HardwareID();
        return hwID.getSerial();
    }

    public String getAccountID() {
        return com.openmdmremote.BuildConfig.FLEED_ID;
    }
}
