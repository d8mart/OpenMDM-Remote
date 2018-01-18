package com.openmdmremote;

import android.content.Context;
import android.util.Base64;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.openmdmremote.harbor.settings.HarborAuthSettings;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MyGoogleAnalytics {
    private final Tracker tracker;
    private final HarborAuthSettings harborAuth;

    public MyGoogleAnalytics(Context context) {
        harborAuth = new HarborAuthSettings(context);

        GoogleAnalytics analytics = GoogleAnalytics.getInstance(context);

        tracker = analytics.newTracker(BuildConfig.TRACKINGID);
        tracker.enableExceptionReporting(true);
        tracker.enableAdvertisingIdCollection(true);
        setAnalyticsUID();
    }

    private void setAnalyticsUID() {
        // https://support.google.com/analytics/answer/6205850?hl=en
        if (harborAuth.isRegisteredToHarbor()) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                md.update(harborAuth.getDeviceNickName().getBytes("UTF-8"));
                tracker.set("&uid", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    public void AccountRegistrationSuccess() {
        //todo: Hozza kellene adni valtozokent, hogy uj regisztracio vagy mar korabban volt ezen a deviceon.
        setAnalyticsUID();
        tracker.send(new HitBuilders.EventBuilder("Registration", "success").build());
    }

    public void AccountRegistrationFailed() {
        setAnalyticsUID();
        tracker.send(new HitBuilders.EventBuilder("Registration", "failed").build());
    }

    public void AccountLoginFailed() {
        setAnalyticsUID();
        tracker.send(new HitBuilders.EventBuilder("Login", "failed").build());
    }

    public void AccountLoginSuccess() {
        setAnalyticsUID();
        tracker.send(new HitBuilders.EventBuilder("Login", "success").build());
    }

    public void BrowserLeft() {
        HitBuilders.EventBuilder analitics = new HitBuilders.EventBuilder();
        analitics.setCategory("BrowserLeft");
        analitics.setAction("leftuser");
        tracker.send(analitics.build());
    }

    public void BrowserLoginSuccess() {
        HitBuilders.EventBuilder analitics = new HitBuilders.EventBuilder();
        analitics.setCategory("BrowserLogin");
        analitics.setAction("success");
        tracker.send(analitics.build());
    }

    public void BrowserLoginFailed() {
        HitBuilders.EventBuilder analitics = new HitBuilders.EventBuilder();
        analitics.setCategory("BrowserLogin");
        analitics.setAction("failed");
        tracker.send(analitics.build());
    }

    public void BrowserLoginAdminFailed() {
        HitBuilders.EventBuilder analitics = new HitBuilders.EventBuilder();
        analitics.setCategory("BrowserLogin");
        analitics.setAction("adminFailed");
        tracker.send(analitics.build());
    }

    public void BrowserLoginAdminSuccess() {
        HitBuilders.EventBuilder analitics = new HitBuilders.EventBuilder();
        analitics.setCategory("BrowserLogin");
        analitics.setAction("adminSuccess");
        tracker.send(analitics.build());
    }

    public void BrowserLoginInUsed() {
        HitBuilders.EventBuilder analitics = new HitBuilders.EventBuilder();
        analitics.setCategory("BrowserLogin");
        analitics.setAction("inused");
        tracker.send(analitics.build());
    }
}
