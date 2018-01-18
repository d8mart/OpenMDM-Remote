package com.openmdmremote.ui.main;

import android.Manifest;
import android.app.Activity;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;

public class LocationPermissionManager {

    public final static int GPS_PERMISSION_REQUEST_CODE = 1;
    private final Activity activity;
    private int permission;

    public LocationPermissionManager(Activity activity) {
        this.activity = activity;
    }

    private void refreshPermission() {
        permission = PermissionChecker.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private boolean shouldAskPermission() {
        refreshPermission();

        switch (permission) {
            case PermissionChecker.PERMISSION_GRANTED:
                return false;
            case PermissionChecker.PERMISSION_DENIED:
                return true;
            case PermissionChecker.PERMISSION_DENIED_APP_OP:
                return false;
            default:
                return true;
        }
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION},
                GPS_PERMISSION_REQUEST_CODE);
    }

    public void checkPermission() {
        if(shouldAskPermission()) {
            requestLocationPermission();
        }
    }
}
