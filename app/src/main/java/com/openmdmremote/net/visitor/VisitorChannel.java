package com.openmdmremote.net.visitor;

public interface VisitorChannel {
    void sendMessage(byte[] msg);
    void sendMessage(String msg);
}
