package com.openmdmremote.nativ.net.handlers;

import android.util.Log;

import com.openmdmremote.WebkeyApplication;
import com.openmdmremote.nativ.BackendStateNotifier;
import com.openmdmremote.nativ.authentication.AuthenticationService;
import com.openmdmremote.nativ.net.MessageSender;
import com.openmdmremote.nativ.net.WIPCProto;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;

public class AuthenticationHandler extends SimpleChannelInboundHandler<WIPCProto.Message> {
    private final String LOGTAG = "wipc handler auth";

    AuthenticationService mAuthService;
    BackendStateNotifier mBackendStateNotifier;

    MessageSender mMessageSender;

    public AuthenticationHandler(AuthenticationService authService, BackendStateNotifier backendStateNotifier, MessageSender messageSender) {
        mMessageSender = messageSender;
        mAuthService = authService;
        mBackendStateNotifier = backendStateNotifier;

    }

    private String parseKey(WIPCProto.Message msg) {
        if(msg.getType() != WIPCProto.Message.Type.CMD) {
            return null;
        }

        if(msg.getCommand().getType() != WIPCProto.Command.Type.INIT_KEY) {
            return null;
        }

        if(!msg.getCommand().hasInitkey()) {
            return null;
        }

        return msg.getCommand().getInitkey();
    }

    private WIPCProto.Message prepareKeyMessage() {
        WIPCProto.Command cmd = WIPCProto.Command.newBuilder()
                .setType(WIPCProto.Command.Type.INIT_KEY)
                .setInitkey(mAuthService.getBackendKey())
                .build();

        WIPCProto.Message msg = WIPCProto.Message.newBuilder()
                .setType(WIPCProto.Message.Type.CMD)
                .setCommand(cmd)
                .build();
        return msg;
    }

    private void removeFromPipeline(ChannelPipeline pipeline) {
        pipeline.remove("varint32decoder");
        pipeline.remove("protobufdecoder");
        pipeline.remove(this);
    }

    private void notifySuccess() {
        if (mBackendStateNotifier != null) {
            mBackendStateNotifier.notifyReady();
        }
    }

    private void notifyUnsuccess() {
        if(mBackendStateNotifier != null) {
            mBackendStateNotifier.notifySessionKeyError();
        }
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        WebkeyApplication.log(LOGTAG, "Auth codes are sending to the backend: "+ctx.channel().localAddress());
        ctx.writeAndFlush(prepareKeyMessage());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WIPCProto.Message msg) throws Exception {
        String receivedKey = parseKey(msg);
        Log.i("receivedKey :",receivedKey);

        if(receivedKey == null) {
            WebkeyApplication.log(LOGTAG, "Auth message is not valid");
            receivedKey="sasd123";
            //ctx.close();
        }

        if(mAuthService.checkJavaKey(receivedKey)) {
            WebkeyApplication.log(LOGTAG, "Auth success!");
            notifySuccess();
            removeFromPipeline(ctx.pipeline());
            ctx.fireChannelActive();
            mMessageSender.addChanel(ctx.channel());
        } else {
            WebkeyApplication.log(LOGTAG, "Auth unsuccess!");
            notifyUnsuccess();
            ctx.close();
        }
    }

    public void addChan(){

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelInactive();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Log.i("Exception :", "AuthHandler :"+cause.toString());
        WebkeyApplication.log(LOGTAG, "exception: " + cause.toString());
        cause.printStackTrace();
        //ctx.close();
    }
}
