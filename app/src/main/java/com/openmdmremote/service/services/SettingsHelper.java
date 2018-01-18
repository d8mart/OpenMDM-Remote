package com.openmdmremote.service.services;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingsHelper {

    private final String PREFERENCE = "WEBKEY";
    private final int SETTINGS_VERSION = 1;
    private final String SETTINGS_VERSION_KEY = "settings_version";

    protected static final String AUTOSTART = "autostart";
    protected static final String FIRST_START = "firststart";
    protected static final String SESSION_KEY = "sessionkey";
    protected static final String BACKEND_KEY = "backendkey";
    protected static final String VERSION_CODE = "versioncode";

    public static final String LOCAL_HTTP_PORT = "localhttpport";
    public static final String LOCAL_WS_PORT = "localwsport";
    public static final String REMOTELOGGING = "remotelogging";

    // Removed keys.
    public static final String DEVICE_TOKEN = "userid";
    public static final String NICKNAME = "nickname";

    protected static final String STARTED = "started"; // Changed when service started.

    protected SharedPreferences preferences;

    public SettingsHelper(Context context) {
        preferences = context.getSharedPreferences(PREFERENCE, Context.MODE_PRIVATE);
        // This check care for shared preferences only
        if (checkUpdate()) {
            onUpdate();
        }
    }

    // Return true if should update.
    private boolean checkUpdate() {
        if (!preferences.contains(SETTINGS_VERSION_KEY)) {
            return false;
        }

        int storedVersion = preferences.getInt(SETTINGS_VERSION_KEY, 0);
        return storedVersion < SETTINGS_VERSION;
    }

    protected void onUpdate() {
        preferences.edit().putInt(SETTINGS_VERSION_KEY, SETTINGS_VERSION).apply();
    }
}
