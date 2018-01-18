package com.openmdmremote.nativ.net.handlers;

import com.openmdmremote.WebkeyApplication;
import com.openmdmremote.nativ.net.WIPCProto;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;

public class ProtoSwitch extends ByteToMessageDecoder {
    private final String LOGTAG = "wipc handler ProtoSwitch";

    private static final byte MSG_TYPE_PROTOBUF = 0x2;
    private static final byte MSG_TYPE_IMAGE = 0x1;

    private final ScreenFrameDecoder screenFrameDecoder = new ScreenFrameDecoder();
    private final ScreenReader mScreenHandler;

    public ProtoSwitch(ScreenReader screenHandler) {
        mScreenHandler = screenHandler;

    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // Will use the first five bytes to detect a protocol.
        in.markReaderIndex();
        if (in.readableBytes() < 1) {
            in.resetReaderIndex();
            return;
        }

        byte msgType = in.readByte();
        switch (msgType) {
            case MSG_TYPE_PROTOBUF:
                WebkeyApplication.log(LOGTAG, "switch to protobuf");
                switchToProtobuf(ctx);
                break;
            case MSG_TYPE_IMAGE:
                switchToReadScreen(ctx);
                break;
            default:
                WebkeyApplication.log(LOGTAG, "can not recognize");
                in.clear();
                //ctx.close();
        }
    }

    private void switchToReadScreen(ChannelHandlerContext ctx) {
        ChannelPipeline p = ctx.pipeline();
        p.addLast("screenframedecoder", screenFrameDecoder);
        p.addLast("screenhandler", mScreenHandler);
        p.remove(this);
    }

    private void switchToProtobuf(ChannelHandlerContext ctx) {
        ChannelPipeline p = ctx.pipeline();
        p.addLast("varint32decoder", new ProtobufVarint32FrameDecoder());
        p.addLast("protobufdecoder", new ProtobufDecoder(WIPCProto.Message.getDefaultInstance()));
        p.addLast("protobufhandler", new ProtobufHandler());
        p.remove(this);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        WebkeyApplication.log("Webkey-net/ProtoSwitchHandler", "exception: " + cause.toString());
        cause.printStackTrace();

        //ctx.close();
    }
}
