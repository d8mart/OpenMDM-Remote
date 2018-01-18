package com.openmdmremote.net.visitor;


public interface VisitorChangesListener {
    void onLeftLastVisitor();

    void onNewVisitor(String username, BrowserInfo browserInfo);
}
