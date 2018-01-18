package com.openmdmremote.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.StrictMode;
import android.util.Log;

import com.openmdmremote.R;
import com.openmdmremote.WebkeyApplication;
import com.openmdmremote.harbor.client.ConnectionStateNotifier.OnHarborConnectionListener;
import com.openmdmremote.harbor.client.HarborClient;
import com.openmdmremote.harbor.settings.HarborAuthSettings;
import com.openmdmremote.nativ.BackendHaltListener;
import com.openmdmremote.nativ.WIPC;
import com.openmdmremote.nativ.binary.Installer;
import com.openmdmremote.net.localserver.LocalServer;
import com.openmdmremote.net.remoteserver.MyVisitorFactory;
import com.openmdmremote.net.visitor.VisitorChangesListener;
import com.openmdmremote.net.visitor.VisitorManager;
import com.openmdmremote.service.handlers.ScreencapHandler;
import com.openmdmremote.service.services.LocalAuthService;
import com.openmdmremote.service.services.Settings;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.List;

/**
 * The service are running while the Activity is alive.
 */
public class BackgroundService extends Service {
    private final String LOGTAG = "BackgroundService";

    private final IBinder myBinder = new MyLocalBinder();
    HarborAuthSettings harborAuthSettings;
    Settings settings;

    private LocalServer localServer;
    private HarborClient harborClient;
    private OnBackgroundServiceListener onBackgroundServiceListener;

    private BackendHaltListener backendHaltListener;

    private boolean wipcHalted = false;

    private VisitorManager visitorManager;

    Context context;

    private boolean WSServerStarted = false;

    // For the foregroundNotification.
    public static Service mainService;

    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        WebkeyApplication.log(LOGTAG, "onCreate the service");
        mainService = this;
        context = this.getApplicationContext();
        harborAuthSettings = new HarborAuthSettings(context);
        settings = new Settings(context);
        
        checkBackendUpdates();

        visitorManager = new VisitorManager(context);
        initHarborConnection();
        initClocalWebserver();

        backendHaltListener = new BackendHaltListener() {
            @Override
            public void backendHalted() {
                WebkeyApplication.log("BackroundService", "halted the wipc");
                wipcHalted = true;
            }
        };

        // For low memory kill
     //  if(settings.getStarted()) {

            startWebServices();
     //   }
    }

    ScreencapHandler.ImageSendWorker sender;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null && intent.getAction() !=null) {
            if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
                startWebServices();
            } else if(intent.getAction().equals("webkey.intent.action.START")){
                Log.i("action","START");

                        //    checkBackendUpdates();

                        //    startWebServices();


            }
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy(){
        localServer.stop();
        harborClient.disconnect();
        super.onDestroy();
    }

    public void closeConns(){
        localServer.stop();
        harborClient.disconnect();
    }

    private void initClocalWebserver() {
        localServer = new LocalServer(context, visitorManager);
    }

    public void initHarborConnection() {
        MyVisitorFactory myVisitorFactory = new MyVisitorFactory(visitorManager);
        harborClient = new HarborClient(context, myVisitorFactory);
    }

    private void checkBackendUpdates() {
        Installer installer = new Installer(this);
        installer.checkUpdate();
    }

    private void waitForWipcHalt() {
        for (int i = 0; i < 4; i++) {
            if (wipcHalted) {
                break;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            i++;
        }

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public class MyLocalBinder extends Binder {
        public BackgroundService getService() {
            return BackgroundService.this;
        }
    }

    public synchronized void startWebServices() {
        WebkeyApplication.log(LOGTAG, "start service functions");
        if(webServiceIsAlive()) {
          //  return;
        }

        Log.i("INITlocalserver","HP : "+settings.getHttpPort() + " WSP : "+ settings.getWSport());
      try {
          WSServerStarted = localServer.init(this, settings.getHttpPort(), settings.getWSport());

          if (harborAuthSettings.isRegisteredToHarbor()) {
              harborClient.connect();
          } else if (settings.isFleeted()) {
              harborClient.connect();
          } else {
              harborClient.connect(); //conectar aunque no este registrado
          }

          notifyBackgroundSeviceListener();

      }finally {

          WIPC.getInstance(context).runBackend();

      }

        settings.setStarted(true);
    }

    private void notifyBackgroundSeviceListener() {
        if(onBackgroundServiceListener != null) {
            if(WSServerStarted) {
                onBackgroundServiceListener.serviceStarted();
            } else {
                onBackgroundServiceListener.serviceStopped();
            }
        }
    }

    public void leftActivity() {
        if(!WSServerStarted) {
            WebkeyApplication.log(LOGTAG, "Left the activity");
            stopWebServices();
            stopSelf();
        }
    }

    public synchronized void stopWebServices() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                // The order is important.
                WebkeyApplication.log(LOGTAG, "Stop the services");
                // Stop the backend service.
                WIPC wipc = WIPC.getInstance(context);
                wipcHalted = false;
                wipc.addHaltListener(backendHaltListener);
                wipc.teardown();

                // Stop the server.
                localServer.stop();

                // todo remove it
                if (harborAuthSettings.isRegisteredToHarbor())
                    harborClient.disconnect();

                waitForWipcHalt();

                // Notify back to the ui.
                if(onBackgroundServiceListener != null) {
                    onBackgroundServiceListener.serviceStopped();
                }

                wipc.removeHaltListener(backendHaltListener);
                settings.setStarted(false);
            }
        }).start();
    }

    public void logOut() {
        LocalAuthService localAuthService = new LocalAuthService(context);
        localAuthService.cleanUsers();
        harborAuthSettings.eraseCredentials();
        stopWebServices();
    }

    public OnHarborConnectionListener.ConnectionStates getHarborConnectionState() {
        if (harborClient.isConnected()) {
            return OnHarborConnectionListener.ConnectionStates.CONNECTED;
        } else {
            return OnHarborConnectionListener.ConnectionStates.DISCONNECTED;
        }
    }

    public boolean webServiceIsAlive() {
        return localServer.isAlive();
    }

    public String getWebkeyNickAddress() {
        String proto;
        String port = "";
        proto = "https://";
        Log.i("GETNICKADDRESS",String.valueOf(proto + context.getResources().getString(R.string.wk_server_address) + port + "/" + harborAuthSettings.getDeviceNickName()));
        return proto + context.getResources().getString(R.string.wk_server_address) + port + "/" + harborAuthSettings.getDeviceNickName();
    }

    public void getAddresses(List<String> addresses) {
        addresses.clear();
        if (localServer.isAlive()) {
            int port = localServer.getHttp_port();

            List<NetworkInterface> interfaces;
            try {
                interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
                for (NetworkInterface ni : interfaces) {
                    if (!ni.isLoopback() && ni.isUp()) {
                        for (InetAddress address : Collections.list(ni.getInetAddresses())) {
                            if (address instanceof Inet4Address) {
                                addresses.add(address.getHostAddress()+":"+Integer.toString(port));
                            }
                        }
                    }
                }
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }
    }

    public void addBackgroundServiceListener(OnBackgroundServiceListener onBackgroundServiceListener) {
        this.onBackgroundServiceListener = onBackgroundServiceListener;
        if (webServiceIsAlive()) {
            this.onBackgroundServiceListener.serviceStarted();
        } else {
            this.onBackgroundServiceListener.serviceStopped();
        }
    }

    public void removeBackgroundServiceListener() {
        onBackgroundServiceListener = null;
    }

    public void addHarborConnectionListener(OnHarborConnectionListener onHarborConnectionListener) {
        harborClient.addHarborConnectionListener(onHarborConnectionListener);
    }

    public void removeHarborConnectionListener(OnHarborConnectionListener onHarborConnectionListener) {
        harborClient.removeHarborConnectionListener(onHarborConnectionListener);
    }

    public void addVisitorChangesListener(VisitorChangesListener visitorChangesListener) {
        visitorManager.addVisitorChangesListener(visitorChangesListener);
    }

    public void removeVisitorChangesListener(VisitorChangesListener visitorChangesListener) {
        visitorManager.removeVisitorChangesListener(visitorChangesListener);
    }
}

