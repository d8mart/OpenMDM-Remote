package com.openmdmremote.harbor.account;

import android.content.Context;

import com.openmdmremote.WebkeyApplication;
import com.openmdmremote.harbor.account.http.CallLogIn;
import com.openmdmremote.harbor.settings.HarborAuthSettings;

public class LogIn implements CallLogIn.OnResultListener {

    private final HarborAuthSettings harborAuthSettings;
    private final CallLogIn callLogIn;
    private String nick;
    private LogInListener logInListener;

    public LogIn(Context context) {
        callLogIn = new CallLogIn(context);
        harborAuthSettings = new HarborAuthSettings(context);
    }

    public void doLogIn(LogInListener logInListener, String nick, String password) {
        WebkeyApplication.log("LogIn", "Try login");
        this.logInListener = logInListener;
        this.nick = nick;
        callLogIn.sendCredentials(this, nick, password);
    }

    private void saveAccountCredentials() {
        harborAuthSettings.setAccountName(nick);
    }

    @Override
    public void onWrongPwd(String response) {
        WebkeyApplication.log("LogIn", "Login failed: " + response);
        logInListener.onWrongPwd();
    }

    @Override
    public void onOtherError(String error) {
        WebkeyApplication.log("LogIn", "Http error result: " + error);
        logInListener.onOtherError();
    }

    @Override
    public void onSuccess() {
        WebkeyApplication.log("LogIn", "Login success!");
        saveAccountCredentials();
        logInListener.onSuccess();
    }

    public interface LogInListener {
        void onWrongPwd();
        void onOtherError();
        void onSuccess();
    }
}
