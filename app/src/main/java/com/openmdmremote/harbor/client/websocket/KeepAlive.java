package com.openmdmremote.harbor.client.websocket;

import com.openmdmremote.WebkeyApplication;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.timeout.IdleStateEvent;

import static io.netty.buffer.Unpooled.wrappedBuffer;


public class KeepAlive extends ChannelDuplexHandler {

    private final String LOGTAG = "HarborKeepAlive";

    public static final int INTERVAL = 60;
    private final byte[] KEEPALIVEMSG = new byte[]{(byte) 0b10011001};
    private final BinaryWebSocketFrame KEEPALIVE_FRAME = new BinaryWebSocketFrame(wrappedBuffer(KEEPALIVEMSG));

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            sendKeepAlive(ctx);
        }
    }

    private void sendKeepAlive(ChannelHandlerContext ctx) {
        KEEPALIVE_FRAME.retain();
        ChannelFuture cf = ctx.writeAndFlush(KEEPALIVE_FRAME);
        cf.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if(!future.isSuccess()) {
                    WebkeyApplication.log(LOGTAG, "Failed to send keepalive: "+future.cause().toString());
                }
            }
        });
    }
}
