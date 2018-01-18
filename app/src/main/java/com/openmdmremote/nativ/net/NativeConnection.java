package com.openmdmremote.nativ.net;

import com.openmdmremote.WebkeyApplication;
import com.openmdmremote.nativ.BackendStateNotifier;
import com.openmdmremote.nativ.authentication.AuthenticationService;
import com.openmdmremote.nativ.net.exceptions.AlreadyConnectedException;
import com.openmdmremote.nativ.net.exceptions.ConnectionInProgressException;
import com.openmdmremote.nativ.net.handlers.AuthenticationHandler;
import com.openmdmremote.nativ.net.handlers.Halt;
import com.openmdmremote.nativ.net.handlers.ProtoSwitch;
import com.openmdmremote.nativ.net.handlers.ScreenReader;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

public class NativeConnection {
    private final String LOGTAG = "WIPC NativeConnection";
    private final int PORT = 8888;
    private final String HOST = "127.0.0.1";
    private ReconnectOnCloseListener reconnectOnCloseListener;
    private BackendStateNotifier mBackendStateNotifier;
    private AuthenticationService mAuthService;
    private MessageDispatcher messageDispatcher = new MessageDispatcher();

    private ChannelFuture channelFuture = null;

    Bootstrap bootstrap;

    public NativeConnection(final AuthenticationService authService, final BackendStateNotifier backendStateNotifier) {
        mAuthService = authService;
        mBackendStateNotifier = backendStateNotifier;

        final EventLoopGroup group = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .remoteAddress(HOST, PORT)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline p = ch.pipeline();

                        ScreenReader screenReader = new ScreenReader(messageDispatcher);

                        MessageSender messageSender = new MessageSender(messageDispatcher);

                        // For input channel.
                        p.addLast("varint32decoder", new ProtobufVarint32FrameDecoder());
                        p.addLast("protobufdecoder", new ProtobufDecoder(WIPCProto.Message.getDefaultInstance()));

                        // For output channel.
                        p.addLast("varint32encoder", new ProtobufVarint32LengthFieldPrepender());
                        p.addLast("protobufencoder", new ProtobufEncoder());

                        // First time channel.
                        p.addLast("auth", new AuthenticationHandler(authService, mBackendStateNotifier, messageSender));
                        p.addLast("protoswitch", new ProtoSwitch(screenReader));
                    }
                });


        reconnectOnCloseListener = new ReconnectOnCloseListener(this);
    }

    /*
    It may used for unit test only.
     */
    public NativeConnection(final AuthenticationService authService) {
        this(authService, new BackendStateNotifier());
    }


    public synchronized void onReconnect() {
        doConnect();
    }

    private synchronized void doConnect() {
        WebkeyApplication.log(LOGTAG, "try connect NativeConection");
        channelFuture = bootstrap.connect();
        reconnectOnCloseListener.requestReconnect();
        channelFuture.addListener(reconnectOnCloseListener);

        /*
        Backend state notification
        Ha az auth handler nincs a pipelineban akkor az azt jeletni, hogy az auth sikeres volt es
        onnantol kezdve a disconnect callback hasznos.
         */
        channelFuture.channel().closeFuture().addListener(new ChannelFutureListener() {
            @Override
            public synchronized void operationComplete(ChannelFuture future) throws Exception {
                if (mBackendStateNotifier != null) {
                    if (future.channel().pipeline().get("auth") == null) {
                        mBackendStateNotifier.notifyDisconnected();
                        WebkeyApplication.log(LOGTAG, "onReconnect NativeConection");
                        onReconnect();
                    }
                }
            }
        });
    }

    private boolean hasConnectionInProgress() {
        if(channelFuture == null ) {
            return false;
        }

        if (channelFuture.isDone() && reconnectOnCloseListener.isReconnectionActive()) {
            return true;
        } else if(!channelFuture.isDone()){
            return true;
        } else {
            return false;
        }

    }

    public synchronized void connect() throws AlreadyConnectedException, ConnectionInProgressException {
        if(isReady()) {
            throw new AlreadyConnectedException();
        }
        if(hasConnectionInProgress()) {
            throw new ConnectionInProgressException();
        }

        doConnect();
    }

    public void backendHalt() {
        reconnectOnCloseListener.requestDisconnect();

        if (!isReady() && !hasConnectionInProgress()) {
            channelFuture = bootstrap.connect();
        }

        /*
         * A hozzadas hatasara automatikusan kuldi a halt messaget.
         */
        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                channelFuture.channel().pipeline().addLast("halt", new Halt(mAuthService));
            }
        });
    }

    public boolean isReady() {
        if(channelFuture != null) {
            return channelFuture.channel().isActive();
        } else {
            return false;
        }
    }

    public synchronized void disconnect() {
        WebkeyApplication.log(LOGTAG, "Disconnect from the backend");
        reconnectOnCloseListener.requestDisconnect();
        if(channelFuture != null) {
            channelFuture.channel().disconnect();
            channelFuture.channel().close();
        }
    }

    public MessageDispatcher getMessageDispatcher() {
        return messageDispatcher;
    }
}
