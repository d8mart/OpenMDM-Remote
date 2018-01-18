package com.openmdmremote.nativ;

import android.content.Context;

import com.openmdmremote.nativ.authentication.AuthenticationService;
import com.openmdmremote.nativ.binary.BinaryManager;
import com.openmdmremote.nativ.net.MessageDispatcher;
import com.openmdmremote.nativ.net.NativeConnection;
import com.openmdmremote.nativ.net.WIPCProto;
import com.openmdmremote.nativ.net.exceptions.AlreadyConnectedException;
import com.openmdmremote.nativ.net.exceptions.ConnectionInProgressException;


public class WIPC {
    private static WIPC instance = null;

    private BackendStateNotifier backendStateNotifier = new BackendStateNotifier();
    private final AuthenticationService authService;
    private final BinaryManager binaryManager;
    private final NativeConnection nativeConnection;

    MessageDispatcher mMessageDispatcher;

    public WIPC(Context context) {
        authService = new AuthenticationService(context);
        binaryManager = new BinaryManager(context, authService, backendStateNotifier);
        nativeConnection = new NativeConnection(authService, backendStateNotifier);
        mMessageDispatcher = nativeConnection.getMessageDispatcher();
    }

    public static WIPC getInstance(Context c) {
        if (instance == null) {
            synchronized (WIPC.class) {
                if (instance == null) {
                    instance = new WIPC(c);
                }
            }
        }
        return instance;
    }


    public void addListener(final BackendStateListener b){
        if (nativeConnection.isReady()) {
            b.backendReady();
        }
        backendStateNotifier.addListener(b);
    }


    public void addHaltListener(final BackendHaltListener h) {
        backendStateNotifier.addHaltListener(h);
    }

    public void removeHaltListener(final BackendHaltListener h) {
        backendStateNotifier.removeHaltListener(h);
    }

    public void removeListener(BackendStateListener backendStateListener) {
        backendStateNotifier.remove(backendStateListener);
    }

    public synchronized void runBackend() {
        binaryManager.runBackend();
    }

    public void connect() {
        try {
            nativeConnection.connect();
        } catch (AlreadyConnectedException e) {
            backendStateNotifier.notifyReady();
        } catch (ConnectionInProgressException e) {
        }
        backendStateNotifier.notifyKey(authService.getSessionKey());
    }

    public void disconnect() {
        nativeConnection.disconnect();
    }

    public void teardown() {
        nativeConnection.backendHalt();
    }

    public byte[] takeImage() throws InterruptedException {
        return mMessageDispatcher.pollImage();
    }

    public void sendMsg(WIPCProto.Message msg) {
        mMessageDispatcher.addMesageToBackend(msg);
    }
}

