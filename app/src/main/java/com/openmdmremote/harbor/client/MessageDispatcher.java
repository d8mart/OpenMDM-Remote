package com.openmdmremote.harbor.client;

import com.openmdmremote.harbor.HRPCProto;
import com.openmdmremote.harbor.client.handlers.HarborMessageHandler;

import java.util.Map;

public class MessageDispatcher {
    private final Map<HRPCProto.Message.Type, HarborMessageHandler> handlers;

    public MessageDispatcher(Map<HRPCProto.Message.Type, HarborMessageHandler> handlers) {
        this.handlers = handlers;

    }

    public void onMessage(HRPCProto.Message msg) {
        handlers.get(msg.getType()).onMessage(msg);
    }
}
