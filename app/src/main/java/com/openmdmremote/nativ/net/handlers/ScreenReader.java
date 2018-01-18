package com.openmdmremote.nativ.net.handlers;

import com.openmdmremote.WebkeyApplication;
import com.openmdmremote.nativ.net.MessageDispatcher;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;

@ChannelHandler.Sharable
public class ScreenReader extends SimpleChannelInboundHandler<ByteBuf> {
    private final String LOGTAG = "wipc handler ScreenReader";
    int i = 0;
    MessageDispatcher mMessageDispatcher;

    public ScreenReader(MessageDispatcher messageDispatcher) {
        mMessageDispatcher = messageDispatcher;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        /*
        http://stackoverflow.com/questions/19296386/netty-java-getting-data-from-bytebuf
         */
        /**
         * image size is:
         * |img array|  4byte freq | 8byte unxtime | 1byte seq |
         */

        byte[] img = new byte[msg.readableBytes()+4+8+1];
        msg.getBytes(msg.readerIndex(), img, 0, msg.readableBytes());

        mMessageDispatcher.addImage(img);

        ReferenceCountUtil.release(msg);

        ctx.pipeline().remove(this);
        ctx.pipeline().addFirst(new ProtoSwitch(this));
    }

    private void debugWrite(byte[] img) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream("/sdcard/webkey/"+Integer.toString(i)+"_img.jpg");
            fos.write(img);
            i++;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        WebkeyApplication.log(LOGTAG, "exception: " + cause.toString());
        cause.printStackTrace();
       // ctx.close();
    }
}
