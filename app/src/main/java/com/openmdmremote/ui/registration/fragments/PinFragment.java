package com.openmdmremote.ui.registration.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.openmdmremote.R;
import com.openmdmremote.harbor.account.Pairing;
import com.openmdmremote.service.services.RegistrationService;
import com.openmdmremote.ui.registration.RegistrationUtil;
import com.openmdmremote.ui.typefaces.MyTextInputLayout;
import com.openmdmremote.ui.typefaces.MyTextView;

public class PinFragment extends Fragment {
    public static final String FRAGMENT_TAG = "pinfragment";
    private View view;
    private RegistrationUtil registrationUtil;
    private RegistrationService regService;
    private MyTextInputLayout pinInput;
    private MyTextView errorMsg;

    public PinFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_pin_pairing, container, false);

        pinInput = (MyTextInputLayout) view.findViewById(R.id.inputlayout_pairingping);
        errorMsg = (MyTextView) view.findViewById(R.id.error_msg);

        setTextOnPinField();

        view.findViewById(R.id.btn_pairing).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPinFromEditText();
                doPairing();
            }
        });

        if(regService.hasPin()) {
            doPairing();
        }

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

    private void setTextOnPinField() {
        if(regService.hasPin()) {
            pinInput.getEditText().setText(regService.getPin());
        }
    }

    private void doPairing() {
        showProgress();
        regService.pairing(new Pairing.PairingListener() {

            @Override
            public void onError() {
                showErrorMsg(R.string.devpair_error_internal);
            }

            @Override
            public void onWrongCode() {
                showErrorMsg(R.string.devpair_error_wrong_pin);
            }

            @Override
            public void onSuccess() {
                hideError();
            }
        });
    }

    private void hideError() {
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                errorMsg.setText("");
                errorMsg.setVisibility(View.GONE);
                dismissProgress();
                getActivity().finish();
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

    public void getPinFromEditText() {
        Editable pin = pinInput.getEditText().getText();
        regService.setPin(pin.toString());
    }
}
