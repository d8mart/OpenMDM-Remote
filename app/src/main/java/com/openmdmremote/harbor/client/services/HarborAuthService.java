package com.openmdmremote.harbor.client.services;

import android.content.Context;
import com.openmdmremote.WebkeyApplication;
import com.openmdmremote.harbor.HRPCProto;
import com.openmdmremote.harbor.client.AuthCredentials;
import com.openmdmremote.harbor.client.MessageBuilder;
import com.openmdmremote.harbor.client.handlers.HarborMessageHandler;
import com.openmdmremote.harbor.client.websocket.MyWebSocketClientNetty;
import com.openmdmremote.harbor.settings.HarborAuthSettings;

public class HarborAuthService implements HarborMessageHandler {
    private final String LOGTAG = "HarborAuthService";

    private final HarborAuthSettings harborAuthSettings;
    private MyWebSocketClientNetty webSocketClient;
    public  boolean loggedin = false;
    private OnHarborAuthListener authListener;

    public HarborAuthService(Context context) {
        harborAuthSettings = new HarborAuthSettings(context);
    }


    public void doAuth(MyWebSocketClientNetty webSocketClient, OnHarborAuthListener authListener) {
        this.webSocketClient = webSocketClient;
        this.authListener = authListener;

        if(hasRegistered()) {
            sendAuthRequest();
        } else {
            sendRegistrationRequest();
        }
    }

    private void sendAuthRequest() {
        AuthCredentials authCredentials = harborAuthSettings.getAuthCredentials();
        HRPCProto.Message msg = MessageBuilder.getAuthRequestMessage(authCredentials);
        //webSocketClient.writeAndFlush(msg);
    }

    private void sendRegistrationRequest() {
        AuthCredentials authCredentials = harborAuthSettings.getAuthCredentials();
        HRPCProto.Message msg = MessageBuilder.getRegistrationRequest(authCredentials);
        //webSocketClient.writeAndFlush(msg);
    }

    @Override
    public void onMessage(HRPCProto.Message msg) {
        if (!msg.getAuthresponse().getSuccess()) {
            loginFailed();
            WebkeyApplication.log(LOGTAG,"Authentication failed");
            return;
        }

        if (msg.getAuthresponse().hasToken()) {
            savecreds(msg.getAuthresponse());
            WebkeyApplication.log(LOGTAG,"Registration success");
        }

        loginSuccess();
        WebkeyApplication.log(LOGTAG,"Login success");

    }

    private void savecreds(HRPCProto.AuthResponse authresponse) {
        harborAuthSettings.setDeviceToken(authresponse.getToken());
        harborAuthSettings.signUpRemoteUser(authresponse.getRemotepwd());
    }

    @Override
    public void onClosed() {
        loggedin = false;
    }

    private void loginFailed() {
        WebkeyApplication.log(LOGTAG, "Logged in failed");
        loggedin = false;
        authListener.onAuthResult(false);
    }

    private void loginSuccess() {
        WebkeyApplication.log(LOGTAG, "Logged in to harbor");
        loggedin = true;
        authListener.onAuthResult(true);
    }

    private boolean hasRegistered() {
        if(harborAuthSettings.getAuthCredentials().getDeviceToken() == null) {
            return false;
        }

        if(harborAuthSettings.getAuthCredentials().getDeviceToken().equals("")) {
            return false;
        }

        return true;
    }

    public boolean isConnected() {
        return loggedin;
    }
}
