package com.openmdmremote.ui.main;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.openmdmremote.R;
import com.openmdmremote.service.services.LocalAuthService;
import com.openmdmremote.ui.typefaces.FontSetter;
import com.openmdmremote.ui.typefaces.MyTextInputLayout;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserAddDialog extends DialogFragment {

    private Context context;
    private AlertDialog.Builder alertDialgBuilder;
    private FontSetter fontSetter;
    private DialogInterface.OnDismissListener onDismissListener;
    private MyTextInputLayout username;
    private MyTextInputLayout pwd;
    private MyTextInputLayout pwdAgain;

    public UserAddDialog() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        alertDialgBuilder = new AlertDialog.Builder(context);
        fontSetter = new FontSetter(context);
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_adduser, null);
        username = (MyTextInputLayout) view.findViewById(R.id.inputlayout_add_username);
        pwd = (MyTextInputLayout) view.findViewById(R.id.inputlayout_add_pwd);
        pwdAgain = (MyTextInputLayout) view.findViewById(R.id.inputlayout_add_pwdagain);

        // For the pwd comparator.
        pwd.getEditText().setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    comparePasswords();
                }
            }
        });

        pwdAgain.getEditText().setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    comparePasswords();
                }
            }
        });

        alertDialgBuilder.setView(view);
        alertDialgBuilder.setPositiveButton(R.string.adduser_confirm, null);
        alertDialgBuilder.setNegativeButton(getResources().getString(R.string.adduser_deny), null);

        final AlertDialog alertDialog = (AlertDialog) alertDialgBuilder.create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button negative = ((AlertDialog) dialogInterface).getButton(DialogInterface.BUTTON_NEGATIVE);
                Button positive = ((AlertDialog) dialogInterface).getButton(DialogInterface.BUTTON_POSITIVE);
                positive.setTextColor(getResources().getColor(R.color.colorAccent));
                negative.setTextColor(getResources().getColor(R.color.colorAccent));
                fontSetter.updateFont(negative);
                fontSetter.updateFont(positive);

                positive.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (validate()) {
                            String u = username.getEditText().getText().toString();
                            String pw = pwd.getEditText().getText().toString();
                            if (addNewUser(u, pw)) {
                                dismiss();
                            }
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
        if (!validateNick()) {
            valid = false;
        }

        // Pwd check.
        if (!validatePassword()) {
            valid = false;
        }

        // Pwd mach check.
        if (!comparePasswords()) {
            valid = false;
        }

        return valid;
    }

    private boolean validateNick() {
        Pattern pattern = Pattern.compile("^[0-9a-zA-Z\\-\\.@].*$");
        Matcher matcher = pattern.matcher(username.getEditText().getText().toString());
        if (!matcher.find()) {
            username.setError(getResources().getString(R.string.adduser_txt_error_nick));
            username.setOnwTypeFace();
            return false;
        } else {
            username.setError(null);
            username.setErrorEnabled(false);
            return true;
        }
    }

    private boolean validatePassword() {
        String pw1 = pwd.getEditText().getText().toString();
        if (pw1.isEmpty() || pw1.length() < 5) {
            pwd.setError(getResources().getString(R.string.adduser_txt_error_pwd));
            pwd.setOnwTypeFace();
            return false;
        } else {
            pwd.setError(null);
            pwd.setErrorEnabled(false);
            return true;
        }
    }

    private boolean comparePasswords() {
        String pwd1 = pwd.getEditText().getText().toString();
        String pwd2 = pwdAgain.getEditText().getText().toString();
        if (pwd1.length() > 0 && pwd2.length() > 0) {
            if (pwd1.equals(pwd2)) {
                pwdAgain.setError(null);
                pwdAgain.setErrorEnabled(false);
                return true;
            } else {
                pwdAgain.setError(getResources().getString(R.string.adduser_txt_error_pwdagain));
                pwdAgain.setOnwTypeFace();
                return false;
            }
        }
        return false;
    }

    private boolean addNewUser(String username, String password) {
        LocalAuthService a = new LocalAuthService(context);
        if (a.signUpLocalUser(username, password)) {
            Toast.makeText(context, R.string.adduser_success, Toast.LENGTH_SHORT).show();
            return true;
        } else {
            this.username.setError(getResources().getString(R.string.adduser_fail));
            this.username.setOnwTypeFace();
            return false;
        }
    }
}
