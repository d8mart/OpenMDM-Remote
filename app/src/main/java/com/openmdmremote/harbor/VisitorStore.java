package com.openmdmremote.harbor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class VisitorStore {

    private final HashMap<String, Visitor> peers = new HashMap<>();

    public void addNewVisitor(Visitor visitor) {
        peers.put(visitor.getConntrackId(), visitor);
    }

    public void leftVisitor(String conntrackId) {
        Visitor p = peers.remove(conntrackId);
        if (p != null) {
            p.onClose();
        }
    }

    public void tearDown() {
        Iterator it = peers.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry peers = (Map.Entry) it.next();
            Visitor p = (Visitor) peers.getValue();
            p.onClose();
            it.remove();
        }
    }

    public Visitor getVisitor(String connTrackId) {
        return peers.get(connTrackId);
    }
}
