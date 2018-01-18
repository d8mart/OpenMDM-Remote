package com.openmdmremote.service.handlers;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.openmdmremote.nativ.WIPC;
import com.openmdmremote.nativ.net.WIPCProto;
import com.openmdmremote.net.visitor.WebkeyVisitor;
import com.openmdmremote.service.dto.ButtonEventPayload;
import com.openmdmremote.service.dto.Message;
import com.openmdmremote.service.handlers.interfaces.MessageHandler;

import static com.openmdmremote.service.dto.WebkeyButtons.getAndroidButtonCode;

public class ButtonHandler implements MessageHandler {
    WIPC wipc;

    Context mContext;

    private Gson gson = new GsonBuilder().create();


    public ButtonHandler(Context context) {
        mContext = context;
        wipc = WIPC.getInstance(mContext);

    }

    @Override
    public void onData(Message msg) {
        ButtonEventPayload payload = gson.fromJson(msg.payload, ButtonEventPayload.class);
        WIPCProto.Message.Builder bmsg = WIPCProto.Message.newBuilder();
        bmsg.setType(WIPCProto.Message.Type.BUTTON);

        WIPCProto.Button.Type ty = WIPCProto.Button.Type.UP;
        switch (payload.type){
            case UP:
                ty = WIPCProto.Button.Type.UP;
                break;
            case DOWN:
                ty = WIPCProto.Button.Type.DOWN;
                break;
            case LONGPRESS:
                ty = WIPCProto.Button.Type.LONGPRESS;
                break;
        }

        /*
        A backend csak UP-ot kezel
        */
        if(ty == WIPCProto.Button.Type.DOWN) {
            return;
        }

        WIPCProto.Button b = WIPCProto.Button.newBuilder()
                .setType(ty)
                .setButtonid(getAndroidButtonCode(payload.code))
                .build();
        bmsg.setButton(b);

        wipc.sendMsg(bmsg.build());
    }

    @Override
    public void onLeftUser(WebkeyVisitor webkeyVisitor) {
    }

    @Override
    public void onLeftAllUsers() {
    }
}
