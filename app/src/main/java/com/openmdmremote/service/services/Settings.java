package com.openmdmremote.service.services;

import android.content.Context;

import com.openmdmremote.BuildConfig;

public class Settings extends SettingsHelper {

    public Settings (Context context){
        super(context);
    }

    @Override
    protected void onUpdate() {
        super.onUpdate();
        String HARBOR_PORT = "harborserverport";
        String HARBOR_SECURE = "harborsecure";
        String HARBOR_ADDRESS = "harboraddress";
        String NEWNICK = "newnick";

        preferences.edit().remove(HARBOR_SECURE).commit();

        preferences.edit().remove(NEWNICK);
        preferences.edit().remove(HARBOR_PORT).commit();
        preferences.edit().remove(HARBOR_ADDRESS).commit();
    }

    public boolean getAutostart() {
        return preferences.getBoolean(AUTOSTART, true);
    }

    public int getHttpPort() {
        return preferences.getInt(LOCAL_HTTP_PORT, 8080);
    }

    public void setHttpPort(int localHttpPort) {
        preferences.edit().putInt(LOCAL_HTTP_PORT, localHttpPort).apply();
    }

    public int getWSport() {
        return preferences.getInt(LOCAL_WS_PORT, 8081);
    }

    public void setWSPort(int localWSPort) {
        preferences.edit().putInt(LOCAL_WS_PORT, localWSPort).apply();
    }

    public boolean getFirstStart() {
        return preferences.getBoolean(FIRST_START, true);
    }

    public void setFirstStart(boolean firstStart) {
        preferences.edit().putBoolean(FIRST_START, firstStart).commit();
    }

    public int getSavedVersionCode() {
        return preferences.getInt(VERSION_CODE, 0);
    }

    public void updatePackageVersionCode(int vcode) {
        preferences.edit().putInt(VERSION_CODE, vcode).commit();
    }

    // Backend security.
    public boolean isSessionKeyInited() {
        if(getSessionKey().equals("budaf") || getBackendKey().equals("budaf")) {
            return false;
        } else {
            return true;
        }
    }

    public String getSessionKey() {
        return preferences.getString(SESSION_KEY, "budaf");
    }

    public void setSessionKey(String sessionKey) {
        preferences.edit().putString(SESSION_KEY, sessionKey).commit();
    }

    public String getBackendKey() {
        return preferences.getString(BACKEND_KEY, "budaf");
    }

    public void setBackendKey(String backendKey) {
        preferences.edit().putString(BACKEND_KEY, backendKey).commit();
    }

    public void setStarted(boolean started) {
        preferences.edit().putBoolean(STARTED, started).commit();
    }

    public boolean getStarted() {
        return preferences.getBoolean(STARTED, false);
    }

    // Logentries remote logging service
    public boolean getRemoteLogging(){
        return preferences.getBoolean(REMOTELOGGING, false);
    }

    // Just for migration.
    public String getDeviceToken() {
        return preferences.getString(DEVICE_TOKEN, "");
    }

    public String getDeviceNick() {
        return preferences.getString(NICKNAME, "");
    }

    public boolean hasOldReg() {
        return preferences.contains(DEVICE_TOKEN);
    }

    public void removeDevicetokenAndNick() {
        preferences.edit().remove(DEVICE_TOKEN).commit();
        preferences.edit().remove(NICKNAME).commit();
    }

    public boolean isFleeted() {
        if(BuildConfig.FLEED_ID != null) {
            return true;
        } else {
            return false;
        }
    }
}
