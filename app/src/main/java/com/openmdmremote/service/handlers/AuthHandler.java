package com.openmdmremote.service.handlers;


import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.openmdmremote.R;
import com.openmdmremote.WebkeyApplication;
import com.openmdmremote.harbor.settings.HarborAuthSettings;
import com.openmdmremote.net.visitor.BrowserInfo;
import com.openmdmremote.net.visitor.VisitorManager;
import com.openmdmremote.net.visitor.WebkeyVisitor;
import com.openmdmremote.service.dto.AuthPayload;
import com.openmdmremote.service.dto.AuthReplyPayload;
import com.openmdmremote.service.dto.Message;
import com.openmdmremote.service.handlers.interfaces.MessageHandler;
import com.openmdmremote.service.services.Credentials;
import com.openmdmremote.service.services.LocalAuthService;

import static com.openmdmremote.service.dto.Message.Type.AUTH;

public class AuthHandler implements MessageHandler {
    private final static String LOGTAG = "AuthHandler";

    private final WebkeyVisitor mWebkeyVisitor;
    private final LocalAuthService localAuthService;
    private final VisitorManager mVisitorManager;
    private Context context;

    private Gson gson = new GsonBuilder().create();
    private BrowserInfo browserInfo;
    private AuthPayload authPayload;


    public AuthHandler(Context context, WebkeyVisitor webkeyVisitor, VisitorManager visitorManager) {
        this.context = context;
        mWebkeyVisitor = webkeyVisitor;
        mVisitorManager = visitorManager;
        localAuthService = new LocalAuthService(this.context);
    }

    @Override
    public void onData(Message msg) {
        authPayload = gson.fromJson(msg.payload, AuthPayload.class);
        if (authPayload == null) {
            return;
        }

        if (!authenticate()) {
            WebkeyApplication.log(LOGTAG, "User "+authPayload.username+" auth failed");
            sendAuthResponse(new AuthReplyPayload(true, "", context.getString(R.string.browser_toast_username_error)));
            return;
        }

        if (!hasSomeoneLoggedIn()) {
            WebkeyApplication.log(LOGTAG, "Try login "+authPayload.username+" but already has been logged in someone else");
            sendAuthResponse(new AuthReplyPayload(true, "", context.getString(R.string.browser_toast_login_failed)));

            WebkeyApplication.getGoogleAnalitics().BrowserLoginInUsed();
            return;
        }
        WebkeyApplication.log(LOGTAG, "Auth success: "+authPayload.username);
        parseBrowserInfo();
        authSuccess();
    }

    @Override
    public void onLeftUser(WebkeyVisitor webkeyVisitor) {
        WebkeyApplication.log(LOGTAG, "Left user: "+webkeyVisitor.getUsername());
    }

    @Override
    public void onLeftAllUsers() {
    }

    private boolean authenticate() {
        Credentials credentials = new Credentials(authPayload.username, authPayload.password);
        if (!localAuthService.authorize(credentials)) {
            WebkeyApplication.getGoogleAnalitics().BrowserLoginFailed();
            return false;
        } else {
            return true;
        }
    }

    private void authSuccess() {
        mWebkeyVisitor.authSuccess(browserInfo, authPayload.username);
        sendAuthResponse(new AuthReplyPayload(false, new HarborAuthSettings(context).getDeviceNickName(), ""));

        WebkeyApplication.getGoogleAnalitics().BrowserLoginSuccess();
    }

    private void parseBrowserInfo() {
        long logintime = System.currentTimeMillis();
        try {
            browserInfo = new BrowserInfo(logintime, authPayload.browseragent, authPayload.platform);
        } catch (Exception e) {
            browserInfo = new BrowserInfo(logintime, "", "");
        }
    }

    private boolean hasSomeoneLoggedIn() {
        return mVisitorManager.hasSomeoneLoggedInAndKick(mWebkeyVisitor);
    }

    private void sendAuthResponse(AuthReplyPayload authReplyPayload) {
        mWebkeyVisitor.sendGson(new Message("1", AUTH, authReplyPayload));
    }
}