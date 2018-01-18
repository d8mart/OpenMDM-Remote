package com.openmdmremote.ui.typefaces;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class MyArrayAdapter extends ArrayAdapter {
    FontSetter fontSetter;

    public MyArrayAdapter(Context context, int simple_list_item_1, int text1, List object) {
        super(context, simple_list_item_1, text1, object);
        fontSetter = new FontSetter(context);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = super.getView(position, convertView, parent);

        if (convertView == null) {
            // Remove the padding.
            ((TextView) v).setPadding(0, 0, 0, 0);

            // Set type.
            fontSetter.updateFontToLight((TextView) v);
        }
        return v;
    }
}
