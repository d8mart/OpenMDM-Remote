package com.openmdmremote.harbor.account;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.os.Build;
import android.util.Patterns;

import java.util.Random;
import java.util.regex.Pattern;

public class DeviceInfo {

    private final Context context;

    public DeviceInfo(Context context) {
        this.context = context;
    }
    public String getDeviceType() {
        return Build.BRAND + "," + Build.MODEL;
    }

    public String getAndroidId() {
        return android.provider.Settings.Secure.getString(context.getContentResolver(),
                android.provider.Settings.Secure.ANDROID_ID);
    }

    public String generateNick() {
        String nick = getDeviceNickByEmail();
        if (nick != null) {
            return nick;
        } else{
            return getDeviceNickByBrand();
        }
    }

    public String generateRandomNick() {
        Random r = new Random();
        int max = 100;
        int min = 1;
        int randomNum = r.nextInt(max - min + 1) + min;
        String nick = generateNick();
        return nick + Integer.toString(randomNum);
    }

    private String getDeviceNickByEmail() {
        String email = getEmailAddress();
        if (email != null) {
            String[] nick = email.split("@");
            return nick[0].toLowerCase();
        }
        return null;
    }

    private String getEmailAddress() {
        Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
        Account[] accounts = AccountManager.get(context).getAccounts();
        for (Account account : accounts) {
            if (emailPattern.matcher(account.name).matches()) {
                return account.name;
            }
        }
        return null;
    }

    private String getDeviceNickByBrand() {
        return Build.MODEL.replaceAll("\\s+","").toLowerCase();
    }
}
