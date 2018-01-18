package com.openmdmremote.nativ.handlers;

import android.content.Context;

import com.openmdmremote.nativ.BackendStateListener;
import com.openmdmremote.nativ.WIPC;
import com.openmdmremote.net.visitor.WebkeyVisitor;
import com.openmdmremote.service.dto.Message;
import com.openmdmremote.service.dto.ToastPayload;
import com.openmdmremote.service.handlers.interfaces.INotifiactions;

public class Notifications implements INotifiactions, BackendStateListener {
    private final String LOGTAG = "handler Notifications";

    WebkeyVisitor mWebkeyVisitor;
    private WIPC wipc;

    public Notifications(Context context, WebkeyVisitor webkeyVisitor) {
        mWebkeyVisitor = webkeyVisitor;
        wipc = WIPC.getInstance(context);
        wipc.addListener(this);

        // With this line implicit push the session key if necessary.
        wipc.connect();
    }

    @Override
    public void leftUser() {
        wipc.removeListener(this);
    }

    @Override
    public void backendReady() {

    }

    @Override
    public void backendHalted() {
        mWebkeyVisitor.sendGson(new Message("1", Message.Type.BACKENDHALTED, ""));
    }

    @Override
    public void sessionKeyError(String msg) {
        mWebkeyVisitor.sendToast(ToastPayload.ToastType.ERROR, "Backend start failed: " + msg, false);
    }

    @Override
    public void sessionKey(String ipcSessionKey) {
        mWebkeyVisitor.sendGson(new Message("1", Message.Type.SESSIONKEY, ipcSessionKey));
    }

    @Override
    public void backendUpdate() {
    }
}
