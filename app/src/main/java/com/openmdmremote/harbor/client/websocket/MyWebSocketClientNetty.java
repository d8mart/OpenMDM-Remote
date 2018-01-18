package com.openmdmremote.harbor.client.websocket;

import android.content.Context;
import android.util.Log;

import com.openmdmremote.WebkeyApplication;
import com.openmdmremote.harbor.HRPCProto;
import com.openmdmremote.harbor.client.ConnectionArguments;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.timeout.IdleStateHandler;

import static io.netty.buffer.Unpooled.wrappedBuffer;

public class MyWebSocketClientNetty {

    private static final String LOGTAG = "Netty";

    private final ConnectionArguments connectionArguments;
    private final Bootstrap bootstrap;
    private final ReconnectManager reconnectManager;
    private final OnWebSocketEventListener onWebSocketEventListener;
    private Channel channel;

    public MyWebSocketClientNetty(Context context, final ConnectionArguments connectionArguments, final OnWebSocketEventListener onWebSocketEventListener) {
        this.connectionArguments = connectionArguments;
        this.onWebSocketEventListener = onWebSocketEventListener;
        EventLoopGroup group = new NioEventLoopGroup();

        reconnectManager = new ReconnectManager(context, this);
        bootstrap = new Bootstrap();

        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                      @Override
                      protected void initChannel(SocketChannel ch) throws InstantiationException {
                          ChannelPipeline p = ch.pipeline();

                         /* if (connectionArguments.isSecure()) {
                              SSLEngine sslEngine = connectionArguments.getSSLContext().createSSLEngine();
                              sslEngine.setUseClientMode(true);
                              p.addLast(new SslHandler(sslEngine));
                          }*/

                          p.addLast(new HttpClientCodec());
                          p.addLast(new HttpObjectAggregator(64 * 1024));
                          p.addLast(new IdleStateHandler(0, 0, KeepAlive.INTERVAL));
                          p.addLast(new KeepAlive());
                          p.addLast(new MyWebSocketHandler(onWebSocketEventListener, WebSocketClientHandshakerFactory.newHandshaker(connectionArguments.getUri(), WebSocketVersion.V13, null, true, new DefaultHttpHeaders())));
                }
            });

        prepareAddress();



    }

    private void prepareAddress() {
        Thread t = new Thread() {
            public void run() {
                Log.i("PREPAREADDRESS","HOST :"+connectionArguments.getUri().getHost()+" PORT :"+connectionArguments.getUri().getPort());
                bootstrap.remoteAddress(connectionArguments.getUri().getHost(), connectionArguments.getUri().getPort());
               // Log.i("BOOTSTRAP",String.valueOf(bootstrap));
            }
        };
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {

        }
    }

    public void connect() {
        if(channelIsOpen()) {
            WebkeyApplication.log(LOGTAG, "Channel is already open");

        } else {
            WebkeyApplication.log(LOGTAG, "Try connect MyWebsocketClientNetty");

            ChannelFuture future = bootstrap.connect();
            channel = future.channel();
            Log.i("channelremotead",channel.toString());
            enableReconnection();

            addOperationCompleteListener(future);
            addOnCloseListener();
        }

       /* ChannelFuture future = bootstrap.connect();
        channel = future.channel();
        Log.i("channelremotead",channel.toString());
        enableReconnection();

        addOperationCompleteListener(future);
        addOnCloseListener();*/
    }

    private boolean channelIsOpen() {
        if(channel == null) {
            return false;
        }


        return channel.isOpen();
    }

    public void disconnect() {
        reconnectManager.disableReconnection();
        if(channel != null) {
            channel.disconnect();
        }
    }

    public void writeAndFlush(HRPCProto.Message msg) {
        Log.i("MyNettyPROTOMSG",String.valueOf(msg));
       // Log.i("PROTOWRAPPEDBUFFER",wrappedBuffer(msg.toByteArray()).toString());
        if(channel != null)
        channel.writeAndFlush(new BinaryWebSocketFrame(wrappedBuffer(msg.toByteArray())));
    }

    // enviar imagen no .proto
    public void writeAndFlushNOPROTO(byte[] msg) {
        Log.i("MyNettyNOPROTOMSG",String.valueOf(msg));
        // Log.i("PROTOWRAPPEDBUFFER",wrappedBuffer(msg.toByteArray()).toString());
        if(channel != null)
        channel.writeAndFlush(new BinaryWebSocketFrame(wrappedBuffer(msg)));
    }
    //

    private void enableReconnection() {
        reconnectManager.enableReconnection(channel);
    }


    private void addOperationCompleteListener(ChannelFuture future) {
        future.addListener( new ChannelFutureListener() {

            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if(!future.isSuccess()) {
                    WebkeyApplication.log(LOGTAG, "Socket connection has been failed ("+connectionArguments.getUri()+"). Reason: " + future.cause().toString());
                    future.channel().close();
                } else {
                    WebkeyApplication.log(LOGTAG, "Socket connection established."+" ( "+connectionArguments.getUri()+" ) ");
                    reconnectManager.connectionEstablished();
                }
            }
        });
    }

    private void addOnCloseListener() {
        channel.closeFuture().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future ) {
                onWebSocketEventListener.onClose();
            }
        });
    }
}
