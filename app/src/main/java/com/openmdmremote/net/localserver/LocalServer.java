package com.openmdmremote.net.localserver;

import android.app.Service;
import android.content.Context;

import com.openmdmremote.BuildConfig;
import com.openmdmremote.WebkeyApplication;
import com.openmdmremote.net.visitor.VisitorManager;

import java.io.IOException;

import javax.net.ssl.SSLServerSocketFactory;

import fi.iki.elonen.NanoHTTPD;

public class LocalServer {

    private int http_port;
    private NanoHTTPD hs;
    private final VisitorManager visitorManager;
    private WsServer ws;
    private MyKeyStore keyStore;

    public LocalServer(Context context, VisitorManager visitorManager) {
        this.visitorManager = visitorManager;
        keyStore = new MyKeyStore(context);
    }


    public synchronized boolean init(Service service, int http_port, int ws_port){
        this.http_port = http_port;

        if(isAlive()) {
            return false;
        }

        // Start the WebSocket server and HTTP server
        try {
            hs = new HttpServer(http_port, service);
            ws = new WsServer(ws_port, visitorManager);

            if(BuildConfig.LOCAL_HTTPD_IS_SECURE) {
                makeSecure();
            }

          //  Log.i("Localhttps","Host :"+hs.getHostname()+" port :"+hs.getListeningPort());
          //  Log.i("Localwss","Host :"+ws.getHostname()+" port :"+ws.getListeningPort());

            hs.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
            ws.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        } catch (InstantiationException | IOException e) {
            WebkeyApplication.log("LocalServer","init failed: "+e.toString());
        }

        if(isAlive()) {
            WebkeyApplication.log("LocalServer","services are running");
        } else {
            WebkeyApplication.log("LocalServer","failed to start web services");
        }

        // Check the results.
        if(isAlive()) {
            return true;
        } else {
            ws.stop();
            hs.stop();
            return false;
        }
    }

    private void makeSecure() throws InstantiationException {
        SSLServerSocketFactory factory = keyStore.getSSLServerSocketFactory();

        hs.makeSecure(factory, null);
        ws.makeSecure(factory, null);
    }

    public boolean isAlive() {
        if(ws != null && hs != null) {
            if(ws.isAlive() && hs.isAlive()) {
                return true;
            }
        }
        return false;
    }

    public void stop(){
        if(isAlive()) {
            ws.stop();
            hs.stop();
        }
    }

    public int getHttp_port(){
        return http_port;
    }
}
