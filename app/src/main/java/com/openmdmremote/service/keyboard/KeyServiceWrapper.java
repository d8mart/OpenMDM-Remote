package com.openmdmremote.service.keyboard;

import com.openmdmremote.service.handlers.interfaces.KeyServiceListener;

public class KeyServiceWrapper {

    private static KeyinjectService keyinjectService;
    private static KeyServiceListener listener;


    public void setKeyinjectServiceInstance(KeyinjectService kis) {
        keyinjectService = kis;
        if(listener != null) {
            if(kis == null) {
                listener.onKeyboardDisabled();
            } else {
                listener.onKeyboardEnabled();
            }
        }
    }

    public void setListener(KeyServiceListener listener) {
        this.listener = listener;
    }

    public KeyinjectService getKeyinjectServiceInstance() {
        return keyinjectService;
    }
}
