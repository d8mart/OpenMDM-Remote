package com.openmdmremote.ui.main;

import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;

import com.openmdmremote.R;
import com.openmdmremote.WebkeyApplication;
import com.openmdmremote.harbor.settings.HarborAuthSettings;
import com.openmdmremote.service.BackgroundService;
import com.openmdmremote.service.services.Settings;
import com.openmdmremote.ui.preferences.LocalHttpPortDialog;
import com.openmdmremote.ui.preferences.LocalWSPortDialog;
import com.openmdmremote.ui.registration.RegistrationActivity;


public class FragmentSettings extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {

    private static final String DIALOG_FRAGMENT_TAG = "android.support.v7.preference.PreferenceFragment.DIALOG";

    Settings settings;
    HarborAuthSettings authSettings;
    public FragmentSettings() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settings = new Settings(getActivity());
        authSettings = new HarborAuthSettings(getActivity());

        getPreferenceManager().setSharedPreferencesName("WEBKEY");
        addPreferencesFromResource(R.xml.fragment_preferences);
        PreferenceManager.setDefaultValues(getActivity(), R.xml.fragment_preferences, false);

        setLogoutFunction();

        // Summary changes.
        bindPreferenceSummaryToValue(findPreference(Settings.LOCAL_HTTP_PORT));
        bindPreferenceSummaryToValue(findPreference(Settings.LOCAL_WS_PORT));


        findPreference(Settings.REMOTELOGGING).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object value) {
                WebkeyApplication.updateLogentriesSetting((Boolean) value);
                return true;
            }
        });

        findPreference(Settings.LOCAL_HTTP_PORT).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showLocalHttpDialog();
                return false;
            }
        });

        findPreference(Settings.LOCAL_WS_PORT).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showLocalWSDialog();
                return false;
            }
        });

        findPreference("account_preference").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                logOut();
                return true;
            }
        });

    }

    private void setLogoutFunction() {
        if(settings.isFleeted()) {
            findPreference("account_preference").setVisible(false);
        } else {
            findPreference("account_preference").setVisible(true);
        }
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {

    }

    private void logOut() {
        BackgroundService service = ((MainActivity) getActivity()).getService();
        service.logOut();
        Intent intent = new Intent(getActivity(), RegistrationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        getActivity().startActivity(intent);
        getActivity().finish();
    }


    private void showLocalHttpDialog() {
        LocalHttpPortDialog localHttpPortDialog = new LocalHttpPortDialog();
        localHttpPortDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                FragmentSettings.this.onPreferenceChange(
                        findPreference(Settings.LOCAL_HTTP_PORT), settings.getHttpPort());
            }
        });
        FragmentManager fm = getActivity().getFragmentManager();
        localHttpPortDialog.show(fm, "httpportdialog");
    }

    private void showLocalWSDialog() {
        LocalWSPortDialog localWSPortDialog = new LocalWSPortDialog();
        localWSPortDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                FragmentSettings.this.onPreferenceChange(
                        findPreference(Settings.LOCAL_WS_PORT), settings.getWSport());
            }
        });
        FragmentManager fm = getActivity().getFragmentManager();
        localWSPortDialog.show(fm, "wsportdialog");
    }

    private void bindPreferenceSummaryToValue(Preference preference) {
        if(preference != null) {
            preference.setOnPreferenceChangeListener(this);

            if(preference.getKey().equals(Settings.LOCAL_HTTP_PORT))
                this.onPreferenceChange(preference, settings.getHttpPort());

            if(preference.getKey().equals(Settings.LOCAL_WS_PORT))
                this.onPreferenceChange(preference, settings.getWSport());
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        preference.setSummary(value.toString());
        return true;
    }
}
