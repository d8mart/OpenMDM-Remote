package com.openmdmremote.ui.registration;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.openmdmremote.R;
import com.openmdmremote.WebkeyApplication;
import com.openmdmremote.harbor.settings.HarborAuthSettings;
import com.openmdmremote.service.services.RegistrationService;
import com.openmdmremote.ui.main.MainActivity;
import com.openmdmremote.ui.registration.fragments.PinFragment;
import com.openmdmremote.ui.registration.fragments.WelcomeTourFragment;


public class RegistrationActivity extends AppCompatActivity implements RegistrationUtil, FragmentManager.OnBackStackChangedListener {
    private static final String LOGTAG = "RegistrationActivity";
    private FrameLayout progress;

    private RegistrationService registrationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registrationService = new RegistrationService(this);
        setContentView(R.layout.activity_first_start);

        initActionBar();

        if(savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().
                    replace(R.id.fragment_container, new WelcomeTourFragment(), WelcomeTourFragment.FRAGMENT_TAG).
                    commit();
        }

        progress = (FrameLayout) findViewById(R.id.progress);
    }

    private void initActionBar() {
        hideBackButton();

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        //show custom title with custom font
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        LayoutInflater inflator = LayoutInflater.from(this);
        View v = inflator.inflate(R.layout.title_view, null);
        getSupportActionBar().setCustomView(v);

        //add change listener for back button handling
        getSupportFragmentManager().addOnBackStackChangedListener(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        readPin();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.registration, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        switch (item.getItemId()) {
            case android.R.id.home:
                WebkeyApplication.log(LOGTAG, "Back button");
                FragmentManager fm = getSupportFragmentManager();
                fm.popBackStack();
                return true;
            case R.id.pairing_menu:
                openPinFragment();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void readPin() {
        String pin = getPINfromIntent();
        if(pin != null) {
            registrationService.setPin(pin);
            openPinFragment();
        }
    }

    private void openPinFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, new PinFragment(), PinFragment.FRAGMENT_TAG);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void hideBackButton() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    private void showBackButton() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onStop() {
        super.onStop();
        HarborAuthSettings harborAuthSettings = new HarborAuthSettings(this);
        if (harborAuthSettings.isRegisteredToHarbor()) {
            restartTheMainActivity();
        }
    }

    private String getPINfromIntent(){
        String pin = null;
        Uri data = getIntent().getData();
        if(data != null) {
            pin = data.getPathSegments().get(1);
        }
        return pin;
    }

    private void restartTheMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void enableDisableView(View view, boolean enabled) {
        view.setEnabled(enabled);
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;

            for (int idx = 0; idx < group.getChildCount(); idx++) {
                enableDisableView(group.getChildAt(idx), enabled);
            }
        }
    }

    @Override
    public void onProgress(View view) {
        progress.setVisibility(View.VISIBLE);
        enableDisableView(view, false);
        getSupportActionBar().hide();
    }

    @Override
    public void onDismiss(View view) {
        progress.setVisibility(View.INVISIBLE);
        enableDisableView(view, true);
        getSupportActionBar().show();
    }

    @Override
    public RegistrationService getRegistrationService() {
        return registrationService;
    }

    @Override
    public void onBackStackChanged() {
        if (currentFragmentIsWelcomeTour()) {
            hideBackButton();
            showTitle();
        } else {
            showBackButton();
            hideTitle();
        }
    }

    private void hideTitle() {
        getSupportActionBar().setDisplayShowCustomEnabled(false);
    }

    private void showTitle() {
        getSupportActionBar().setDisplayShowCustomEnabled(true);

    }

    private boolean currentFragmentIsWelcomeTour() {
        Fragment welcomeFragment = getSupportFragmentManager().findFragmentByTag(WelcomeTourFragment.FRAGMENT_TAG);
        if (welcomeFragment == null) {
            return false;
        }

        if (welcomeFragment.isVisible()) {
            return true;
        } else {
            return false;
        }

    }
}