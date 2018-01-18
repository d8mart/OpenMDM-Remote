package com.openmdmremote.net.visitor;

import android.content.Context;
import android.util.Log;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.openmdmremote.R;
import com.openmdmremote.WebkeyApplication;
import com.openmdmremote.harbor.client.HarborClient;
import com.openmdmremote.service.dto.Message;
import com.openmdmremote.service.dto.Payload;
import com.openmdmremote.service.dto.SignUpHandler;
import com.openmdmremote.service.dto.ToastPayload;
import com.openmdmremote.service.handlers.AdminAuthHandler;
import com.openmdmremote.service.handlers.AuthHandler;
import com.openmdmremote.service.handlers.ButtonHandler;
import com.openmdmremote.service.handlers.KeyHandler;
import com.openmdmremote.service.handlers.LocationHandler;
import com.openmdmremote.service.handlers.NotificationsHandler;
import com.openmdmremote.service.handlers.OpenURLHandler;
import com.openmdmremote.service.handlers.ScreencapHandler;
import com.openmdmremote.service.handlers.TouchHandler;
import com.openmdmremote.service.handlers.interfaces.MessageHandler;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import ua.naiksoftware.stomp.client.StompClient;

import static com.openmdmremote.service.dto.Message.Type.BUTTON;
import static com.openmdmremote.service.dto.Message.Type.ERROR;
import static com.openmdmremote.service.dto.Message.Type.KEY;
import static com.openmdmremote.service.dto.Message.Type.LOCATION_START;
import static com.openmdmremote.service.dto.Message.Type.LOCATION_STOP;
import static com.openmdmremote.service.dto.Message.Type.OPENURL;
import static com.openmdmremote.service.dto.Message.Type.RESTART;
import static com.openmdmremote.service.dto.Message.Type.SCREEN_ACK;
import static com.openmdmremote.service.dto.Message.Type.SCREEN_OPTIONS;
import static com.openmdmremote.service.dto.Message.Type.SCREEN_START;
import static com.openmdmremote.service.dto.Message.Type.SCREEN_STOP;
import static com.openmdmremote.service.dto.Message.Type.TOAST;
import static com.openmdmremote.service.dto.Message.Type.TOUCH;

public class WebkeyVisitor {

    private Context context;
    private Gson gson = new GsonBuilder().create();

    private final VisitorChannel mVisitorChannel;
    private final VisitorManager visitorManager;

    private ListMultimap<Message.Type, MessageHandler> handlers = ArrayListMultimap.create();

    private boolean loggedin = false;

    private String username = "";
    private BrowserInfo browserInfo;
    private HarborClient harborClient;

    //

    OkHttpClient client;
    StompClient stompClient;

    public WebkeyVisitor(VisitorManager visitorManager, VisitorChannel visitorChannel) {
        this.visitorManager = visitorManager;
        this.mVisitorChannel = visitorChannel;
        context = WebkeyApplication.getContext();
        authNOTSuccess(); //need
        visitorManager.addNewVisitor(this);
    }

    public WebkeyVisitor(VisitorChannel visitorChannel) {
        this.visitorManager = null;
        this.mVisitorChannel = visitorChannel;
        context = WebkeyApplication.getContext();
        authNOTSuccess();
        //  visitorManager.addNewVisitor(this);
    }

    public void onMessage(String message){
        Message msg;
        try {
            msg = parseMsg(message);
        } catch (Exception e) {
            sendGson(new Message("-1", ERROR, context.getString(R.string.browser_toast_msg_format_wrong)));
            WebkeyApplication.log("WebkeyVisitor", "Message parse error: " + message);
            return;
        }

        switch (msg.type) {
            case AUTH:
                login(msg);
                break;
            case ADMINAUTH:
                adminLogin(msg);
                break;
            case SIGNUP:
                signUp(msg);
                break;
            default:
                handleMessage(msg);
                break;
        }
    }

    private Message parseMsg(String message) throws Exception {
        Message msg = gson.fromJson(message, Message.class);
        if (msg == null || msg.id == null || msg.type == null) {
            throw new Exception();
        }
        return msg;
    }


    private void adminLogin(Message msg) {
        AdminAuthHandler adminAuthHandler = new AdminAuthHandler(context, this, visitorManager);
        adminAuthHandler.onData(msg);
    }

    private void login(Message msg) {
        AuthHandler authHandler = new AuthHandler(context, this, visitorManager);
        authHandler.onData(msg);
    }

    public void authSuccess(BrowserInfo browserInfo, String username) {
        Log.i("WebkeyVisitor","authSuccess");
        this.browserInfo = browserInfo;
        this.username = username;
        loggedin = true;
        setupHandlers();
        visitorManager.visitorLeggedIn(this);
    }

    public synchronized void authNOTSuccess() {
        Log.i("WebkeyVisitor","authNotSuccess");
        loggedin = true;
        setupHandlers();
        //visitorManager.visitorLeggedIn(this);
    }

    private synchronized void setupHandlers() {
        ScreencapHandler sh = new ScreencapHandler(context, this);
        handlers.put(SCREEN_START, sh);
        handlers.put(SCREEN_STOP, sh);
        handlers.put(SCREEN_OPTIONS, sh);
        handlers.put(SCREEN_ACK, sh);

        NotificationsHandler nh = new NotificationsHandler(context, this);
        handlers.put(SCREEN_STOP, nh);

        handlers.put(TOUCH, new TouchHandler(context));
        handlers.put(BUTTON, new ButtonHandler(context));
        handlers.put(KEY, new KeyHandler(context));

        LocationHandler lh = new LocationHandler(context, this);
        handlers.put(LOCATION_START, lh);
        handlers.put(LOCATION_STOP, lh);

        handlers.put(OPENURL, new OpenURLHandler(context));
    }

    private void signUp(Message msg) {
        SignUpHandler signupHandler = new SignUpHandler(context, this);
        signupHandler.onData(msg);
    }

    private void handleMessage(Message msg) {
        Log.i("OUThmTYPE",msg.type.toString());
        if(handlers.containsKey(msg.type)) {
            Log.i("hmTYPE",msg.type.toString());
            List<MessageHandler> handlerList = handlers.get(msg.type);
            for(MessageHandler h : handlerList) {
                h.onData(msg);
            }
        }
    }

    public void onClose() {
        for (MessageHandler handler : new HashSet<>(handlers.values())) {
             handler.onLeftUser(this);
        }

        visitorManager.leftVisitor(this);
        WebkeyApplication.getGoogleAnalitics().BrowserLeft();
    }

    // It called when left all users
    public void cleanHandlers() {
        for (MessageHandler handler : new HashSet<>(handlers.values())) {
            handler.onLeftAllUsers();
        }
    }

    public void requestRestart(){
        sendGson(new Message("1", RESTART, new Payload()));
    }

    public void sendToast(ToastPayload.ToastType type, String e, Boolean sticky) {
        sendGson(new Message("1", TOAST, new ToastPayload(type, e, sticky)));
    }

    public void send(byte[] data) throws IOException {


     /*  if(stompClient==null ){
        stompClient = Stomp.over(org.java_websocket.WebSocket.class,"ws://192.168.0.5:8080/gs-image-websocket/websocket"); //ws://192.168.0.5:8080
           stompClient.connect();
       }else{
           if(stompClient.isConnected()){
               Log.i("WS","CoNECTADO");
           }
       }

        String str3 = Arrays.toString(data);

        stompClient.send("/app/image",str3).doOnError(new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                throwable.printStackTrace(); Log.i("send topic","fallo");
            }
        }).subscribe();*/

        // original code
        Log.i("WebkeyVisitor","send bytes");
        mVisitorChannel.sendMessage(data);


    }

    public void sendGson(Message message) {
        Log.i("WebkeyVisitor","send Message");
        mVisitorChannel.sendMessage(gson.toJson(message)); //comentado antes
    }

    public boolean isLoggedIn() {
        return loggedin;
    }

    public String getUsername() {
        return username;
    }

    public BrowserInfo getBrowserInfo() {
        return browserInfo;
    }

    public void setHarborClient(HarborClient harborClient) {
        this.harborClient = harborClient;
    }

    public HarborClient getHarborClient() {
        return harborClient;
    }

    public int getVisitorID() {
        return this.hashCode();
    }




    public class WsTest extends WebSocketListener{
         byte[] bytes;


        public WsTest( byte[] bytes){
            this.bytes = bytes;
        }
        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            super.onOpen(webSocket, response);

            webSocket.send(bytes.toString());

        }

        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            super.onMessage(webSocket, bytes);
            Log.i("mesng",bytes.toString());
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            super.onFailure(webSocket, t, response);
            Log.i("resp",t.getMessage());
        }
    }


}
