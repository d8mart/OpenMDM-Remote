package com.openmdmremote;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.openmdmremote.service.BackgroundService;
import com.openmdmremote.service.services.Settings;

public class BootStarter extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Settings settings = new Settings(context);

        /* Ez azert low memory kill management miatt kell
         * Ha nem csinaljuk akkor ha nincs bekapcsolva az autostart beallitas akkor telo restart es
         * activity inditas utan automatikusan indulna a ws service
         */

        settings.setStarted(false);
        if(settings.getAutostart()) {
            Intent serviceIntent = new Intent(context, BackgroundService.class);
            serviceIntent.setAction(intent.getAction());
            context.startService(serviceIntent);
        }
    }
}
