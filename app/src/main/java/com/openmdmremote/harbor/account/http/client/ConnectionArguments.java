package com.openmdmremote.harbor.account.http.client;

import com.openmdmremote.harbor.settings.HarborServerSettings;

public class ConnectionArguments {
    private final boolean secure;
    private final String host;
    private final int port;

    public ConnectionArguments() {
        HarborServerSettings harborServerSettings = new HarborServerSettings();
        secure = harborServerSettings.isSecure();
        host = harborServerSettings.getHarborServerAddress();
        port = harborServerSettings.getHarborServerPort();
    }

    public boolean isSecure() {
        return secure;
    }

    public int getPort() {
        return port;
    }

    public String getHost() {
        return host;
    }
}
