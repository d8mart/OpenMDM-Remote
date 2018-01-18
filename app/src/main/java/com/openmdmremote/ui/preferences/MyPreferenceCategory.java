package com.openmdmremote.ui.preferences;

import android.content.Context;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.widget.TextView;

import com.openmdmremote.R;
import com.openmdmremote.ui.typefaces.FontSetter;

public class MyPreferenceCategory extends PreferenceCategory {
    FontSetter fontSetter;
    Context context;

    public MyPreferenceCategory(Context context) {
        super(context);
        this.context = context;
        fontSetter = new FontSetter(context);
    }

    public MyPreferenceCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        fontSetter = new FontSetter(context);

    }

    public MyPreferenceCategory(Context context, AttributeSet attrs,
                                int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        fontSetter = new FontSetter(context);

    }


    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        TextView titleView = (TextView) holder.findViewById(android.R.id.title);
        fontSetter.updateFontToMedium(titleView);
        titleView.setTextColor(context.getResources().getColor(R.color.colorAccent));
    }
}