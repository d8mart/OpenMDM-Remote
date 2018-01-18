package com.openmdmremote.harbor.client.websocket;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class NetConnectionReceiver extends WakefulBroadcastReceiver {

    private final String LOGTAG = "NetConnectionReceiver";
    private OnNetworkConnectionEventListener onNetworkConnectionEventListener;

    private boolean firstTime = true;

    public NetConnectionReceiver() {

    }

    public NetConnectionReceiver(OnNetworkConnectionEventListener onNetworkConnectionEventListener) {
        this.onNetworkConnectionEventListener = onNetworkConnectionEventListener;

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (firstTime) {
            firstTime = false;
            return;
        }

        onNetworkConnectionEventListener.onNetworkChanged();
    }
}