package com.openmdmremote.harbor.client.handlers;

import com.openmdmremote.harbor.HRPCProto;

public interface HarborMessageHandler {
    void onMessage(HRPCProto.Message msg);

    void onClosed();
}
