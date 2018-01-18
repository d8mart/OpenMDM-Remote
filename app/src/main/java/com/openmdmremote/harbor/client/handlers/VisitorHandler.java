package com.openmdmremote.harbor.client.handlers;

import android.util.Log;

import com.openmdmremote.harbor.HRPCProto;
import com.openmdmremote.harbor.Visitor;
import com.openmdmremote.harbor.VisitorStore;
import com.openmdmremote.harbor.client.HarborClient;
import com.openmdmremote.harbor.interfaces.VisitorFactory;

public class VisitorHandler implements HarborMessageHandler {

    private final VisitorStore visitorStore;
    private final VisitorFactory visitorFactory;
    private final HarborClient harborClient;

    public VisitorHandler(VisitorFactory visitorFactory, HarborClient harborClient) {
        this.visitorFactory = visitorFactory;
        this.harborClient = harborClient;
        visitorStore = new VisitorStore();
    }

    @Override
    public void onMessage(HRPCProto.Message msg) {
        if (msg.getType() == HRPCProto.Message.Type.VISITOR) {
            onVisitorMsg(msg.getVisitor());
        }

        if (msg.getType() == HRPCProto.Message.Type.TRANSPORT) {
            transportMSG(msg.getTransportpkg());
        }
    }

    private void transportMSG(HRPCProto.TransportPKG transportpkg) {
        String connTrackId = transportpkg.getConntrackid();
        String transportMsg = transportpkg.getMsgjson();
        Visitor visitor = visitorStore.getVisitor(connTrackId);
        if(visitor != null) {
            visitor.onMessage(transportMsg);
        }else{ // Si el visitor es nulo tambien ejecuta ( todavia no tenemos implementado el visitor en openmdm )
            newVisitor(connTrackId);
            visitor = visitorStore.getVisitor(connTrackId);
            visitor.onMessage(transportMsg);
        }
    }

    private void onVisitorMsg(HRPCProto.Visitor visitorMsg) {
        HRPCProto.Visitor.Type msgType = visitorMsg.getType();
        String conntrackid = visitorMsg.getConntrackid();

        if (msgType == HRPCProto.Visitor.Type.LEFT) {
            visitorLeft(conntrackid);
        } else if (msgType == HRPCProto.Visitor.Type.NEW) {
            Log.i("VISITORHANDLER","Nuevo visitante entrante :"+conntrackid);
            newVisitor(conntrackid);
        }
    }

    @Override
    public void onClosed() {
        visitorStore.tearDown();
    }

    private void newVisitor(String conntrackid) {
        Visitor visitor = visitorFactory.getPeer(conntrackid, harborClient);
        visitorStore.addNewVisitor(visitor);
    }

    private void visitorLeft(String conntrackid) {
        visitorStore.leftVisitor(conntrackid);
    }
}
