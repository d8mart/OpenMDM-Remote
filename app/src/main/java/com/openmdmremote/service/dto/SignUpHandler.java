package com.openmdmremote.service.dto;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.openmdmremote.R;
import com.openmdmremote.net.visitor.WebkeyVisitor;
import com.openmdmremote.service.handlers.interfaces.MessageHandler;
import com.openmdmremote.service.services.LocalAuthService;
import com.openmdmremote.service.services.Credentials;

import static com.openmdmremote.service.dto.Message.Type.ERROR;
import static com.openmdmremote.service.dto.Message.Type.SIGNUP;

public class SignUpHandler implements MessageHandler {

    private final WebkeyVisitor mWebkeyVisitor;
    private final LocalAuthService localAuthService;
    Context mContext;

    private Gson gson = new GsonBuilder().create();


    public SignUpHandler(Context context, WebkeyVisitor webkeyVisitor) {
        mContext = context;
        mWebkeyVisitor = webkeyVisitor;
        localAuthService = new LocalAuthService(mContext);
    }

    @Override
    public void onData(Message msg) {
        AuthPayload authPayload = gson.fromJson(msg.payload, AuthPayload.class);
        if (authPayload == null) {
            return;
        }

        Credentials credentials = new Credentials(authPayload.username, authPayload.password);

        if (localAuthService.signUpFromBrowser(credentials)) {
            Message signUpInitiated = new Message("1", SIGNUP, "");
            mWebkeyVisitor.sendGson(signUpInitiated);
        } else {
            mWebkeyVisitor.sendGson(new Message("-1", ERROR, mContext.getString(R.string.browser_toast_user_already_exist)));
        }
    }

    @Override
    public void onLeftUser(WebkeyVisitor webkeyVisitor) {
    }

    @Override
    public void onLeftAllUsers() {
    }
}