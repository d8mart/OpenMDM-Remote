package com.openmdmremote.harbor.account;

import android.content.Context;
import android.os.Build;

import com.openmdmremote.WebkeyApplication;
import com.openmdmremote.harbor.account.http.CallDeviceReg;
import com.openmdmremote.harbor.settings.HarborAuthSettings;

public class DeviceReg implements CallDeviceReg.OnResultListener {

    private final HarborAuthSettings harborAuthSettings;
    private final Context context;
    private final CallDeviceReg callDeviceReg;
    private String deviceNick;
    private DeviceRegListener deviceRegListener;

    public DeviceReg(Context context) {
        this.context = context;
        callDeviceReg = new CallDeviceReg(context);
        harborAuthSettings = new HarborAuthSettings(context);
    }

    public void doDeviceReg(DeviceRegListener deviceRegListener, String deviceName) {
        WebkeyApplication.log("DevReg", "Reg device...");
        this.deviceRegListener = deviceRegListener;
        this.deviceNick = deviceName;
        callDeviceReg.sendCredentials(this, deviceName, getDeviceType(), getAndroidId());
    }

    private String getDeviceType() {
        return Build.BRAND + "," +
                Build.MODEL;
    }

    private String getAndroidId() {
        return android.provider.Settings.Secure.getString(context.getContentResolver(),
                android.provider.Settings.Secure.ANDROID_ID);
    }

    @Override
    public void onNicInUsed(String response) {
        WebkeyApplication.log("DevReg", "Nick in used: " + response);
        deviceRegListener.onNicInUsed();
    }

    @Override
    public void onOtherError(String error) {
        WebkeyApplication.log("DevReg", "HttpError: " + error);
        deviceRegListener.onOtherError();
    }

    @Override
    public void onSuccess(String uuid, String remotePWD) {
        harborAuthSettings.setDeviceToken(uuid);
        harborAuthSettings.setDeviceNickName(deviceNick);
        harborAuthSettings.signUpRemoteUser(remotePWD);
        deviceRegListener.onSuccess();
    }

    public interface DeviceRegListener {
        void onNicInUsed();
        void onOtherError();
        void onSuccess();
    }
}
