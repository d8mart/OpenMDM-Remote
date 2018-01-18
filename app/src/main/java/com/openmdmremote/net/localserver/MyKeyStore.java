package com.openmdmremote.net.localserver;

import android.content.Context;

import com.openmdmremote.WebkeyApplication;

import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.x509.X509V3CertificateGenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Date;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import static android.content.Context.MODE_PRIVATE;

public class MyKeyStore {
    private final String ALIAS = "default";
    private final String KEYSTORE = "httpdstore.bks";
    private final char[] STOREPASSWORD = "macskafarka".toCharArray();
    private final char[] KEYPASSWORD = "macskafarka".toCharArray();
    private final String ISSUER = "CN=Webkey httpd, OU=Android, C=HU";
    private final Context context;

    public MyKeyStore(Context context) {
        this.context = context;


        if(!keyStoreIsExist()) {
            createBKS();
        }
    }

    private boolean keyStoreIsExist() {
        File bks = new File(context.getFilesDir(), KEYSTORE);
        return bks.exists();
    }

    private KeyPair generateKeys() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        return keyGen.generateKeyPair();
    }

    private X509Certificate generateCertificate(KeyPair keyPair) throws CertificateEncodingException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();
        certGen.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
        certGen.setIssuerDN(new X509Principal(ISSUER));
        certGen.setNotBefore(new Date(System.currentTimeMillis()));
        certGen.setNotAfter(new Date(System.currentTimeMillis() + (1000L * 60 * 60 *24 * 365 * 10)));
        certGen.setPublicKey(keyPair.getPublic());
        certGen.setSubjectDN(new X509Principal(ISSUER));
        certGen.setSignatureAlgorithm("SHA1WithRSAEncryption");
        return certGen.generate(keyPair.getPrivate());
    }

    private void createBKS() {
        try {
            KeyStore ks = java.security.KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(null, STOREPASSWORD);

            KeyPair keyPair = generateKeys();
            X509Certificate cert = generateCertificate(keyPair);

            ks.setKeyEntry(ALIAS, keyPair.getPrivate(), KEYPASSWORD, new java.security.cert.Certificate[]{cert});

            FileOutputStream fos = context.openFileOutput(KEYSTORE, MODE_PRIVATE);
            ks.store(fos, STOREPASSWORD);
            fos.close();
        } catch (Exception e) {
            WebkeyApplication.log("MyKeyStore","error during create key store: "+e.toString());
        }
    }

    public SSLServerSocketFactory getSSLServerSocketFactory() throws InstantiationException {
        try {
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            File bks = new File(context.getFilesDir(), KEYSTORE);
            ks.load(new FileInputStream(bks), STOREPASSWORD);

            KeyManagerFactory kmf = KeyManagerFactory.getInstance("X509");
            kmf.init(ks, KEYPASSWORD);

            TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
            tmf.init(ks);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

            return sslContext.getServerSocketFactory();
        } catch (Exception e) {
            throw new InstantiationException("Cannot create sslFactory. Error: " + e.toString());
        }
    }
}
