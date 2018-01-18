package com.openmdmremote.ui.registration;

import android.view.View;

import com.openmdmremote.service.services.RegistrationService;

public interface RegistrationUtil {
    void onProgress(View view);

    void onDismiss(View view);

    RegistrationService getRegistrationService();
}