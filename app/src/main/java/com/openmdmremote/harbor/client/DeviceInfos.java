package com.openmdmremote.harbor.client;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;

import com.openmdmremote.nativ.binary.EnvironmentSettings;

public class DeviceInfos {
    private final Context context;
    private String androidID;
    private String androidVersion;
    private String location;
    private int sdklevel;
    private boolean rooted;
    private String model;
    private String brand;

    public DeviceInfos(Context context) {
        this.context = context;
        setAndroidId();
        setAndroidVersion();
        setLocation();
        setSdklevel();
        setRootedProperty();
        setModel();
        setBrand();
    }

    private void setModel() {
        model = Build.MODEL;
    }

    private void setBrand() {
        brand = Build.BRAND;
    }

    private void setAndroidId() {
        androidID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    private void setRootedProperty() {
        rooted = EnvironmentSettings.checkRoot();
    }

    private void setLocation() {
        LocationManager m_location_manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (m_location_manager != null) {
            Location lm = null;
            try {
                lm = m_location_manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                double latitude = lm.getLatitude();
                double longitude = lm.getLongitude();
                if (latitude != 0.0 && longitude != 0.0) {
                    location = Double.toString(latitude) + "," + Double.toString(longitude);
                }

            } catch (SecurityException e) {

            } catch (Exception e) {

            }
        }
    }


    private void setSdklevel() {
        sdklevel = android.os.Build.VERSION.SDK_INT;
    }

    private void setAndroidVersion() {
        androidVersion = Build.VERSION.RELEASE;
    }

    public String getAndroidID() {
        return androidID;
    }

    public String getAndroidVersion() {
        return androidVersion;
    }

    public boolean isRooted() {
        return rooted;
    }

    public String getLocation() {
        return location;
    }

    public int getSdklevel() {
        return sdklevel;
    }

    public boolean hasLocation() {
        if(location == null) {
            return false;
        } else {
            return true;
        }
    }

    public String getModel() {
        return model;
    }

    public String getBrand() {
        return brand;
    }
}