package com.openmdmremote.harbor.ssl;

import android.content.Context;

import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

public class SSLUtile {
    public static SSLContext getSSLContext(Context context) throws InstantiationException {
        // setup wss
        String STORETYPE = "BKS";
        String KEYSTORE = "ssl/store.bks";
        String STOREPASSWORD = "macskafarka";
        String KEYPASSWORD = "macskafarka";

        try {
            KeyStore ks = KeyStore.getInstance(STORETYPE);

            ks.load(context.getAssets().open(KEYSTORE), STOREPASSWORD.toCharArray());

            KeyManagerFactory kmf = KeyManagerFactory.getInstance("X509");
            kmf.init(ks, KEYPASSWORD.toCharArray());
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
            tmf.init(ks);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

            return sslContext;
        } catch (Exception e) {
            throw new InstantiationException("Cannot create sslFactory. Error: " + e.getMessage());
        }
    }
}
