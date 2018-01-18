package com.openmdmremote.ui.preferences;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.widget.TextView;

import com.openmdmremote.ui.typefaces.FontSetter;

public class MyCheckBoxPreference extends CheckBoxPreference {
    FontSetter fontSetter;

    public MyCheckBoxPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        fontSetter = new FontSetter(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MyCheckBoxPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        fontSetter = new FontSetter(context);
    }

    public MyCheckBoxPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        fontSetter = new FontSetter(context);
    }

    public MyCheckBoxPreference(Context context) {
        super(context);
        fontSetter = new FontSetter(context);
    }


    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        TextView titleView = (TextView) holder.findViewById(android.R.id.title);
        TextView txtSum = (TextView) holder.findViewById(android.R.id.summary);
        fontSetter.updateFontToMedium(titleView);
        fontSetter.updateFontToMedium(txtSum);
    }
}
