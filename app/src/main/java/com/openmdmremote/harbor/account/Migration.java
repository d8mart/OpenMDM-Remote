package com.openmdmremote.harbor.account;

import android.content.Context;

import com.openmdmremote.WebkeyApplication;
import com.openmdmremote.harbor.account.http.CallMigration;
import com.openmdmremote.harbor.settings.HarborAuthSettings;
import com.openmdmremote.service.services.Settings;

public class Migration implements CallMigration.OnResultListener {

    private final HarborAuthSettings harborAuthSettings;
    private final CallMigration callMigration;
    private final Settings settings;
    private String deviceNick;
    private MigrationListener migrationListener;

    public Migration(Context context) {
        callMigration = new CallMigration(context);
        harborAuthSettings = new HarborAuthSettings(context);
        settings = new Settings(context);
    }

    public void doMigration(MigrationListener migrationListener) {
        if(!settings.hasOldReg()) {
            WebkeyApplication.log("Migration", "No need to migration");
            migrationListener.noNeedMigration();
            return;
        }

        WebkeyApplication.log("Migration", "Try migrate");
        this.migrationListener = migrationListener;
        deviceNick = settings.getDeviceNick();
        callMigration.callMigration(this, settings.getDeviceToken());
    }


    @Override
    public void onError(String error) {
        WebkeyApplication.log("Migration", "Error: " + error);
        migrationListener.onError();
    }

    @Override
    public void onSuccess(String uuid, String remotePWD) {
        WebkeyApplication.log("Migration", "Success");
        harborAuthSettings.setDeviceToken(uuid);
        harborAuthSettings.setDeviceNickName(deviceNick);
        harborAuthSettings.signUpRemoteUser(remotePWD);

        // Clean the old property.
        settings.removeDevicetokenAndNick();

        migrationListener.onSuccess();
    }

    public interface MigrationListener {
        void onError();
        void noNeedMigration();
        void onSuccess();
    }
}
