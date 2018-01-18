package com.openmdmremote.service.handlers;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.openmdmremote.net.visitor.WebkeyVisitor;
import com.openmdmremote.service.dto.Message;
import com.openmdmremote.service.dto.OpenURLPayload;
import com.openmdmremote.service.handlers.interfaces.MessageHandler;

public class OpenURLHandler implements MessageHandler {
    private final Context context;
    private Gson gson = new GsonBuilder().create();

    String url;

    public OpenURLHandler(Context context) {
        this.context = context;
    }

    @Override
    public void onData(Message msg) {
        OpenURLPayload payload = gson.fromJson(msg.payload, OpenURLPayload.class);
        url = payload.url;
        if(validateAndFixUrl()) {
            openUrl();
        }
    }

    private boolean validateAndFixUrl() {
        if(url.length() <= 0) {
            return false;
        }

        if(url.startsWith("http://")) {
            return true;
        }

        if(url.startsWith("https://")) {
            return true;
        }

        url = "http://"+url;
        return true;
    }

    private void openUrl() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(browserIntent);
    }

    @Override
    public void onLeftUser(WebkeyVisitor webkeyVisitor) {

    }

    @Override
    public void onLeftAllUsers() {

    }
}
