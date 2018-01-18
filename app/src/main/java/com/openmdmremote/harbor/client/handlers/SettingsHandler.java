package com.openmdmremote.harbor.client.handlers;

import android.content.Context;

import com.openmdmremote.WebkeyApplication;
import com.openmdmremote.harbor.HRPCProto;
import com.openmdmremote.harbor.settings.HarborAuthSettings;

public class SettingsHandler implements HarborMessageHandler {
    private final Context context;

    public SettingsHandler(Context context) {
        this.context = context;
    }

    @Override
    public void onMessage(HRPCProto.Message msg) {
        if(msg.getSettings().hasNick()) {
            updateNickName(msg.getSettings().getNick());
        }
    }

    @Override
    public void onClosed() {

    }

    private void updateNickName(String nickName) {
        HarborAuthSettings harborAuthSettings = new HarborAuthSettings(context);
        harborAuthSettings.setDeviceNickName(nickName);
        WebkeyApplication.log("HarborClient","Device nick has been updated");
    }
}
