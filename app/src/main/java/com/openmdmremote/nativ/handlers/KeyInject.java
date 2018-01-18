package com.openmdmremote.nativ.handlers;

import android.content.Context;
import android.provider.Settings;

import com.openmdmremote.nativ.WIPC;
import com.openmdmremote.nativ.net.WIPCProto;
import com.openmdmremote.service.handlers.interfaces.IKeyInject;

public class KeyInject implements IKeyInject {
    private WIPC wipc;
    private Context mContext;

    private final String DEFAULT_IME = "com.android.inputmethod.latin/.LatinIME";
    private String prevInputMethod = "";

    public KeyInject(Context context) {
        wipc = WIPC.getInstance(context);
        mContext = context;
    }

    private void determineCurrentInputMethod() {
        String default_ime = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
        if(default_ime == null) {
            prevInputMethod = DEFAULT_IME;
        } else {
            prevInputMethod = default_ime;
        }
    }

    @Override
    public void enable() {
        determineCurrentInputMethod();

        WIPCProto.Message.Builder bmsg = WIPCProto.Message.newBuilder();
        bmsg.setType(WIPCProto.Message.Type.KEY);

        WIPCProto.Key key = WIPCProto.Key.newBuilder()
                .setType(WIPCProto.Key.Type.ENABLE)
                .setDefaultInputMethod(prevInputMethod)
                .build();

        bmsg.setKey(key);

        wipc.sendMsg(bmsg.build());
    }

    @Override
    public void disable() {
        WIPCProto.Message.Builder bmsg = WIPCProto.Message.newBuilder();
        bmsg.setType(WIPCProto.Message.Type.KEY);

        WIPCProto.Key key = WIPCProto.Key.newBuilder()
                .setType(WIPCProto.Key.Type.DISABLE)
                .build();

        bmsg.setKey(key);

        wipc.sendMsg(bmsg.build());
    }
}
