package com.openmdmremote.harbor.client.ConnectionStateNotifier;

import java.util.LinkedHashSet;
import java.util.Set;

public class ConnectionStateNotifier {
    private Set<OnHarborConnectionListener> listeners = new LinkedHashSet<>();
    private OnHarborConnectionListener.ConnectionStates lastConnState = OnHarborConnectionListener.ConnectionStates.DISCONNECTED;

    private final Object listenerLock = new Object();

    public void addHarborConnectionListener(OnHarborConnectionListener onHarborConnectionListener) {
        synchronized (listenerLock) {
            if (!listeners.contains(onHarborConnectionListener)) {
                onHarborConnectionListener.onHarborConnectionChanged(lastConnState);
                listeners.add(onHarborConnectionListener);
            }
        }
    }

    public void removeHarborConnectionListener(OnHarborConnectionListener onHarborConnectionListener) {
        synchronized (listenerLock) {
            listeners.remove(onHarborConnectionListener);
        }
    }

    private void notify(OnHarborConnectionListener.ConnectionStates state) {
        synchronized (listenerLock) {
            lastConnState = state;
            for (OnHarborConnectionListener l : listeners) {
                l.onHarborConnectionChanged(state);
            }
        }
    }

    public void notifyConnected() {
        notify(OnHarborConnectionListener.ConnectionStates.CONNECTED);
    }

    public void notifyDisconnected() {
        notify(OnHarborConnectionListener.ConnectionStates.DISCONNECTED);
    }

    public void notifyConnecting() {
        notify(OnHarborConnectionListener.ConnectionStates.CONNECTING);
    }
}
