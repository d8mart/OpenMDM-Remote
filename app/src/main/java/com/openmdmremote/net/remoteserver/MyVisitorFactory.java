package com.openmdmremote.net.remoteserver;

import com.openmdmremote.harbor.Visitor;
import com.openmdmremote.harbor.client.HarborClient;
import com.openmdmremote.harbor.interfaces.VisitorFactory;
import com.openmdmremote.net.visitor.VisitorManager;

public class MyVisitorFactory implements VisitorFactory {
    private final VisitorManager visitorManager;

    public MyVisitorFactory(VisitorManager visitorManager) {
        this.visitorManager = visitorManager;
    }

    @Override
    public Visitor getPeer(String conntrackId, HarborClient harborClient) {
        return new MyVisitor(conntrackId, harborClient, visitorManager);
    }
}
