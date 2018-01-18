package com.openmdmremote.ui.typefaces;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.Button;

public class MyButton extends Button {
    public MyButton(Context context) {
        super(context);
        setFont();
    }

    public MyButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFont();
    }

    public MyButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setFont();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MyButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setFont();
    }

    private void setFont() {
        FontSetter fontSetter = new FontSetter(this.getContext());
        fontSetter.updateFont(this);
    }
}
