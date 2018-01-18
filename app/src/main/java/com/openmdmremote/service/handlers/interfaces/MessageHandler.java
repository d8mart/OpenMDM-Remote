package com.openmdmremote.service.handlers.interfaces;

import com.openmdmremote.net.visitor.WebkeyVisitor;
import com.openmdmremote.service.dto.Message;

public interface MessageHandler {
    public void onData(Message payload);
    public void onLeftUser(WebkeyVisitor webkeyVisitor);
    public void onLeftAllUsers();
}
