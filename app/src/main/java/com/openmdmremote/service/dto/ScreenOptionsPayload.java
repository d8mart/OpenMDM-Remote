package com.openmdmremote.service.dto;

import android.view.Surface;

public class ScreenOptionsPayload extends Payload {

    public boolean hasnavbar = true;

    public enum Rotation {
            ROTATION_0, ROTATION_90, ROTATION_180, ROTATION_270;

        public static Rotation fromInteger(int x) {
            switch(x) {
                case Surface.ROTATION_0 :
                    return ROTATION_0;
                case Surface.ROTATION_90:
                    return ROTATION_90;
                case Surface.ROTATION_180:
                    return ROTATION_180;
                case Surface.ROTATION_270:
                    return ROTATION_270;
            }
            return null;
        }
    }

	public int screenX;
	public int screenY;
    public Rotation rotation;

	// set defaults
	public ScreenOptionsPayload() {
		screenX = 0;
		screenY = 0;
	}

    public ScreenOptionsPayload(int x, int y, int rotation, boolean navbar){
        screenX = x;
        screenY = y;
        this.rotation = Rotation.fromInteger(rotation);
        this.hasnavbar = navbar;

    }

	public boolean isValid(){
		return screenX > 100 && screenY > 100;
	}
	
	@Override
	public String toString() {
		return "x: " + screenX + ", y: " + screenY;
	}
}
