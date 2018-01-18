package com.openmdmremote.nativ.authentication;

import android.content.Context;

import com.openmdmremote.service.services.Settings;

import java.util.Random;

public class AuthenticationService {
    private static final String LOGTAG = "Backend AuthenticationService";

    Notifier notifier = new Notifier();

    private String javaKey;
    private String backendKey;
    private static final int KEYLEN = 5;

    private Settings settings;

    public AuthenticationService(Context context) {
        settings = new Settings(context);
        if(!settings.isSessionKeyInited()) {
            generateNewKeys();
        } else {
            loadKeys();
        }
    }

    private void loadKeys() {
        javaKey = settings.getSessionKey();
        backendKey = settings.getBackendKey();
    }

    private void generateNewKeys() {
        javaKey = generateNewKey();
        backendKey = generateNewKey();

        settings.setSessionKey(javaKey);
        settings.setBackendKey(backendKey);
    }

    private String generateNewKey() {
       /* final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuwxyz";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder(KEYLEN);
        for (int i = 0; i < KEYLEN; i++) {
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        }
        return sb.toString();*/

        return "openm";
    }

    public void renewKeys() {
        generateNewKeys();
    }

    public String getSessionKey() {
        return javaKey + backendKey;
    }

    public String getBackendKey() {
        return backendKey;
    }

    public boolean checkJavaKey(String receivedKey) {
        if (javaKey.equals(receivedKey)) {
            notifier.notifyAuthenticationComplete(true);
            return true;
        } else {
            generateNewKeys();
            notifier.notifyAuthenticationComplete(false);
            return false;
        }
    }

    public void addAuthCheckListener(AuthCheckListener authCheckListener) {
        notifier.addListener(authCheckListener);
    }
}
