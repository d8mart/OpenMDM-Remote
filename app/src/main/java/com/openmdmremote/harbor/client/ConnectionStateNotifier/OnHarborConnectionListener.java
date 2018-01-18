package com.openmdmremote.harbor.client.ConnectionStateNotifier;

public interface OnHarborConnectionListener {
    enum ConnectionStates {
        DISCONNECTED, CONNECTING, CONNECTED
    }

    void onHarborConnectionChanged(ConnectionStates connectionStates);
}
