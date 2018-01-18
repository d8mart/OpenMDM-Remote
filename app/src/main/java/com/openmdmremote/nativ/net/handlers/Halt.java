package com.openmdmremote.nativ.net.handlers;

import com.openmdmremote.WebkeyApplication;
import com.openmdmremote.nativ.authentication.AuthenticationService;
import com.openmdmremote.nativ.net.WIPCProto;
import com.openmdmremote.nativ.net.WIPCProto.Message;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class Halt extends ChannelInboundHandlerAdapter {
    private final String LOGTAG = "wipc handler halt";

    AuthenticationService mAuthService;

    public Halt(AuthenticationService authService) {
        mAuthService = authService;
    }

    private void sendHalteAndClose(ChannelHandlerContext ctx) {
        ctx.writeAndFlush(prepareHaltMessage()).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                WebkeyApplication.log(LOGTAG, "Channel close");
                future.channel().close();
            }
        });
        // Security reason.
        mAuthService.renewKeys();
    }

    private Message prepareHaltMessage() {
        WIPCProto.Command cmd = WIPCProto.Command.newBuilder()
                .setType(WIPCProto.Command.Type.HALT)
                .build();

        Message msg = Message.newBuilder()
                .setType(Message.Type.CMD)
                .setCommand(cmd)
                .build();
        return msg;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        if(ctx.pipeline().get("auth") == null) {
            sendHalteAndClose(ctx);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        sendHalteAndClose(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelInactive();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        WebkeyApplication.log(LOGTAG, "exception: " + cause.toString());
        //ctx.close();
    }
}
