package com.openmdmremote.harbor.settings;

import android.content.Context;
import android.content.SharedPreferences;

class HarborAuthSettingsHelper {

    private final String PREFERENCE = "harbor";
    private final int SETTINGS_VERSION = 1;
    private final String SETTINGS_VERSION_KEY = "settings_version_harbor";

    protected static final String ACCOUNT_NICKNAME = "accountnick";
    protected static final String DEVICE_TOKEN = "devicetoken";
    public static final String NICKNAME = "nickname";
    public static final String REMOTE_USER_PWD = "remoteuserpwd";
    public static final String REMOTE_USER_STATE = "remoteuserstate";

    protected SharedPreferences preferences;

    public HarborAuthSettingsHelper(Context context) {
        preferences = context.getSharedPreferences(PREFERENCE, Context.MODE_PRIVATE);
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
