package com.openmdmremote.harbor.settings;

import android.content.Context;

import com.openmdmremote.harbor.client.AuthCredentials;

public class HarborAuthSettings extends HarborAuthSettingsHelper {

    public HarborAuthSettings(Context context) {
        super(context);
    }

    @Override
    protected void onUpdate() {
        super.onUpdate();
        String REGISTERED_TO_HARBOR = "registered";
        preferences.edit().remove(REGISTERED_TO_HARBOR).apply();
    }

    private String getDeviceToken() {
        return preferences.getString(DEVICE_TOKEN, "");
    }

    public boolean isRegisteredToHarbor() {
        return preferences.contains(DEVICE_TOKEN);
    }

    public String getDeviceNickName() {
        return preferences.getString(NICKNAME, "").toLowerCase();
    }

    public void setDeviceNickName(String nickname) {
        preferences.edit().putString(NICKNAME, nickname).apply();
    }

    public void setDeviceToken(String token) {
        preferences.edit().putString(DEVICE_TOKEN, token).apply();
    }

    public String getAccountName() {
        return preferences.getString(ACCOUNT_NICKNAME, "");
    }

    public void setAccountName(String nick) {
        preferences.edit().putString(ACCOUNT_NICKNAME, nick).apply();
    }

    public void signUpRemoteUser(String pwd) {
        preferences.edit().putString(REMOTE_USER_PWD, pwd).apply();
        setRemoteAccess(true);
    }

    public void setRemoteAccess(boolean remoteAccess) {
        preferences.edit().putBoolean(REMOTE_USER_STATE, remoteAccess).apply();
    }

    public boolean isRemoteAccessEnabled() {
        return preferences.getBoolean(REMOTE_USER_STATE, true);
    }

    public void eraseCredentials() {
        preferences.edit().remove(DEVICE_TOKEN).apply();
        preferences.edit().remove(ACCOUNT_NICKNAME).apply();
        preferences.edit().remove(REMOTE_USER_STATE).apply();
    }

    public AuthCredentials getAuthCredentials() {
        if(getDeviceToken().equals("")) {
            return new AuthCredentials();
        } else {
            return new AuthCredentials(getDeviceToken());
        }
    }
}
