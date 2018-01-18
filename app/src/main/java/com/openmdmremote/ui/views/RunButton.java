package com.openmdmremote.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageButton;

import com.openmdmremote.R;

public class RunButton extends ImageButton {

    public RunButton(Context context) {
        super(context);
        setPengingState();
    }

    public RunButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setPengingState();
    }

    public void setOnState() {
        this.setImageResource(R.drawable.switch_on);
        this.setClickable(true);
    }

    public void setOffState() {
        this.setImageResource(R.drawable.switch_off);
        this.setClickable(true);
    }

    public void setPengingState() {
        this.setImageResource(R.drawable.switch_loading);
        this.setClickable(false);
    }
}
