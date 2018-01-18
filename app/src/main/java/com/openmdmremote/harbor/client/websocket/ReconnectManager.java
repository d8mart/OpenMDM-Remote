package com.openmdmremote.harbor.client.websocket;

import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;

import com.openmdmremote.WebkeyApplication;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

public class ReconnectManager implements OnNetworkConnectionEventListener {

    private final String LOGTAG = "ReconnectManager";
    private final Context context;
    private final ConnectivityManager connectivityManager;
    private final NetConnectionReceiver networkConnectionReceiver;
    private final Handler handler;
    private final MyWebSocketClientNetty myWebSocketClientNetty;

    private final int RETRIES = 6;
    private final int SHORT_SLEEP_PERIOD = 3000;
    private final int LONG_SLEEP_PERIOD = 15000;
    private int sleepTime = SHORT_SLEEP_PERIOD;
    private int reconnectTimes = RETRIES;

    private boolean isRegistered = false;
    private final Object mutex = new Object();

    private volatile Channel channel;
    private final ChannelFutureListener channelFutureListener;

    public ReconnectManager(Context context, MyWebSocketClientNetty myWebSocketClientNetty) {
        this.context = context;
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        handler = new Handler();
        this.myWebSocketClientNetty = myWebSocketClientNetty;
        networkConnectionReceiver = new NetConnectionReceiver(this);

        channelFutureListener = new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future ) {
                WebkeyApplication.log(LOGTAG, "Connection lost");
                connectionLost();
            }
        };
    }

    @Override
    public void onNetworkChanged() {
        if(isNetworkAvailable()) {
            WebkeyApplication.log(LOGTAG, "Network state has changed. It is available now");
        } else {
            WebkeyApplication.log(LOGTAG,"Network state has changed. It is not available now");
        }

        WebkeyApplication.log(LOGTAG, "Initialize reconnection");
        cleanScheduledReconnections();
        myWebSocketClientNetty.connect();
    }

    private boolean isNetworkAvailable() {
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        if(activeNetwork == null) {
            return false;
        }

        if (activeNetwork.isConnectedOrConnecting()) {
            return true;
        } else {
            return false;
        }
    }

    private void registerReceiver() {
        synchronized (mutex) {
            if (!isRegistered) {
                context.registerReceiver(networkConnectionReceiver,
                        new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
                isRegistered = true;
            }
        }
    }

    private void unregisterReceiver() {
        synchronized (mutex) {
            if (isRegistered) {
                context.unregisterReceiver(networkConnectionReceiver);
                isRegistered = false;
            }
        }
    }

    private void scheduleReconnection() {
        updateReconnectTimes();

        if(reconnectTimes <= 0) {
            registerReceiver();
        }

        WebkeyApplication.log(LOGTAG, "Schedule reconnection after " + sleepTime + "ms (" + reconnectTimes + "X)");
        cleanScheduledReconnections();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                WebkeyApplication.log(LOGTAG, "Initialize reconnection");
                myWebSocketClientNetty.connect();
            }
        }, sleepTime);
    }

    private void updateReconnectTimes() {
        if (reconnectTimes > 0) {
            reconnectTimes--;
        } else {
            sleepTime = LONG_SLEEP_PERIOD;
        }
    }

    private void cleanScheduledReconnections() {
        handler.removeCallbacksAndMessages(null);
    }


    public void enableReconnection(Channel channel) {
        this.channel = channel;
        channel.closeFuture().addListener(channelFutureListener);
        registerReceiver();
    }

    public void disableReconnection() {
        handler.removeCallbacksAndMessages(null);
        if(channel != null) {
            channel.closeFuture().removeListener(channelFutureListener);
        }
        unregisterReceiver();
    }

    public void connectionEstablished() {
        reconnectTimes = RETRIES;
        sleepTime = SHORT_SLEEP_PERIOD;
    }

    public void connectionLost() {
        if (!isNetworkAvailable()) {
            WebkeyApplication.log(LOGTAG, "Network not available. Try connect later and now");
            registerReceiver();
        }

        scheduleReconnection();
    }
}
