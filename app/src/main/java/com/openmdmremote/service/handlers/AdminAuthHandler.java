package com.openmdmremote.service.handlers;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.openmdmremote.R;
import com.openmdmremote.WebkeyApplication;
import com.openmdmremote.harbor.client.HarborClient;
import com.openmdmremote.harbor.interfaces.OnAdminAuthListener;
import com.openmdmremote.harbor.settings.HarborAuthSettings;
import com.openmdmremote.net.visitor.BrowserInfo;
import com.openmdmremote.net.visitor.VisitorManager;
import com.openmdmremote.net.visitor.WebkeyVisitor;
import com.openmdmremote.service.dto.AdminAuthPayload;
import com.openmdmremote.service.dto.AuthReplyPayload;
import com.openmdmremote.service.dto.Message;
import com.openmdmremote.service.handlers.interfaces.MessageHandler;

import static com.openmdmremote.service.dto.Message.Type.AUTH;

public class AdminAuthHandler implements MessageHandler {

    private final static String LOGTAG = "AdminAuthHandler";

    private final Context context;

    private final WebkeyVisitor webkeyVisitor;
    private final VisitorManager visitorManager;


    private Gson gson = new GsonBuilder().create();
    private BrowserInfo browserInfo;
    private AdminAuthPayload adminAuthPayload;
    private final String adminNick;


    public AdminAuthHandler(Context context, WebkeyVisitor webkeyVisitor, VisitorManager visitorManager) {
        this.context = context;
        this.webkeyVisitor = webkeyVisitor;
        this.visitorManager = visitorManager;

        // Determine the admin's nick.
        HarborAuthSettings harborHarborAuthSettings = new HarborAuthSettings(context);
        adminNick = harborHarborAuthSettings.getAccountName();

    }

    @Override
    public void onData(Message msg) {
        WebkeyApplication.log(LOGTAG, "received admin token");

        adminAuthPayload = gson.fromJson(msg.payload, AdminAuthPayload.class);

        if (adminAuthPayload == null) {
            return;
        }

        parseBrowserInfo();
        sendAuthRequestToHarbor(adminAuthPayload.token);
    }

    private void sendAuthRequestToHarbor(String session) {
        HarborClient harborClient = webkeyVisitor.getHarborClient();
        //authNOTSuccess();
        if (harborClient == null) {
            loginFailed();
            return;
        }

        harborClient.sendRemoteAuthRequestToHarbor(session, new OnAdminAuthListener() {
            @Override
            public void onAuthSucess() {
                if (hasSomeoneLoggedInAndKick()) {
                    authSuccess();
                } else {
                    loginFailed();
                   // authNOTSuccess();
                }
            }

            @Override
            public void onAuthFailed() {
                loginFailed();
            }
        });

    }

    private void loginFailed() {
        sendAuthResponse(new AuthReplyPayload(true, "", context.getString(R.string.browser_toast_login_failed)));
        WebkeyApplication.getGoogleAnalitics().BrowserLoginAdminFailed();
    }

    @Override
    public void onLeftUser(WebkeyVisitor webkeyVisitor) {
    }

    @Override
    public void onLeftAllUsers() {
    }

    private void authSuccess() {
        webkeyVisitor.authSuccess(browserInfo, adminNick);

        sendAuthResponse(new AuthReplyPayload(false, new HarborAuthSettings(context).getDeviceNickName(), ""));

        WebkeyApplication.getGoogleAnalitics().BrowserLoginAdminSuccess();
    }

    private void authNOTSuccess() {
        webkeyVisitor.authNOTSuccess();
    }

    private void parseBrowserInfo() {
        long logintime = System.currentTimeMillis();
        try {
            browserInfo = new BrowserInfo(logintime, adminAuthPayload.browseragent, adminAuthPayload.platform);
        } catch (Exception e) {
            browserInfo = new BrowserInfo(logintime, "", "");
        }
    }

    private boolean hasSomeoneLoggedInAndKick() {
        return visitorManager.hasSomeoneLoggedInAndKick(webkeyVisitor);
    }

    private void sendAuthResponse(AuthReplyPayload authReplyPayload) {
        webkeyVisitor.sendGson(new Message("1", AUTH, authReplyPayload));
    }
}