package com.openmdmremote.harbor.account;

import android.content.Context;

import com.openmdmremote.WebkeyApplication;
import com.openmdmremote.harbor.account.http.CallSignUp;
import com.openmdmremote.harbor.settings.HarborAuthSettings;

public class SignUp implements CallSignUp.OnResultListener {

    private final HarborAuthSettings harborHarborAuthSettings;
    private final CallSignUp callSignUp;

    private String nick;
    private SignUpListener sigUpListener;

    public SignUp(Context context) {
        callSignUp = new CallSignUp(context);
        harborHarborAuthSettings = new HarborAuthSettings(context);
    }

    public void doSignUp(SignUpListener signUpListener, String nick, String password) {
        WebkeyApplication.log("SignUp", "Try signup");
        this.sigUpListener = signUpListener;
        this.nick = nick;
        callSignUp.sendCredentials(this, nick, password);
    }

    private void saveAccountCredentials() {
        harborHarborAuthSettings.setAccountName(nick);
    }

    @Override
    public void onNicInUsed(String response) {
        WebkeyApplication.log("SignUp", "Registration failed: " + response);
        sigUpListener.onNicInUsed();
    }

    @Override
    public void onOtherError(String error) {
        WebkeyApplication.log("SignUp", "Json parse exception: " + error);
        sigUpListener.onOtherError();
    }

    @Override
    public void onSuccess() {
        WebkeyApplication.log("SignUp", "Registration success!");
        saveAccountCredentials();
        sigUpListener.onSuccess();
    }

    public interface SignUpListener {
        void onNicInUsed();
        void onOtherError();
        void onSuccess();
    }
}
