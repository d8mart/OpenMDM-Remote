package com.openmdmremote.nativ.net;

import android.os.Handler;
import android.os.Looper;

import com.openmdmremote.WebkeyApplication;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

public class MessageSender {
    private static final String LOGTAG = "WIPC MessageSender";
    private Handler handler;

    MessageDispatcher mMessageDispatcher;
    volatile Channel mChannel;

    public MessageSender(MessageDispatcher messageDispatcher) {
        mMessageDispatcher = messageDispatcher;

        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                handler = new Handler();
                Looper.loop();
            }
        }).start();

        // Ensure.
        for(int i=50; i > 0; i--) {
            if(handler != null) {
                return;
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
        }
    }

    public void addChanel(Channel channel) {
        mChannel = channel;
        sendMessage();
    }

    private void sendMessage() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    WIPCProto.Message msg = mMessageDispatcher.takeToTheNative();
                    mChannel.writeAndFlush(msg).addListener(trafficGenerator);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    WebkeyApplication.log(LOGTAG, e.toString());
                }
            }
        });
    }

    private ChannelFutureListener trafficGenerator = new ChannelFutureListener() {
        @Override
        public void operationComplete(ChannelFuture future) {
            if (future.isSuccess()) {
                sendMessage();
            }
        }
    };
}
