package com.openmdmremote.net.localserver;

import android.util.Log;

import com.openmdmremote.net.visitor.VisitorManager;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoWSD;

public class WsServer extends NanoWSD {
    private final VisitorManager mVisitorManager;


    public WsServer(int port, VisitorManager visitorManager) {
        super(port);
        mVisitorManager = visitorManager;
    }

    @Override
    public WebSocket openWebSocket(NanoHTTPD.IHTTPSession handshake) {
        Log.i("WSSERVEROPENSOCK",handshake.getRemoteHostName());
        return new LocalWsConnection(handshake, mVisitorManager);
    }
}