package com.openmdmremote.harbor.client.handlers;

import android.content.Context;

import com.openmdmremote.WebkeyApplication;
import com.openmdmremote.harbor.HRPCProto;
import com.openmdmremote.harbor.client.MessageBuilder;
import com.openmdmremote.harbor.interfaces.OnAdminAuthListener;
import com.openmdmremote.harbor.settings.HarborAuthSettings;
import com.openmdmremote.harbor.client.websocket.MyWebSocketClientNetty;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RemoteAuthService implements HarborMessageHandler {
    private final String LOGTAG = "RemoteAuthService";

    private final Map onAdminAuthListeners = new HashMap();
    private final MyWebSocketClientNetty websocketClient;
    private final Context context;

    public RemoteAuthService(Context context, MyWebSocketClientNetty webSocketClient) {
        this.websocketClient = webSocketClient;
        this.context = context;
    }

    @Override
    public void onMessage(HRPCProto.Message msg) {
        remoteAuthResult(msg.getRemoteauth().getAuthresponse());
    }

    @Override
    public void onClosed() {
        onAdminAuthListeners.clear();
    }

    public void remoteAuthRequest(String session, OnAdminAuthListener onAdminAuthListener) {
        if(!validateRequest()) {
            onAdminAuthListener.onAuthFailed();
            return;
        }

        int requestID = generateReuqestID();
        onAdminAuthListeners.put(requestID, onAdminAuthListener);
        sendAuthRequest(session, requestID);
    }

    private boolean validateRequest() {
        HarborAuthSettings harborAuthSettings = new HarborAuthSettings(context);

        if (!harborAuthSettings.isRegisteredToHarbor()) {
            return false;
        }

        if (!harborAuthSettings.isRemoteAccessEnabled()) {
            return false;
        }
        return true;
    }

    private int generateReuqestID() {
        return UUID.randomUUID().hashCode();
    }

    private void sendAuthRequest(String session, int requestID) {
        HRPCProto.Message msg = MessageBuilder.getRemoteAuthRequestMessage(session, requestID);
        //websocketClient.writeAndFlush(msg);
    }

    private void remoteAuthResult(HRPCProto.RemoteAuth.AuthResponse authResponse) {
        WebkeyApplication.log(LOGTAG, "Received remote auth response");
        int requestID = authResponse.getVisitorid();
        boolean success = authResponse.getSuccess();

        OnAdminAuthListener authHandler = (OnAdminAuthListener) onAdminAuthListeners.remove(requestID);

        if (authHandler != null) {
            if (success) {
                WebkeyApplication.log(LOGTAG, "remote auth login success");
                authHandler.onAuthSucess();
            } else {
                WebkeyApplication.log(LOGTAG, "remote auth login failed");
                authHandler.onAuthFailed();
            }
        }
    }
}
