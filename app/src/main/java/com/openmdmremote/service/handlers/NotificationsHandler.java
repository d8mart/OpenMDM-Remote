package com.openmdmremote.service.handlers;

import android.content.Context;

import com.openmdmremote.nativ.handlers.Notifications;
import com.openmdmremote.net.visitor.WebkeyVisitor;
import com.openmdmremote.service.dto.Message;
import com.openmdmremote.service.handlers.interfaces.INotifiactions;
import com.openmdmremote.service.handlers.interfaces.MessageHandler;

public class NotificationsHandler implements MessageHandler {
    INotifiactions notifications;

    public NotificationsHandler(Context context, WebkeyVisitor webkeyVisitor) {
        notifications = new Notifications(context, webkeyVisitor);
    }

    @Override
    public void onData(Message payload) {

    }

    @Override
    public void onLeftUser(WebkeyVisitor webkeyVisitor) {
        notifications.leftUser();
    }

    @Override
    public void onLeftAllUsers() {
    }
}
