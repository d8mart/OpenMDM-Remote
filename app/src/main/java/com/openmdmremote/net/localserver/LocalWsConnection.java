package com.openmdmremote.net.localserver;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import com.openmdmremote.WebkeyApplication;
import com.openmdmremote.net.visitor.VisitorChannel;
import com.openmdmremote.net.visitor.VisitorManager;
import com.openmdmremote.net.visitor.WebkeyVisitor;

import java.io.IOException;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoWSD;

public class LocalWsConnection extends NanoWSD.WebSocket implements VisitorChannel {

    private final Handler handler;
    private final Looper looper;
    WebkeyVisitor webkeyVisitor;

	public LocalWsConnection(NanoHTTPD.IHTTPSession handshake, VisitorManager visitorManager) {
		super(handshake);
        webkeyVisitor = new WebkeyVisitor(visitorManager, this);

        // Prepare looper.
        HandlerThread thread = new HandlerThread("Local WS server looper");
        thread.start();
        looper = thread.getLooper();
        handler = new Handler(looper);
	}

    @Override
    protected void onOpen() {
    }

    @Override
	protected void onMessage(NanoWSD.WebSocketFrame messageFrame) {
        Log.i("onMessage LocalWsConn","Recibido");

        messageFrame.setUnmasked();
        webkeyVisitor.onMessage(messageFrame.getTextPayload());
    }

	@Override
	protected void onPong(NanoWSD.WebSocketFrame pongFrame) {
        WebkeyApplication.log("LocalWsConnection", "Pong frame!");
    }

	@Override
	protected void onClose(NanoWSD.WebSocketFrame.CloseCode code, String reason, boolean initiatedByRemote) {
        webkeyVisitor.onClose();
        looper.quit();
    }
    
    @Override
	protected void onException(IOException e) {
    }

    /*From here the visitor's functions; */
    @Override
    public void sendMessage(final byte[] msg) {
        Log.i("LOCALWSTEST",String.valueOf(msg));
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    send(msg);

                } catch (IOException e) {
                }
            }
        });
    }

    @Override
    public void sendMessage(final String msg) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    send(msg);
                } catch (IOException e) {
                }
            }
        });
    }
}