package com.openmdmremote.harbor;

import com.openmdmremote.harbor.client.HarborClient;
import com.openmdmremote.harbor.client.HarborMessage;

public class Visitor {

    private HarborClient harborClient;
    private String conntrackId;

    public Visitor(String conntrackId, HarborClient harborClient) {
        this.conntrackId = conntrackId;
        this.harborClient = harborClient;
    }

    public void onMessage(String msgjson) {
    }

    public void onClose() {
    }

    public void send(byte[] data) {
        harborClient.transportMessage(new HarborMessage(conntrackId, data));
    }

    public void send(String msg) {
        harborClient.transportMessage(new HarborMessage(conntrackId, msg));
    }

    public String getConntrackId() {
        return conntrackId;
    }
}