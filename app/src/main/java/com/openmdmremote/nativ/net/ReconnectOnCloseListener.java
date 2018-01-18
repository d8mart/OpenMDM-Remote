package com.openmdmremote.nativ.net;

import android.util.Log;

import com.openmdmremote.WebkeyApplication;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;

public class ReconnectOnCloseListener implements ChannelFutureListener {
    private final String LOGTAG = "WIPC ReconnectOnCloseListener";

    private final long RECONNECT_INTERVALL = 4L;

    private final NativeConnection mNativeConnection;
    private final AtomicBoolean disconnectRequested = new AtomicBoolean(false);
    private Timer timer = new HashedWheelTimer();


    public ReconnectOnCloseListener(NativeConnection NativeConnection) {
        this.mNativeConnection = NativeConnection;
    }

    public synchronized void requestReconnect() {
        disconnectRequested.set(false);
        timer = new HashedWheelTimer();
    }

    public synchronized void requestDisconnect() {
        disconnectRequested.set(true);
        timer.stop();
    }

    // Call when closed the connection or connection failed.
    @Override
    public synchronized void operationComplete(ChannelFuture future) throws Exception {
        if (!future.isSuccess()) {
            WebkeyApplication.log(LOGTAG, "connection failed");
            scheduleReconnect();
        } else {
            Channel channel = future.channel();
            channel.closeFuture().addListener(this);
        }
    }

    private synchronized void scheduleReconnect() {

        if (!disconnectRequested.get()) {
            timer.newTimeout(new TimerTask() {
                public synchronized void run(Timeout timeout) throws Exception {
                    Log.i("scheduleReconnect","onReconnect");
                    mNativeConnection.onReconnect();
                }
            }, RECONNECT_INTERVALL, TimeUnit.SECONDS);
        }
    }

    public boolean isReconnectionActive() {
        return !disconnectRequested.get();
    }
}
