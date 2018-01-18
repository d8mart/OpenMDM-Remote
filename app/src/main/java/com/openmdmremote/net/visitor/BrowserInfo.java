package com.openmdmremote.net.visitor;

import com.openmdmremote.WebkeyApplication;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BrowserInfo {
    private Pattern win = Pattern.compile("win", Pattern.CASE_INSENSITIVE);
    private Pattern mac = Pattern.compile("mac|apple|osx", Pattern.CASE_INSENSITIVE);
    private Pattern linux = Pattern.compile("linux", Pattern.CASE_INSENSITIVE);

    public enum OS {
        WIN, MAC, LINUX, UNKNOWN
    }

    private Long loginTime;
    private String agent;
    private OS platform;

    public BrowserInfo(long loginTime, String agent, String platform) {
        this.loginTime = loginTime;
        this.agent = agent;
        declarePlatform(platform);
    }

    private boolean isWindows(String platform) {
        Matcher m = win.matcher(platform);
        return m.find();
    }

    private boolean isLinux(String platform) {
        Matcher m = linux.matcher(platform);
        return m.find();
    }

    private boolean isMac(String platform) {
        Matcher m = mac.matcher(platform);
        return m.find();
    }


    private void declarePlatform(String platform) {
        if (isWindows(platform)) {
            this.platform = OS.WIN;
            return;
        }

        if (isMac(platform)) {
            this.platform = OS.MAC;
            return;
        }

        if (isLinux(platform)) {
            this.platform = OS.LINUX;
            return;
        }

        this.platform = OS.UNKNOWN;
        WebkeyApplication.log("Browser info", "OS: " + platform);
    }

    public String getAgent() {
        return agent;
    }

    public OS getPlatform() {
        return platform;
    }

    public Long getloginTime() {
        return loginTime;
    }
}
