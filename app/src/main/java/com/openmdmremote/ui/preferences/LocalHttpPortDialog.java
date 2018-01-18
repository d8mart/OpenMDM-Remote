package com.openmdmremote.ui.preferences;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.openmdmremote.R;
import com.openmdmremote.service.services.Settings;
import com.openmdmremote.ui.typefaces.FontSetter;
import com.openmdmremote.ui.typefaces.MyTextInputLayout;

public class LocalHttpPortDialog extends DialogFragment {

    private Context context;
    private Settings settings;
    private AlertDialog.Builder alertDialgBuilder;
    private FontSetter fontSetter;
    private DialogInterface.OnDismissListener onDismissListener;
    private MyTextInputLayout httpPort;

    private int ihttpPort;

    public LocalHttpPortDialog() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        settings = new Settings(context);
        alertDialgBuilder = new AlertDialog.Builder(context);
        fontSetter = new FontSetter(context);
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_http_port, null);

        httpPort = (MyTextInputLayout) view.findViewById(R.id.inputlayout_http_port);
        httpPort.getEditText().setText(Integer.toString(settings.getHttpPort()));

        alertDialgBuilder.setView(view);
        alertDialgBuilder.setPositiveButton(R.string.adduser_confirm, null);
        alertDialgBuilder.setNegativeButton(getResources().getString(R.string.adduser_deny), null);

        final AlertDialog alertDialog = (AlertDialog) alertDialgBuilder.create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button negative = ((AlertDialog) dialogInterface).getButton(DialogInterface.BUTTON_NEGATIVE);
                Button positive = ((AlertDialog) dialogInterface).getButton(DialogInterface.BUTTON_POSITIVE);
                positive.setTextColor(getResources().getColor(R.color.colorPrimary));
                negative.setTextColor(getResources().getColor(R.color.colorPrimary));
                fontSetter.updateFont(negative);
                fontSetter.updateFont(positive);

                positive.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (validate()) {
                            settings.setHttpPort(ihttpPort);
                            dismiss();
                        }
                    }
                });
            }
        });
        return alertDialog;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (onDismissListener != null) {
            onDismissListener.onDismiss(dialog);
        }
    }

    public void setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
    }

    private boolean validate() {
        boolean valid = true;

        // Nick check.
        String port = httpPort.getEditText().getText().toString();
        ihttpPort = Integer.parseInt(port);
        if (ihttpPort < 0 || ihttpPort > 65535) {
            valid = false;
        }

        if (ihttpPort == settings.getWSport()) {
            valid = false;
        }
        return valid;
    }
}
