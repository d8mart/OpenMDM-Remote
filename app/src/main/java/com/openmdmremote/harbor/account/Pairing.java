package com.openmdmremote.harbor.account;

import android.content.Context;

import com.openmdmremote.WebkeyApplication;
import com.openmdmremote.harbor.account.http.CallPairing;
import com.openmdmremote.harbor.settings.HarborAuthSettings;

public class Pairing implements CallPairing.OnResultListener {
    private final String LOGTAG = "Pairing";

    private final HarborAuthSettings harborAuthSettings;
    private final CallPairing callPairing;
    private PairingListener pairingListener;

    private DeviceInfo deviceInfo;
    private String nick;
    private String pin;

    private final int MAX_RETRY = 4;
    private int newNickRetry = 0;

    public Pairing(Context context) {
        callPairing = new CallPairing(context);
        harborAuthSettings = new HarborAuthSettings(context);
        deviceInfo = new DeviceInfo(context);
    }

    public void pairing(Pairing.PairingListener pairingListener, String pin) {
        this.pairingListener = pairingListener;
        this.pin = pin;
        nick = deviceInfo.generateNick();
        resetPairingRetry();
        pairing();
    }

    private void resetPairingRetry() {
        newNickRetry = 0;
    }

    private void pairing() {
        WebkeyApplication.log(LOGTAG, "Pairing device");
        callPairing.pairing(this, nick, deviceInfo.getDeviceType(), deviceInfo.getAndroidId(), pin);
    }

    @Override
    public void onError(String error) {
        WebkeyApplication.log(LOGTAG, "Http response error: " + error);
        pairingListener.onError();
    }

    @Override
    public void onSuccess(String uuid, String remotePWD) {
        harborAuthSettings.setDeviceToken(uuid);
        harborAuthSettings.setDeviceNickName(nick);
        harborAuthSettings.signUpRemoteUser(remotePWD);
        pairingListener.onSuccess();
    }

    @Override
    public void onWrongPINCode(String message) {
        WebkeyApplication.log(LOGTAG, message);
        pairingListener.onWrongCode();
        return;
    }

    @Override
    public void onNicInUsed(String message) {
        if(newNickRetry >= MAX_RETRY) {
            WebkeyApplication.log(LOGTAG, message);
            pairingListener.onError();
            return;
        }

        newNickRetry+=1;

        nick = deviceInfo.generateRandomNick();
        pairing();
    }

    public interface PairingListener {
        void onError();
        void onWrongCode();
        void onSuccess();
    }
}
