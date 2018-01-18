package com.openmdmremote.service.services;

import android.content.Context;

import com.openmdmremote.WebkeyApplication;
import com.openmdmremote.harbor.account.DeviceReg;
import com.openmdmremote.harbor.account.LogIn;
import com.openmdmremote.harbor.account.Migration;
import com.openmdmremote.harbor.account.Pairing;
import com.openmdmremote.harbor.account.SignUp;
import com.openmdmremote.harbor.settings.HarborAuthSettings;

public class RegistrationService {

    private final HarborAuthSettings harborAuthSettings;
    private final Context context;
    private final Migration migration;

    private String pwd;
    private String pin;

    public RegistrationService(Context context) {
        this.context = context;
        harborAuthSettings = new HarborAuthSettings(context);
        migration = new Migration(context);
    }

    public void signUp(SignUp.SignUpListener signUpListener, String nick, String password) {
        this.pwd = password;
        SignUp signUp = new SignUp(context);
        signUp.doSignUp(signUpListener, nick, password);
    }

    public void logIn(LogIn.LogInListener logInListener, String nick, String password) {
        this.pwd = password;
        LogIn login = new LogIn(context);
        login.doLogIn(logInListener, nick, password);
    }

    public void deviceRegistration(final DeviceReg.DeviceRegListener deviceRegListener, String deviceName) {
        DeviceReg devReg = new DeviceReg(context);
        devReg.doDeviceReg(new DeviceReg.DeviceRegListener() {
            @Override
            public void onNicInUsed() {
                deviceRegListener.onNicInUsed();
            }

            @Override
            public void onOtherError() {
                deviceRegListener.onOtherError();
            }

            @Override
            public void onSuccess() {
                if(registrateLocalUser()) {
                    deviceRegListener.onSuccess();
                } else {
                    deviceRegListener.onOtherError();
                }
            }
        }, deviceName);
    }


    public void migration(final Migration.MigrationListener migrationListener) {
        migration.doMigration(new Migration.MigrationListener() {
            @Override
            public void onError() {
                migrationListener.onError();
            }

            @Override
            public void noNeedMigration() {
                migrationListener.noNeedMigration();
            }

            @Override
            public void onSuccess() {
                if(registrateLocalUser()) {
                    migrationListener.onSuccess();
                } else {
                    migrationListener.onError();
                }
            }
        });
    }

    private boolean registrateLocalUser() {
        String nick = harborAuthSettings.getAccountName();

        if(nick != null && pwd != null) {
            LocalAuthService localAuthService = new LocalAuthService(context);
            localAuthService.signUpLocalUser(nick, pwd);
            WebkeyApplication.log("RegistrationService", "admin user added");
            return true;
        } else {
            WebkeyApplication.log("RegistrationService", "failed to add admin user");
            return false;
        }
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getPin() {
        return pin;
    }

    public boolean hasPin() {
        if(pin == null) {
            return false;
        } else {
            return true;
        }
    }

    public void pairing(Pairing.PairingListener pairingListener) {
        Pairing pairing = new Pairing(context);
        pairing.pairing(pairingListener, pin);
    }
}
