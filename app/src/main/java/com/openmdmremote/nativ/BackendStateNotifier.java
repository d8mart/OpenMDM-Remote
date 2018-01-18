package com.openmdmremote.nativ;

import android.util.Log;

import java.util.LinkedHashSet;
import java.util.Set;

public class BackendStateNotifier {

    Set<BackendStateListener> bsListeners = new LinkedHashSet<>();
    Set<BackendHaltListener> haltListeners = new LinkedHashSet<>();

    private final Object listenerLock = new Object();

    public void addListener(BackendStateListener b) {
        synchronized (listenerLock) {
            bsListeners.add(b);
        }
    }

    public void remove(BackendStateListener backendStateListener) {
        synchronized (listenerLock) {
            bsListeners.remove(backendStateListener);
        }
    }

    public void addHaltListener(BackendHaltListener h) {
        synchronized (listenerLock) {
            haltListeners.add(h);
        }
    }

    public void removeHaltListener(BackendHaltListener h) {
        synchronized (listenerLock) {
            haltListeners.remove(h);
        }
    }


    public void notifyReady() {
        synchronized (listenerLock) {
            for (BackendStateListener b : bsListeners) {
                b.backendReady();
            }
        }
    }

    public void notifyKey(String sessionKey) {
        synchronized (listenerLock) {
            for (BackendStateListener b : bsListeners) {
                b.sessionKey(sessionKey);
            }
        }
    }

    public void notifySessionKeyError() {
        synchronized (listenerLock) {
            for (BackendStateListener b : bsListeners) {
                b.sessionKeyError("session key error");
            }
        }
    }

    public void notifyDisconnected() {
        Log.i("BackendStateNotifier","notifyDisconnected");
        synchronized (listenerLock) {
            for (BackendStateListener b : bsListeners) {
                b.backendHalted();
            }

            for (BackendHaltListener b : haltListeners) {
                b.backendHalted();
            }
        }
    }
}
