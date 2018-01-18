package com.openmdmremote.ui.typefaces;

import android.content.Context;
import android.support.design.widget.TextInputLayout;
import android.util.AttributeSet;
import android.widget.TextView;

import java.lang.reflect.Field;

public class MyTextInputLayout extends android.support.design.widget.TextInputLayout {
    FontSetter fontSetter = new FontSetter(getContext());

    public MyTextInputLayout(Context context) {
        super(context);
        setOnwTypeFace();
    }

    public MyTextInputLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnwTypeFace();
    }


    public void setOnwTypeFace() {
        setErrorEnabled(true);
        post(new Runnable() {
            @Override
            public void run() {
                setTypeface(fontSetter.getMedium());
                getEditText().setTypeface(fontSetter.getLight());
            }
        });

        try {

            Field fErrorView = TextInputLayout.class.getDeclaredField("mErrorView");
            fErrorView.setAccessible(true);
            TextView mErrorView = (TextView) fErrorView.get(this);
            mErrorView.setTypeface(fontSetter.getLight());
            mErrorView.requestLayout();

        } catch (Exception ignored) {
        }
    }
}
