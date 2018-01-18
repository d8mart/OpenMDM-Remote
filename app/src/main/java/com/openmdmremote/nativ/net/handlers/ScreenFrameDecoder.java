package com.openmdmremote.nativ.net.handlers;

import com.openmdmremote.WebkeyApplication;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class ScreenFrameDecoder extends ByteToMessageDecoder {
    private final String LOGTAG =  "wipc handler ScreenFrameDecoder";


    public ScreenFrameDecoder() {
    }

    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // The initial 4 is the frequency's length (read after the image).
        int length = 4;

        in.markReaderIndex();

        if(in.readableBytes() < 4) {
            in.resetReaderIndex();
            return;
        }

        // Read the length of the message.
        length += in.readBytes(4).readInt();

        if(in.readableBytes() < length) {
            in.resetReaderIndex();
            return;
        }
        out.add(in.readBytes(length).retain());
        ctx.pipeline().remove(this);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        WebkeyApplication.log(LOGTAG,"exception: "+cause.toString());
        cause.printStackTrace();
        //ctx.close();
    }
}