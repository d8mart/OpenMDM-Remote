package com.openmdmremote.ui.registration.fragments;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.util.Patterns;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utile {
    public static boolean validateEmailAddress(String address) {
        Pattern pattern = Patterns.EMAIL_ADDRESS;

        Matcher matcher = pattern.matcher(address);
        if (!matcher.find()) {
            return false;
        } else {
            return true;
        }
    }

    public static String figureOutEmail(Context context) {
        Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
        Account[] accounts = AccountManager.get(context).getAccounts();
        for (Account account : accounts) {
            if (emailPattern.matcher(account.name).matches()) {
                return account.name;
            }
        }
        return null;
    }

    public static String figureOutDevice(Context context) {
        String email = figureOutEmail(context);
        if (email != null) {
            String[] nick = email.split("@");
            return nick[0].toLowerCase();
        }
        return null;
    }

    public static boolean validatePassword(String pwd) {
        if (pwd.isEmpty() || pwd.length() < 6) {
            return false;
        } else {
            return true;
        }
    }
}
