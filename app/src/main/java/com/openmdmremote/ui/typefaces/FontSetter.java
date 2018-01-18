package com.openmdmremote.ui.typefaces;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.Button;
import android.widget.TextView;

public class FontSetter {
    private final String FONT_LIGHT = "fonts/TrimMonoWebLight.ttf";
    private final String FONT_BOLD = "fonts/TrimMonoWebBold.ttf";
    private final String FONT_MEDIUM = "fonts/TrimMonoWebMedium.ttf";

    Typeface fontLight;
    Typeface fontBold;
    Typeface fontMedium;

    public FontSetter(Context context) {
        fontLight = Typeface.createFromAsset(context.getAssets(), FONT_LIGHT);
        fontBold = Typeface.createFromAsset(context.getAssets(), FONT_BOLD);
        fontMedium = Typeface.createFromAsset(context.getAssets(), FONT_MEDIUM);
    }

    /*
    private void updateFonts(ViewGroup viewGroup, Typeface ttf) {
        // http://stackoverflow.com/questions/7784418/get-all-child-views-inside-linearlayout-at-once
        int count = viewGroup.getChildCount();
        Log.d("Webkey: ", "count: " + Integer.toString(count));
        for (int i = 0; i < count; i++) {
            View view = viewGroup.getChildAt(i);

            if (view instanceof AppCompatEditText) {
                // Set to fontLight
                ((EditText) view).setTypeface(fontLight);
            } else if (view instanceof TextInputLayout ) {
                // Set to fontMedium.
                setTypefaceToInputLayout((TextInputLayout) view);
            } else if (view instanceof TextView) {
                ((TextView) view).setTypeface(ttf);
            }

            if (view instanceof ViewGroup) {
                updateFonts((ViewGroup) view, ttf);
            }

        }
    }
    */

    /* textView */
    public void updateFontToMedium(TextView textView) {
        textView.setTypeface(fontMedium);
    }

    public void updateFontToLight(TextView textView) {
        textView.setTypeface(fontLight);
    }

    /* Button */
    public void updateFont(Button button) {
        button.setTypeface(fontMedium);
    }

    public Typeface getLight() {
        return fontLight;
    }

    public Typeface getMedium() {
        return fontMedium;
    }
}
