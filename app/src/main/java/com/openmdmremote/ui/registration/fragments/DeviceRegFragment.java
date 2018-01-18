package com.openmdmremote.ui.registration.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.openmdmremote.R;
import com.openmdmremote.harbor.account.DeviceReg;
import com.openmdmremote.harbor.account.Migration;
import com.openmdmremote.service.services.RegistrationService;
import com.openmdmremote.ui.registration.RegistrationUtil;
import com.openmdmremote.ui.typefaces.MyTextInputLayout;

import static com.openmdmremote.ui.registration.fragments.Utile.figureOutDevice;


public class DeviceRegFragment extends Fragment implements DeviceReg.DeviceRegListener {
    public static final String FRAGMENT_TAG = "devicefragment";

    private View view;
    private RegistrationUtil registrationUtil;
    private RegistrationService regService;

    private MyTextInputLayout device;

    private String deviceName;

    public DeviceRegFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_reg_device, container, false);

        device = (MyTextInputLayout) view.findViewById(R.id.inputlayout_devicenick);

        device.getEditText().setText(figureOutDevice(getActivity()));

        view.findViewById(R.id.btn_send_reg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validate()) {
                    sendDeviceRegistration();
                }
            }
        });

        migrate();
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
    }

    @Override
    public void onOtherError() {
        showError();
    }

    @Override
    public void onSuccess() {
        registrationOk();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void migrate() {
        showProgress();
        regService.migration(new Migration.MigrationListener() {
            @Override
            public void onError() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dismissProgress();
                    }
                });
            }

            @Override
            public void noNeedMigration() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dismissProgress();
                    }
                });
            }

            @Override
            public void onSuccess() {
                registrationOk();
            }
        });
    }

    private void registrationOk() {
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                dismissProgress();
                getActivity().finish();
            }
        }, 500);
    }

    private void showUsedNick() {
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                device.setError(getResources().getString(R.string.devreg_error_alreadyinuse));
                device.setOnwTypeFace();
                dismissProgress();
            }
        }, 500);
    }

    private void showError() {
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                showToast(getString(R.string.devreg_toast_server_err_internal));
                dismissProgress();
            }
        }, 500);
    }

    private void sendDeviceRegistration() {
        showProgress();
        regService.deviceRegistration(this, deviceName);
    }

    private void showProgress() {
        registrationUtil.onProgress(view);
    }

    private void dismissProgress() {
        registrationUtil.onDismiss(view);
    }

    private boolean validate() {
        deviceName = device.getEditText().getText().toString();
        if (deviceName.length() > 30 || deviceName.length() < 3) {
            device.setError(getResources().getString(R.string.devreg_error_devicename));
            device.setOnwTypeFace();
            return false;
        } else {
            device.setError(null);
            device.setErrorEnabled(false);
            return true;
        }
    }

    private void showToast(final String msg) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
