package com.openmdmremote.harbor.client.websocket;

import com.openmdmremote.WebkeyApplication;
import com.openmdmremote.harbor.HRPCProto.Message;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler;

public class HarborProtocolHandler extends SimpleChannelInboundHandler<Message> {
    private final static String LOGTAG = "HarborProtocolHandler";
    private final OnWebSocketEventListener harborEventListener;

    public HarborProtocolHandler(OnWebSocketEventListener harborEventListener) {
        this.harborEventListener = harborEventListener;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt == WebSocketClientProtocolHandler.ClientHandshakeStateEvent.HANDSHAKE_COMPLETE) {
            WebkeyApplication.log(LOGTAG, "Connected to ws channel");
            harborEventListener.onOpen();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        WebkeyApplication.log(LOGTAG, "WS connection closed");
        ctx.fireChannelInactive();
        harborEventListener.onClose();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        harborEventListener.onMessage(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        WebkeyApplication.log(LOGTAG, "WS connection exception: " + cause.toString());
        ctx.close();
    }
}
