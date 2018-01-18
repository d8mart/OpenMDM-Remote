package com.openmdmremote.service.handlers;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.openmdmremote.nativ.handlers.KeyInject;
import com.openmdmremote.net.visitor.WebkeyVisitor;
import com.openmdmremote.service.dto.KeyEventPayload;
import com.openmdmremote.service.dto.Message;
import com.openmdmremote.service.handlers.interfaces.IKeyInject;
import com.openmdmremote.service.handlers.interfaces.KeyServiceListener;
import com.openmdmremote.service.handlers.interfaces.MessageHandler;
import com.openmdmremote.service.keyboard.KeyServiceWrapper;
import com.openmdmremote.service.keyboard.KeyinjectService;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class KeyHandler implements MessageHandler, KeyServiceListener {

    Context mContext;

    private Gson gson = new GsonBuilder().create();

    IKeyInject keyInjectEnabler;

    KeyServiceWrapper keyServiceWrapper;
    KeyinjectService mKeyInjectService;

    private BlockingQueue<Message> keyEvents = new ArrayBlockingQueue<Message>(20);

    /*
        Indicate that the native procedure already has been called
        The native code is blocking while the enabe/disable is in
        progress (it is running long time more or less) so this
        function should call only once in one time. Should fix this
        in native code...
     */
    private boolean inProgress = false;

    public KeyHandler(Context context) {
        mContext = context;
        keyInjectEnabler = new KeyInject(context);

        // This class a wrapper for the keyboard service.
        keyServiceWrapper = new KeyServiceWrapper();
        keyServiceWrapper.setListener(this);
    }

    @Override
    public void onData(Message msg) {
        if(mKeyInjectService == null) {
            inProgress = true;
            keyInjectEnabler.enable();

            try {
                keyEvents.add(msg);
            } catch(IllegalStateException e){
                // The queue is full.
            }
            return;
        }

        inject(msg);
    }

    private void inject(Message msg) {
        if(msg == null || mKeyInjectService == null) {
            return;
        }

        KeyEventPayload event = gson.fromJson(msg.payload, KeyEventPayload.class);
        if(event.code.length() == 1) {
            mKeyInjectService.commitText(event.code);

        }

    }

    @Override
    public void onLeftUser(WebkeyVisitor webkeyVisitor) {
    }

    @Override
    public void onLeftAllUsers() {
        keyServiceWrapper.setListener(null);
        mKeyInjectService = null;
        keyInjectEnabler.disable();
    }

    @Override
    public void onKeyboardEnabled() {
        inProgress = false;
        mKeyInjectService = keyServiceWrapper.getKeyinjectServiceInstance();
        while(keyEvents.size() > 0) {
            inject(keyEvents.poll());
        }
    }

    @Override
    public void onKeyboardDisabled() {
        keyServiceWrapper.setListener(null);
        mKeyInjectService = null;
        keyEvents.clear();
    }
}
