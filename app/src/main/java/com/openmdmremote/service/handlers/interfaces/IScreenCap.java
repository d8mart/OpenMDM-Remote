package com.openmdmremote.service.handlers.interfaces;

import com.openmdmremote.nativ.handlers.ScreenCap;

public interface IScreenCap {
    public ScreenCap.BrowserScreenMetrics getScreenMetrics();
    public void updateScreenResolution(int x, int y);
    public void updateFrequency(int f);
    public void checkDiff(boolean diff);
    public byte[] getScreen() throws InterruptedException;
}
