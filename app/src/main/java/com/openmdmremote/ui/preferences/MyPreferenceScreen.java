package com.openmdmremote.ui.preferences;

import android.content.Context;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.widget.TextView;

import com.openmdmremote.ui.typefaces.FontSetter;

public class MyPreferenceScreen extends PreferenceGroup {
    FontSetter fontSetter;

    public MyPreferenceScreen(Context context, AttributeSet attrs) {
        super(context, attrs, android.support.v7.preference.R.attr.preferenceScreenStyle);
        fontSetter = new FontSetter(context);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        TextView titleView = (TextView) holder.findViewById(android.R.id.title);
        TextView summaryView = (TextView) holder.findViewById(android.R.id.summary);
        fontSetter.updateFontToMedium(titleView);
        fontSetter.updateFontToMedium(summaryView);
    }
}
