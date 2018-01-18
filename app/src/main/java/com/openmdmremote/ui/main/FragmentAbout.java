package com.openmdmremote.ui.main;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.openmdmremote.BuildConfig;
import com.openmdmremote.R;


public class FragmentAbout extends Fragment {
    public static final String FRAGMENT_TAG = "aboutfragment";

    View view;
    PackageInfo pInfo;

    public FragmentAbout() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_about, container, false);

        Context context = view.getContext();
        try {
            pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {}

        ((Button) view.findViewById(R.id.btn_donation)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.webkey.cc/html/donate.html"));
                startActivity(browserIntent);
            }
        });

        ((TextView) view.findViewById(R.id.txt_version)).setText(
                getResources().getString(R.string.txt_version)+pInfo.versionName);
        ((TextView) view.findViewById(R.id.txt_vcode)).setText(
                getResources().getString(R.string.txt_vcode)+Integer.toString(pInfo.versionCode));

        if(!BuildConfig.DEBUG_FEATURE) {
            ((TextView) view.findViewById(R.id.txt_vtype)).setText(
                    getResources().getString(R.string.txt_type)+
                            getResources().getString(R.string.txt_release));
        } else {
            ((TextView) view.findViewById(R.id.txt_vtype)).setText(
                    getResources().getString(R.string.txt_type)+
                            getResources().getString(R.string.txt_debug));
        }

        TextView tv = (TextView) view.findViewById(R.id.txt_terms);
        tv.setMovementMethod(new ScrollingMovementMethod());

        return view;
    }
}
