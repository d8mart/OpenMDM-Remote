package com.openmdmremote.nativ;

public interface BackendStateListener {
    void backendReady();
    void backendHalted();

    void sessionKeyError(String msg);
    void sessionKey(String ipcSessionKey);
    void backendUpdate();
}
