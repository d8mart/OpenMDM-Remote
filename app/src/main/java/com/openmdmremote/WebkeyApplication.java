package com.openmdmremote;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.openmdmremote.harbor.settings.HarborServerSettings;
import com.openmdmremote.logfender.logger.AndroidLogger;
import com.openmdmremote.service.services.Settings;

import java.io.IOException;

public class WebkeyApplication extends MultiDexApplication {
    private static Context context;
    private static com.openmdmremote.service.services.Settings settings;

    private static AndroidLogger fenderlog;
    private static boolean useLogentry = false;
    private static int versionCode = 0;

    private static MyGoogleAnalytics googleAnalytics;

    @Override
    public void onCreate(){
        super.onCreate();
        context = getApplicationContext();
        settings = new Settings(context);

        // init remote logging
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionCode = pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        initLogentrie();

        googleAnalytics = new MyGoogleAnalytics(this);
    }

    public static MyGoogleAnalytics getGoogleAnalitics() {
        return googleAnalytics;
    }

    public static void updateLogentriesSetting(boolean enabled){
        useLogentry = enabled;
    }

    private void initLogentrie() {
        try {
            String android_id = android.provider.Settings.Secure.getString(context.getContentResolver(),
                    android.provider.Settings.Secure.ANDROID_ID);

            fenderlog = AndroidLogger.createInstance(android_id, HarborServerSettings.ADDRESS_REMOTELOGGING, HarborServerSettings.LOGCAT_PORT, false);
            if(settings.getRemoteLogging()) {
                useLogentry = true;
            } else {
                useLogentry = false;
            }

        } catch (IOException e) {
            useLogentry = false;
        }
    }

    public static void log(String classTag, String message) {
        String tag = "Webkey-" + classTag;
        if(useLogentry) {
            fenderlog.log("(" + versionCode + ") " + tag + ": " + message);
        }
        Log.i(tag, message);
    }

    public static void logWeb(String tag, String message) {
        if(useLogentry) {
            fenderlog.log("(" + versionCode + ") " + tag + ": " + message);
        }
    }

    public static Context getContext() {
        return context;
    }
}
