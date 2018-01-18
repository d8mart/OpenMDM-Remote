package com.openmdmremote.ui.registration.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.openmdmremote.R;
import com.openmdmremote.harbor.account.http.ForgotPassword;
import com.openmdmremote.ui.registration.RegistrationUtil;
import com.openmdmremote.ui.typefaces.MyTextInputLayout;
import com.openmdmremote.ui.typefaces.MyTextView;

import org.json.JSONException;

import static com.openmdmremote.ui.registration.fragments.Utile.figureOutEmail;
import static com.openmdmremote.ui.registration.fragments.Utile.validateEmailAddress;

public class ResetPasswordFragment extends Fragment implements ForgotPassword.OnForgotPasswordResult {
    public static final String FRAGMENT_TAG = "resetpwdfragment";

    private View view;
    private RegistrationUtil registrationUtil;

    private MyTextView msg;
    private MyTextInputLayout email;
    private String emailAddress;

    private ForgotPassword forgotPassword;

    public ResetPasswordFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_reset_password, container, false);

        forgotPassword = new ForgotPassword(getActivity());

        msg = (MyTextView) view.findViewById(R.id.response_msg);

        email = (MyTextInputLayout) view.findViewById(R.id.inputlayout_resetaddress);
        email.getEditText().setText(figureOutEmail(getActivity()));

        email.getEditText().addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence text, int start, int count, int after) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                email.setError(null);
                email.setErrorEnabled(false);
            }
        });

        view.findViewById(R.id.btn_rest_pwd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validate()) {
                    sendReset();
                }
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof RegistrationUtil) {
            registrationUtil = (RegistrationUtil) context;
        }
    }

    @Override
    public void onResult(ForgotPassword.Results res) {
        switch (res) {
            case OK:
                showMsg(R.string.resetpwd_msg_ok);
                break;
            case ERROR:
                showMsg(R.string.resetpwd_error_internal);
                break;
        }
    }

    private void showMsg(final int res) {
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                msg.setText(getResources().getString(res));
                dismissProgress();
            }
        }, 500);
    }

    private void showInvalidEmail() {
        email.setError(getResources().getString(R.string.resetpwd_error_invalid_address));
        email.setOnwTypeFace();
    }

    private void sendReset() {
        showProgress();
        try {
            forgotPassword.sendRequest(this, emailAddress);
        } catch (JSONException e) {
            dismissProgress();
        }
    }

    private void showProgress() {
        registrationUtil.onProgress(view);
    }

    private void dismissProgress() {
        registrationUtil.onDismiss(view);
    }

    private boolean validate() {
        boolean valid = true;
        if (!validateEmail()) {
            valid = false;
            showInvalidEmail();
        }

        emailAddress = email.getEditText().getText().toString();
        return valid;
    }

    private boolean validateEmail() {
        if (!validateEmailAddress(email.getEditText().getText().toString())) {
            email.setError(getResources().getString(R.string.login_error_email));
            email.setOnwTypeFace();
            return false;
        } else {
            email.setError(null);
            email.setErrorEnabled(false);
            return true;
        }
    }
}
