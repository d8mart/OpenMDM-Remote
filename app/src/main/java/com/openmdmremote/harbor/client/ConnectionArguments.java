package com.openmdmremote.harbor.client;

import android.content.Context;
import android.util.Log;

import com.openmdmremote.harbor.settings.HarborServerSettings;
import com.openmdmremote.harbor.ssl.SSLUtile;
import java.net.URI;
import java.net.URISyntaxException;
import javax.net.ssl.SSLContext;

public class ConnectionArguments {
    private final Context context;

    private boolean secure;
    private String host;
    private int port;
    private URI uri;
    private SSLContext sslContext;

    public ConnectionArguments(Context context, String host, int port, boolean secure) {
        this.context = context;
        this.host = host;
        this.port = port;
        this.secure = secure;

        if (secure) {
            setSSLContext();
        }
        prepareUri();
    }

    private void setSSLContext() {
        try {
            sslContext = SSLUtile.getSSLContext(context);
        } catch (InstantiationException e) {
            prepareUri();
        }
    }

    private void prepareUri() {
        String prefix;
        if (secure) {
            prefix = "ws://";
        } else {
            prefix = "ws://";
        }

        try {
            uri = new URI(prefix + host + ":" + port + HarborServerSettings.PATH_HARBORCONNECTION);
            Log.i("prepareURI_CA",uri.toString());
        } catch (URISyntaxException e) {
        }
    }

    public URI getUri() {
        return uri;
    }

    public SSLContext getSSLContext() {
        return sslContext;
    }

    public boolean isSecure() {
        return secure;
    }
}
