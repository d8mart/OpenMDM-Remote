package com.openmdmremote.ui.registration.fragments;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.openmdmremote.R;
import com.openmdmremote.WebkeyApplication;
import com.openmdmremote.harbor.account.SignUp;
import com.openmdmremote.service.services.RegistrationService;
import com.openmdmremote.ui.main.FragmentAbout;
import com.openmdmremote.ui.registration.RegistrationUtil;
import com.openmdmremote.ui.typefaces.MyTextInputLayout;
import com.openmdmremote.ui.typefaces.MyTextView;

import static com.openmdmremote.ui.registration.fragments.Utile.figureOutEmail;
import static com.openmdmremote.ui.registration.fragments.Utile.validateEmailAddress;
import static com.openmdmremote.ui.registration.fragments.Utile.validatePassword;

public class SignUpFragment extends Fragment implements SignUp.SignUpListener {
    public static final String FRAGMENT_TAG = "signupfragment";

    private MyTextInputLayout email;
    private MyTextInputLayout pwd;
    private MyTextInputLayout pwdagain;
    private View view;

    private String nick;
    private String password;

    private RegistrationService regService;
    private RegistrationUtil registrationUtil;

    public SignUpFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_sign_up, container, false);

        email = (MyTextInputLayout) view.findViewById(R.id.inputlayout_email);
        pwd = (MyTextInputLayout) view.findViewById(R.id.inputlayout_pwd);
        pwdagain = (MyTextInputLayout) view.findViewById(R.id.inputlayout_pwdagain);
        MyTextView license = (MyTextView) view.findViewById(R.id.link_termsofservice);

        // Fill with default data.
        email.getEditText().setText(figureOutEmail(getActivity()));

        view.findViewById(R.id.btn_signup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validate()) {
                    WebkeyApplication.log("SignUp", "sendRegistration request");
                    sendRegistrationRequest();
                }
            }
        });

        // For the pwd comparator.
        pwd.getEditText().setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    comparePasswords();
                }
            }
        });

        pwdagain.getEditText().setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    comparePasswords();
                }
            }
        });

        license.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchToAbout();
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
    public void onNicInUsed() {
        showUsedNick();
        WebkeyApplication.getGoogleAnalitics().AccountRegistrationFailed();
    }

    @Override
    public void onOtherError() {
        WebkeyApplication.getGoogleAnalitics().AccountRegistrationFailed();
        showToast(getString(R.string.signup_toast_server_err_internal));
    }

    @Override
    public void onSuccess() {
        WebkeyApplication.getGoogleAnalitics().AccountRegistrationSuccess();
        switchToDeviceReg();
    }

    private void sendRegistrationRequest() {
        showProgress();
        try {
            regService.signUp(this, nick, password);
        } catch (Exception e) {
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
        }

        if (!validatePwd()) {
            valid = false;
        }

        if (!comparePasswords()) {
            valid = false;
        }

        if (valid) {
            nick = email.getEditText().getText().toString();
            password = pwd.getEditText().getText().toString();
        }
        return valid;
    }

    private boolean comparePasswords() {
        String pwd1 = pwd.getEditText().getText().toString();
        String pwd2 = pwdagain.getEditText().getText().toString();
        if(pwd1.length() > 0 && pwd2.length() > 0 ) {
            if (pwd1.equals(pwd2)) {

                // Shows the icon.
                Drawable image = getActivity().getResources().getDrawable( R.drawable.ic_done);
                int h = image.getIntrinsicHeight();
                int w = image.getIntrinsicWidth();
                image.setBounds( 0, 0, w, h );
                pwdagain.getEditText().setCompoundDrawables(null, null, image, null);
                return true;
            } else {
                Drawable image = getActivity().getResources().getDrawable(R.drawable.ic_action_cancel_white);
                int h = image.getIntrinsicHeight();
                int w = image.getIntrinsicWidth();
                image.setBounds(0, 0, w, h);
                pwdagain.getEditText().setCompoundDrawables(null, null, image, null);
                return false;
            }
        }

        // Hide the icon.
        pwdagain.getEditText().setCompoundDrawables(null, null, null, null);
        return false;
    }

    private boolean validatePwd() {
        String spwd = pwd.getEditText().getText().toString();
        if (!validatePassword(spwd)) {
            pwd.setError(getResources().getString(R.string.signup_error_pwd));
            pwd.setOnwTypeFace();
            return false;
        } else {
            pwd.setError(null);
            pwd.setErrorEnabled(false);
            return true;
        }
    }

    private void showUsedNick() {
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                email.setError(getResources().getString(R.string.signup_error_email_alreadyinuse));
                email.setOnwTypeFace();
                dismissProgress();
            }
        }, 500);
    }

    private boolean validateEmail() {
        if (!validateEmailAddress(email.getEditText().getText().toString())) {
            email.setError(getResources().getString(R.string.signup_error_email));
            email.setOnwTypeFace();
            return false;
        } else {
            email.setError(null);
            email.setErrorEnabled(false);
            return true;
        }
    }

    private void showToast(final String msg) {
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
                dismissProgress();
            }
        }, 500);
    }

    private void switchToDeviceReg() {
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                dismissProgress();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, new DeviceRegFragment(), DeviceRegFragment.FRAGMENT_TAG);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        }, 500);
    }

    private void switchToAbout() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, new FragmentAbout(), FragmentAbout.FRAGMENT_TAG);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
