package com.openmdmremote.nativ.net.handlers;

import com.openmdmremote.WebkeyApplication;
import com.openmdmremote.nativ.net.WIPCProto;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ProtobufHandler extends SimpleChannelInboundHandler<WIPCProto.Message> {

    public ProtobufHandler() {
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WIPCProto.Message msg) throws Exception {
        WebkeyApplication.log("Webkey-net/protobuf handler", "hat itt nem kellene lennunk");
        //ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        WebkeyApplication.log("Webkey-net/protobuf handler", "exception: " + cause.toString());
        //ctx.close();
    }
}
