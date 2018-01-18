package com.openmdmremote.nativ.binary;

import android.content.Context;
import android.util.Log;

import com.openmdmremote.nativ.BackendStateNotifier;
import com.openmdmremote.nativ.authentication.AuthCheckListener;
import com.openmdmremote.nativ.authentication.AuthenticationService;

public class BinaryManager {
    private final int RETRIES = 5;
    private final Binary binary;
    private AuthenticationService authenticationService;
    private BackendStateNotifier backendStateNotifier;
    AuthenticationService authService;
    private int reRunTimes = RETRIES;
    Context context;
    public BinaryManager(Context context, AuthenticationService authenticationService) {
        binary = new Binary(context);
        this.context=context;
        this.authenticationService = authenticationService;
        this.authenticationService.addAuthCheckListener(new AuthCheckListener() {
            @Override
            public void onAuthenticationComplete(boolean success) {
                if(!success) {
                    reRunBackend();
                } else {
                    resetRunTimes();
                }
            }
        });
    }

    public BinaryManager(Context context, AuthenticationService authService, BackendStateNotifier backendStateNotifier) {
        this(context, authService);
        this.backendStateNotifier = backendStateNotifier;
    }

    private synchronized void reRunBackend() {
        if (reRunTimes > 0) {
            if (backendStateNotifier != null) {
                backendStateNotifier.notifyKey(authenticationService.getSessionKey());
            }
            Log.i("BinaryManager","RErunback");
            binary.runBackend(authenticationService.getSessionKey());
            reRunTimes--;
        }
    }


    private void resetRunTimes() {
        reRunTimes = RETRIES;
    }

    public synchronized void runBackend() {
        if(!binary.isRunning()) {
            Log.i("BinaryManager","runback");
            authenticationService.renewKeys();
            if (backendStateNotifier != null) {
                backendStateNotifier.notifyKey(authenticationService.getSessionKey());
            }
            authService = new AuthenticationService(context);

            binary.runBackend(authenticationService.getSessionKey());
        }else{
            authService = new AuthenticationService(context);
            //Log.i("BinaryManager","RErunback");

            reRunBackend(); //
        }
    }
}
