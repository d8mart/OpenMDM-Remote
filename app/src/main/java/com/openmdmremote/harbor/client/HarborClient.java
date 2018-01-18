package com.openmdmremote.harbor.client;

import android.content.Context;
import android.util.Log;

import com.openmdmremote.MacAddress;
import com.openmdmremote.WebkeyApplication;
import com.openmdmremote.harbor.HRPCProto;
import com.openmdmremote.harbor.Visitor;
import com.openmdmremote.harbor.client.ConnectionStateNotifier.ConnectionStateNotifier;
import com.openmdmremote.harbor.client.ConnectionStateNotifier.OnHarborConnectionListener;
import com.openmdmremote.harbor.client.services.HarborAuthService;
import com.openmdmremote.harbor.client.handlers.HarborMessageHandler;
import com.openmdmremote.harbor.client.services.OnHarborAuthListener;
import com.openmdmremote.harbor.client.handlers.RemoteAuthService;
import com.openmdmremote.harbor.client.handlers.SettingsHandler;
import com.openmdmremote.harbor.client.handlers.VisitorHandler;
import com.openmdmremote.harbor.interfaces.OnAdminAuthListener;
import com.openmdmremote.harbor.interfaces.VisitorFactory;
import com.openmdmremote.harbor.client.websocket.MyWebSocketClientNetty;
import com.openmdmremote.harbor.client.websocket.OnWebSocketEventListener;
import com.openmdmremote.harbor.settings.HarborServerSettings;
import com.openmdmremote.net.visitor.VisitorChannel;
import com.openmdmremote.net.visitor.WebkeyVisitor;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class HarborClient  implements OnHarborAuthListener, VisitorChannel {

    private static final String LOGTAG = "HarborClient";

    private final ConnectionStateNotifier connectionStateNotifier = new ConnectionStateNotifier();
    private final Map<HRPCProto.Message.Type, HarborMessageHandler> handlers = new HashMap<>();
    private final MyWebSocketClientNetty webSocketClient;

    private final HarborAuthService harborAuthService;
    private final RemoteAuthService remoteAuthService;
    private final Context context;


    private Set<OnHarborConnectionListener> listeners = new LinkedHashSet<>();
    private OnHarborConnectionListener.ConnectionStates lastConnState = OnHarborConnectionListener.ConnectionStates.DISCONNECTED;

    private final Object listenerLock = new Object();

    public HarborClient(Context context, VisitorFactory visitorFactory) {
        this.context = context;
        harborAuthService = new HarborAuthService(context);


        webSocketClient = new MyWebSocketClientNetty(context, getConnectionArguments(), new OnWebSocketEventListener(){

            @Override
            public void onOpen() {
           //    harborAuthService.doAuth(webSocketClient, HarborClient.this);

                String prefix =  "HARDWARE-ID$"; String macadd = MacAddress.getMacAddress(context);
                if(macadd!=null){
                    String hwrId = MacAddress.buildMacAsId(macadd);
                    sendMessage(prefix+hwrId);
                    Log.i("hrdID","Enviado");
                    harborAuthService.loggedin=true;


                }
                new WebkeyVisitor(HarborClient.this);

            }



            @Override
            public void onMessage(HRPCProto.Message msg) {
                onHarborMessage(msg);
            }

            @Override
            public void onClose() {
                connectionClosed();
            }
        });

        remoteAuthService = new RemoteAuthService(context, webSocketClient);
        SettingsHandler settingsHandler = new SettingsHandler(context);
        VisitorHandler visitorHandler = new VisitorHandler(visitorFactory, this);

        handlers.put(HRPCProto.Message.Type.AUTHRESPONSE, harborAuthService);
        handlers.put(HRPCProto.Message.Type.REMOTEAUTH, remoteAuthService);
        handlers.put(HRPCProto.Message.Type.SETTINGS, settingsHandler);
        handlers.put(HRPCProto.Message.Type.VISITOR, visitorHandler);
        handlers.put(HRPCProto.Message.Type.TRANSPORT, visitorHandler);
    }

    @Override
    public void onAuthResult(boolean success) {
        if(success) {
            connectionStateNotifier.notifyConnected();
            updateDeviceInfo();
        } else {
            webSocketClient.disconnect();
            connectionStateNotifier.notifyDisconnected();
        }
    }

    private void updateDeviceInfo() {
        DeviceInfos deviceInfos = new DeviceInfos(context);
        HRPCProto.Message msg = MessageBuilder.getDeviceInfosMessage(deviceInfos);
        webSocketClient.writeAndFlush(msg);
    }

    private void onHarborMessage(HRPCProto.Message msg) {
        Log.i("onHarborMSN :",msg.toString());
        HRPCProto.Message.Type msgType = msg.getType();
        if(handlers.containsKey(msgType)) {
            handlers.get(msgType).onMessage(msg);
        }
    }

    private void connectionClosed() {
        WebkeyApplication.log(LOGTAG, "Connection closed");
        callHandlersTeardown();
        connectionStateNotifier.notifyDisconnected();
    }

    private void callHandlersTeardown() {
        for(HarborMessageHandler h : handlers.values()){
            h.onClosed();
        }
    }

    public synchronized void connect() {
        connectionStateNotifier.notifyConnecting();
        webSocketClient.connect();
    }

    public void disconnect() {
        webSocketClient.disconnect();
    }

    private ConnectionArguments getConnectionArguments() {
        HarborServerSettings harborHarborServerSettings = new HarborServerSettings();

        String host = harborHarborServerSettings.getHarborServerAddress();
        int port = harborHarborServerSettings.getHarborServerPort();
        boolean secure = harborHarborServerSettings.isSecure();

        return new ConnectionArguments(context, host, port, secure);
    }

    public void transportMessage(HarborMessage harborMessage) {
        HRPCProto.Message msg = MessageBuilder.getTransportMessage(harborMessage);
        webSocketClient.writeAndFlush(msg);
    }

    // transportMessage NOPROTO
    public void transportMessageNOPROTO(byte[] bytes) {
       // HRPCProto.Message msg = MessageBuilder.getTransportMessage(harborMessage);
        webSocketClient.writeAndFlushNOPROTO(bytes);
    }
    //
    public void addHarborConnectionListener(OnHarborConnectionListener onHarborConnectionListener) {
        connectionStateNotifier.addHarborConnectionListener(onHarborConnectionListener);
    }

    public void removeHarborConnectionListener(OnHarborConnectionListener onHarborConnectionListener) {
        connectionStateNotifier.removeHarborConnectionListener(onHarborConnectionListener);
    }

    public boolean isConnected() {
        return harborAuthService.isConnected();
    }

    public void sendRemoteAuthRequestToHarbor(String session, OnAdminAuthListener onAdminAuthListener) {
        remoteAuthService.remoteAuthRequest(session, onAdminAuthListener);
    }

    @Override
    public void sendMessage(byte[] msg) {
        Log.i("HarborClient","send bytes");
        transportMessage(new HarborMessage("asd1234", msg));
       // transportMessageNOPROTO(msg); //transportar imagen o enviar imagen no proto
    }

    @Override
    public void sendMessage(String msg) {
        Log.i("HarborClient","send ex void String :"+msg);
        new Visitor("asd1234",this).send(msg);
    }


}
