package com.openmdmremote.service.handlers;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.openmdmremote.net.visitor.WebkeyVisitor;
import com.openmdmremote.service.dto.Message;
import com.openmdmremote.service.dto.TouchEvent;
import com.openmdmremote.service.dto.TouchPayload;
import com.openmdmremote.nativ.WIPC;
import com.openmdmremote.nativ.net.WIPCProto;
import com.openmdmremote.service.handlers.interfaces.MessageHandler;

public class TouchHandler implements MessageHandler {
    WIPC wipc;
    private Gson gson = new GsonBuilder().create();
    int w;
    int h;
    double reosultion = 65535.0;

    public TouchHandler(Context mContext) {
        wipc = WIPC.getInstance(mContext);
        DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
        w = metrics.widthPixels;
        h = metrics.heightPixels;
    }

    @Override
    public void onData(Message msg) {
        Log.i("onDATA","TOUCHH");
        TouchPayload payload = gson.fromJson(msg.payload, TouchPayload.class);

        WIPCProto.Message.Builder bmsg = WIPCProto.Message.newBuilder();
        bmsg.setType(WIPCProto.Message.Type.TOUCH);
        for(TouchEvent e : payload.events) {
            WIPCProto.Touch.Type ty = null;
            switch (e.type){
                case UP:
                    Log.i("onDATA","UP");

                    ty = WIPCProto.Touch.Type.UP;
                    break;
                case MOVE:
                    ty = WIPCProto.Touch.Type.MOVE;
                    break;
                case DOWN:
                    ty = WIPCProto.Touch.Type.DOWN;
                    break;
            }

            /*
             * If not protect here then will be
             * uint owerflow. So the negativ numbers fill
             * be the maxmimum values.
             */
            if(e.x < 0 ) {
                e.x = 0;
            } else if (e.x > 1) {
                e.x = 1.0;
            }

            if(e.y < 0 ) {
                e.y = 0;
            } else if (e.y > 1) {
                e.y = 1.0;
            }

            WIPCProto.Touch t = WIPCProto.Touch.newBuilder()
                    .setWidth((int) (e.x*reosultion))
                    .setHeight((int) (e.y*reosultion))
                    .setType(ty)
                    .setMirror(e.mirror)
                    .setFlip(e.flip)
                    .build();
            bmsg.addTouch(t);
        }

        wipc.sendMsg(bmsg.build());
    }

    @Override
    public void onLeftUser(WebkeyVisitor webkeyVisitor) {

    }

    @Override
    public void onLeftAllUsers() {

    }
}
