package com.openmdmremote.nativ.authentication;

import java.util.ArrayList;
import java.util.List;

public class Notifier {
    List<AuthCheckListener> authCheckListeners = new ArrayList<AuthCheckListener>();

    public void addListener(AuthCheckListener n) {
        authCheckListeners.add(n);
    }

    public void removeListener(AuthCheckListener authCheckListener) {
        authCheckListeners.remove(authCheckListener);
    }

    public void notifyAuthenticationComplete(boolean success) {
        for (AuthCheckListener a : authCheckListeners) {
            a.onAuthenticationComplete(success);
        }
    }
}
