package com.openmdmremote.service.keyboard;

import android.inputmethodservice.InputMethodService;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import com.openmdmremote.R;
import com.openmdmremote.WebkeyApplication;

public class KeyinjectService extends InputMethodService {

    KeyServiceWrapper keyServiceWrapper;

    @Override
    public void onCreate(){
        super.onCreate();
        keyServiceWrapper = new KeyServiceWrapper();
    }

    @Override
    public View onCreateInputView() {
        View mInputView =  (View) getLayoutInflater().inflate(R.layout.keyboard, null);
        return mInputView;
    }

    @Override
    public void onStartInput(EditorInfo attribute, boolean restarting) {
        super.onStartInput(attribute, restarting);
        keyServiceWrapper.setKeyinjectServiceInstance(this);
    }

    @Override
    public void onFinishInput() {
        super.onFinishInput();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        WebkeyApplication.log("KeyInjectService", "on Destroys");
        keyServiceWrapper.setKeyinjectServiceInstance(null);
    }

    public void commitText(String text){
        InputConnection ic = getCurrentInputConnection();
        ic.commitText(text, 1);
    }
}
