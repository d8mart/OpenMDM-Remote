package com.openmdmremote.service.dto;

import android.view.KeyEvent;

public class WebkeyButtons {
    public static final int VOLUME_UP = 0;
    public static final int VOLUME_DOWN = 1;
    public static final int BACK = 2;
    public static final int HOME = 3;
    public static final int MENU = 4;
    public static final int POWER = 5;
    public static final int ARROW_DOWN = 40;
    public static final int ARROW_RIGHT = 39;
    public static final int ARROW_UP = 38;
    public static final int ARROW_LEFT = 37;
    public static final int CTRL = 17;
    public static final int ALT = 18;
    public static final int ENTER = 13;
    public static final int SHIFT = 16;
    public static final int BACKSPACE = 8;

    public static int getAndroidButtonCode(int code) {
        switch (code) {
            case VOLUME_UP:
                return KeyEvent.KEYCODE_VOLUME_UP;
            case VOLUME_DOWN:
                return KeyEvent.KEYCODE_VOLUME_DOWN;
            case BACK:
                return KeyEvent.KEYCODE_BACK;
            case HOME:
                return KeyEvent.KEYCODE_HOME;
            case MENU:
                return KeyEvent.KEYCODE_MENU;
            case POWER:
                return KeyEvent.KEYCODE_POWER;
            case ARROW_DOWN:
                return KeyEvent.KEYCODE_DPAD_DOWN;
            case ARROW_RIGHT:
                return KeyEvent.KEYCODE_DPAD_RIGHT;
            case ARROW_UP:
                return KeyEvent.KEYCODE_DPAD_UP;
            case ARROW_LEFT:
                return KeyEvent.KEYCODE_DPAD_LEFT;
            case CTRL:
                return KeyEvent.KEYCODE_CTRL_RIGHT;
            case ALT:
                return KeyEvent.KEYCODE_ALT_RIGHT;
            case ENTER:
                return KeyEvent.KEYCODE_ENTER;
            case SHIFT:
                return KeyEvent.KEYCODE_SHIFT_RIGHT;
            case BACKSPACE:
                return KeyEvent.KEYCODE_DEL;
            default:
                return -1;
        }
    }

    public static int getAndroidButtonEventType(ButtonEventPayload.EventType type){
        switch (type) {
            case DOWN:
                return KeyEvent.ACTION_DOWN;
            case UP:
                return KeyEvent.ACTION_UP;
            default:
                return -1;
        }
    }
}
