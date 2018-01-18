package com.openmdmremote.harbor.client.websocket;

import com.openmdmremote.harbor.HRPCProto;

public interface OnWebSocketEventListener {
    void onOpen();
    void onMessage(HRPCProto.Message msg);
    void onClose();
}
