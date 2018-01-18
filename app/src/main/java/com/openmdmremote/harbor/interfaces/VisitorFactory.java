package com.openmdmremote.harbor.interfaces;

import com.openmdmremote.harbor.Visitor;
import com.openmdmremote.harbor.client.HarborClient;

public interface VisitorFactory {
    Visitor getPeer(String conntrackId, HarborClient harborClient);
}
