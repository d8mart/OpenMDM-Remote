package com.openmdmremote.nativ.handlers;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.openmdmremote.nativ.WIPC;
import com.openmdmremote.nativ.net.WIPCProto;
import com.openmdmremote.service.handlers.interfaces.IScreenCap;

public class ScreenCap implements IScreenCap {

    private final WIPC wipc;
    private final Context context;

    private final BrowserScreenMetrics browserScreenMetrics = new BrowserScreenMetrics();

    // This variables represent what the browser want.
    private int askedWidth = 0;
    private int askedHeight = 0;

    public ScreenCap(Context context) {
        Log.i("ScreenCap","constructor");
        this.context = context;
        wipc = WIPC.getInstance(context);
        wipc.connect();

        readNewDisplayMetrics();

        askedWidth = browserScreenMetrics.getWidth();
        askedHeight = browserScreenMetrics.getHeight();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void readNewDisplayMetrics() {
        // Set the default resolution info.
        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();

        // For JellyBeans and onward.
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            display.getRealMetrics(metrics);
        } else {
            display.getMetrics(metrics);
        }

        browserScreenMetrics.setWidth(metrics.widthPixels);
        browserScreenMetrics.setHeight(metrics.heightPixels);
        browserScreenMetrics.setRotation(display.getRotation());
    }

    // It is for a fall back solution. Not used currently.
    private static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public void updateScreenResolution(int x, int y) {
        askedWidth = x;
        askedHeight = y;

        WIPCProto.Resolution r = WIPCProto.Resolution.newBuilder()
                .setWidth(askedWidth)
                .setHeight(askedHeight)
                .build();

        WIPCProto.Screen s = WIPCProto.Screen.newBuilder()
                .setType(WIPCProto.Screen.Type.RESOLUTION)
                .setResolution(r)
                .build();

        WIPCProto.Message msg = WIPCProto.Message.newBuilder()
                .setType(WIPCProto.Message.Type.SCREEN)
                .setScreen(s)
                .build();
        wipc.sendMsg(msg);
    }

    public void updateFrequency(int frequency) {

        WIPCProto.Frequency f = WIPCProto.Frequency.newBuilder()
                .setFrequency(frequency)
                .build();

        WIPCProto.Screen s = WIPCProto.Screen.newBuilder()
                .setType(WIPCProto.Screen.Type.FREQUENCY)
                .setFrequency(f)
                .build();

        WIPCProto.Message msg = WIPCProto.Message.newBuilder()
                .setType(WIPCProto.Message.Type.SCREEN)
                .setScreen(s)
                .build();
        wipc.sendMsg(msg);
    }

    public void checkDiff(boolean checkdiff) {
        WIPCProto.ImageDiff d = WIPCProto.ImageDiff.newBuilder()
                .setDiff(checkdiff)
                .build();

        WIPCProto.Screen s = WIPCProto.Screen.newBuilder()
                .setType(WIPCProto.Screen.Type.IMGDIFF)
                .setImagediff(d)
                .build();

        WIPCProto.Message msg = WIPCProto.Message.newBuilder()
                .setType(WIPCProto.Message.Type.SCREEN)
                .setScreen(s)
                .build();
        wipc.sendMsg(msg);
    }

    @Override
    public BrowserScreenMetrics getScreenMetrics() {
        readNewDisplayMetrics();
        return browserScreenMetrics;
    }

    @Override
    public byte[] getScreen() throws InterruptedException {
        return wipc.takeImage();
    }

    public class BrowserScreenMetrics {
        private int width = 0;
        private int height = 0;
        private int rotation = 0; // This variable has never used.

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public int getRotation() {
            return rotation;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public void setRotation(int rotation) {
            this.rotation = rotation;
        }
    }
}

