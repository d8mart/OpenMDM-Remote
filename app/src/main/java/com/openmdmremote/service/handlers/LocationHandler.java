package com.openmdmremote.service.handlers;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;

import com.openmdmremote.WebkeyApplication;
import com.openmdmremote.net.visitor.WebkeyVisitor;
import com.openmdmremote.service.dto.LocationPayload;
import com.openmdmremote.service.dto.Message;
import com.openmdmremote.service.handlers.interfaces.MessageHandler;

public class LocationHandler implements MessageHandler {


    private Context context;
    private WebkeyVisitor wsUser;
    private LocationManager locationManager;


    private static final long MIN_TIME = 1000; // Minimum time interval between location updates, in milliseconds.
    private static final float MIN_DISTANCE = 0; // Minimum distance between location updates, in meters.

    public LocationHandler(Context c, WebkeyVisitor ws){
        this.context = c;
        this.wsUser = ws;
        locationManager = (LocationManager) this.context.getSystemService(Context.LOCATION_SERVICE);
    }

    private void startUpdates(){
        WebkeyApplication.log("Location", "started");
        Handler mainHandler = new Handler(context.getMainLooper());
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (locationManager.getAllProviders().contains(LocationManager.NETWORK_PROVIDER)) {
                    try {
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, locationListener);
                        sendLocation(locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));
                    } catch (SecurityException e) {
                    } catch (Exception e) {
                    }
                }

                if (locationManager.getAllProviders().contains(LocationManager.GPS_PROVIDER)) {
                    try {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, locationListener);
                        sendLocation(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
                    } catch (SecurityException e) {
                    } catch (Exception e) {
                    }
                }
            }
        });
    }

    private void sendLocation(Location location){
        LocationPayload p = new LocationPayload();
        if(location != null ) {
            p.lat = location.getLatitude();
            p.lon = location.getLongitude();
            wsUser.sendGson(new Message("1", Message.Type.LOCATION, p));
        } else {
            WebkeyApplication.log("Location", "GPS not available");
        }
    }

    private void stopUpdates(){
        locationManager.removeUpdates(locationListener);
    }

    @Override
    public void onData(Message msg) {
        switch(msg.type){
            case LOCATION_START:
                startUpdates();
                break;
            case LOCATION_STOP:
                stopUpdates();
                break;
        }
    }

    @Override
    public void onLeftUser(WebkeyVisitor webkeyVisitor) {
        stopUpdates();
    }

    @Override
    public void onLeftAllUsers() {
        stopUpdates();
    }

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            sendLocation(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
        @Override
        public void onProviderEnabled(String provider) {}
        @Override
        public void onProviderDisabled(String provider) {}
    };

}
