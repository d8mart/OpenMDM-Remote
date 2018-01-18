package com.openmdmremote.harbor.settings;

import com.openmdmremote.BuildConfig;

public class HarborServerSettings {

    // HTTP API
    public static final String PATH_HARBORCONNECTION = "/openmdm-rs/remote"; //     /openmdm-rs/remote   /remote.openmdm/remote  /openmdm-remote/remote
    public static final String PATH_SIGNIN_ACCOUNT = "/_api/account";
    public static final String PATH_LOGIN_ACCOUNT = "/_api/account/login";
    public static final String PATH_DEVICE_REGISTRATION = "/_api/devices";
    public static final String PATH_DEVICE_MIGRATION = "/_api/devices/migration";
    public static final String PATH_DEVICE_PAIRING = "/_api/devices/pairing";
    public static final String PATH_FORGOT_PASSWORD = "/_api/account/password";

    // Log fender
    public static final String PATH_REMOTELOGGING = "/_logcat";
    public static final String ADDRESS_REMOTELOGGING = "logcat.webkey.cc";
    public static final int LOGCAT_PORT = 80;

    private static final String HARBOR_ADDRESS = "qbex.com"; //    qbex.com    192.168.0.4
    private static final boolean HARBOR_IS_SECURE = true;

    public HarborServerSettings() {
    }

    public String getHarborServerAddress() {
        if (BuildConfig.HARBOR_ADDRESS != null) {
            return BuildConfig.HARBOR_ADDRESS;
        } else {
            return HARBOR_ADDRESS;
        }
    }

    public int getHarborServerPort() {
        if (isSecure()) {
            return 8086; // 8086
        } else {
            return 8086;
        }
    }

    public boolean isSecure() {
        return HARBOR_IS_SECURE;
    }
}
