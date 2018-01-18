package com.openmdmremote.ui.registration.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.openmdmremote.R;
import com.openmdmremote.WebkeyApplication;
import com.openmdmremote.harbor.account.LogIn;
import com.openmdmremote.service.services.RegistrationService;
import com.openmdmremote.ui.main.FragmentAbout;
import com.openmdmremote.ui.registration.RegistrationUtil;
import com.openmdmremote.ui.typefaces.MyTextInputLayout;
import com.openmdmremote.ui.typefaces.MyTextView;

import static com.openmdmremote.ui.registration.fragments.Utile.figureOutEmail;
import static com.openmdmremote.ui.registration.fragments.Utile.validateEmailAddress;

public class LogInFragment extends Fragment implements LogIn.LogInListener {
    public static final String FRAGMENT_TAG = "loginfragment";

    private View view;
    private RegistrationUtil registrationUtil;

    private MyTextInputLayout email;
    private MyTextInputLayout password;
    private MyTextView errorMsg;

    private String sNick;
    private String sPassword;

    private RegistrationService regService;

    public LogInFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_log_in, container, false);

        errorMsg = (MyTextView) view.findViewById(R.id.error_msg);

        email = (MyTextInputLayout) view.findViewById(R.id.inputlayout_email);
        email.getEditText().setText(figureOutEmail(getActivity()));

        password = (MyTextInputLayout) view.findViewById(R.id.inputlayout_pwd);

        view.findViewById(R.id.btn_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validate()) {
                    showProgress();
                    regService.logIn(LogInFragment.this, sNick, sPassword);
                }

              // onSuccess();
            }
        });

        view.findViewById(R.id.btn_resetpwd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToResetPwdFragment();
            }
        });


        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof RegistrationUtil) {
            registrationUtil = (RegistrationUtil) context;
            regService = registrationUtil.getRegistrationService();
        }
    }

    @Override
    public void onWrongPwd() {
        WebkeyApplication.getGoogleAnalitics().AccountLoginFailed();
        showErrorMsg(R.string.login_error_password);
    }

    @Override
    public void onOtherError() {
        WebkeyApplication.getGoogleAnalitics().AccountLoginFailed();
        showErrorMsg(R.string.login_error_internal);
    }

    @Override
    public void onSuccess() {
        WebkeyApplication.getGoogleAnalitics().AccountLoginSuccess();
        switchToDeviceReg();
        hideError();
    }

    private void hideError() {
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                errorMsg.setText("");
                errorMsg.setVisibility(View.GONE);
                dismissProgress();
            }
        }, 500);
    }

    private void showErrorMsg(final int res) {
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                errorMsg.setText(res);
                errorMsg.setVisibility(View.VISIBLE);
                dismissProgress();
            }
        }, 500);
    }

    private void showProgress() {
        registrationUtil.onProgress(view);
    }

    private void dismissProgress() {
        registrationUtil.onDismiss(view);
    }

    private void switchToDeviceReg() {
        view.post(new Runnable() {
            @Override
            public void run() {
                dismissProgress();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, new DeviceRegFragment(), DeviceRegFragment.FRAGMENT_TAG);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
    }

    private boolean validate() {
        boolean valid = true;
        if (!validateEmail()) {
            valid = false;
        }

        sNick = email.getEditText().getText().toString();
        sPassword = password.getEditText().getText().toString();
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

    private void switchToResetPwdFragment() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, new ResetPasswordFragment(), FragmentAbout.FRAGMENT_TAG);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
