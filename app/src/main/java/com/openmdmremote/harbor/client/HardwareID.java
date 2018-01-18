package com.openmdmremote.harbor.client;

import android.os.Build;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

class HardwareID {
    private static final String LOGTAG = "HardwareID";

    private final List<byte[]> macs = getMacAddresses();

    public String getSerial() {
        String mac = getMacAddr();
        if (mac != null) {
            return mac;
        }

        String serial = getAndroidSerial();
        return serial;
    }

    private String getMacAddr() {
        if (macs.size() <= 0) {
            return null;
        }

        byte[] mac = getHighestMac();
        return getMacString(mac);
    }

    private String getMacString(byte[] mac) {
        StringBuilder res1 = new StringBuilder();

        for (byte b : mac) {
            res1.append(String.format("%02X", (0xFF & b)) + ":");
        }
        res1.deleteCharAt(res1.length() - 1);
        return res1.toString();
    }

    private byte[] getHighestMac() {
        byte[] highest = macs.get(0);

        for (byte[] m : macs) {
            int a = ByteBuffer.wrap(highest).getInt();
            int b = ByteBuffer.wrap(m).getInt();
            if (b > a) {
                highest = m;
            }
        }

        return highest;
    }

    private List<byte[]> getMacAddresses() {
        List<byte[]> macs = new LinkedList<>();

        try {
            List<NetworkInterface> all  = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                byte[] macBytes = nif.getHardwareAddress();
                if(macBytes != null) {
                    macs.add(macBytes);
                }
            }
        } catch (SocketException e) {

        }
        return macs;
    }

    private String getAndroidSerial() {
        String serial = Build.SERIAL;
        if(serial.toLowerCase().equals("unknown")) {
            return null;
        } else {
            return serial;
        }
    }
}
