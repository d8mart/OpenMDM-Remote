package com.openmdmremote.ui.typefaces;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.TextView;

import com.openmdmremote.R;

public class MyTextView extends TextView {

    boolean bold = true;

    public MyTextView(Context context) {
        super(context);
        setFont();
    }

    public MyTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray flagAttr = context.obtainStyledAttributes(attrs, R.styleable.FontType);
        bold = flagAttr.getBoolean(R.styleable.FontType_bold, bold);
        setFont();
        flagAttr.recycle();
    }

    public MyTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray flagAttr = context.obtainStyledAttributes(attrs, R.styleable.FontType);
        bold = flagAttr.getBoolean(R.styleable.FontType_bold, bold);
        setFont();
        flagAttr.recycle();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MyTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray flagAttr = context.obtainStyledAttributes(attrs, R.styleable.FontType);
        bold = flagAttr.getBoolean(R.styleable.FontType_bold, bold);
        setFont();
        flagAttr.recycle();
    }

    private void setFont() {
        FontSetter fontSetter = new FontSetter(this.getContext());
        if (bold) {
            fontSetter.updateFontToMedium(this);
        } else {
            fontSetter.updateFontToLight(this);

        }
    }
}
