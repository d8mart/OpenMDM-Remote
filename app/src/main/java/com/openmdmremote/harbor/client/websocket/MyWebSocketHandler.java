package com.openmdmremote.harbor.client.websocket;

import android.util.Log;

import com.google.common.base.Throwables;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;
import com.openmdmremote.WebkeyApplication;
import com.openmdmremote.harbor.HRPCProto;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

public class MyWebSocketHandler extends SimpleChannelInboundHandler<Object> {
    private static final String LOGTAG = "WebSocketHandler";

    private final MessageLite prototype = HRPCProto.Message.getDefaultInstance();

    private final WebSocketClientHandshaker handshaker;
    private final OnWebSocketEventListener onWebSocketEventListener;
    private ChannelPromise handshakeFuture;
    private Channel channel;

    public MyWebSocketHandler(OnWebSocketEventListener onWebSocketEventListener, WebSocketClientHandshaker handshaker) {
        this.onWebSocketEventListener = onWebSocketEventListener;
        this.handshaker = handshaker;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx){
        handshakeFuture = ctx.newPromise();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws InterruptedException {
        channel = ctx.channel();
        handshaker.handshake(channel);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        Log.i("channelRead0_MyWSHdlr :",msg.toString());
        Channel ch = ctx.channel();
        if (!handshaker.isHandshakeComplete()) {
            handshaker.finishHandshake(ch, (FullHttpResponse) msg);
            handshakeFuture.setSuccess();
            onWebSocketEventListener.onOpen();
            return;
        } else if (msg instanceof WebSocketFrame) {
            handleWebSocketFrame((WebSocketFrame) msg);
        }
    }

    private void handleWebSocketFrame(WebSocketFrame frame) throws InvalidProtocolBufferException {
        if (frame instanceof CloseWebSocketFrame) {
            handshaker.close(channel, (CloseWebSocketFrame) frame.retain());
            return;
        }
        if (frame instanceof PingWebSocketFrame) {
            channel.writeAndFlush(new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        if (frame instanceof BinaryWebSocketFrame) {
            ByteBuf request = frame.content().retain();
            Log.i("handlewsocketframe",request.toString());
            protoBufDecoder(request);
            return;
        }
    }

    private void protoBufDecoder(ByteBuf msg) {
        final byte[] array;
        final int offset;
        final int length = msg.readableBytes();
        if (msg.hasArray()) {
            array = msg.array();
            offset = msg.arrayOffset() + msg.readerIndex();
        } else {
            array = new byte[length];
            msg.getBytes(msg.readerIndex(), array, 0, length);
            offset = 0;
        }


        try {
            MessageLite protomsg = prototype.getParserForType().parseFrom(array, offset, length);
            onWebSocketEventListener.onMessage((HRPCProto.Message) protomsg);
        } catch (InvalidProtocolBufferException e) {
            WebkeyApplication.log(LOGTAG, "protobuf exception: "+e.toString());
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        WebkeyApplication.log(LOGTAG, "WS connection closed");
        ctx.fireChannelInactive();
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        String errormsg = Throwables.getStackTraceAsString ( cause ) ;
        WebkeyApplication.log(LOGTAG, "Protocol exception: "+errormsg);
        //ctx.close();
    }
}
