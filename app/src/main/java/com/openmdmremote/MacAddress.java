package com.openmdmremote;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

import static android.content.Context.WIFI_SERVICE;

/**
 * Created by Daniel on 15/12/2017.
 */

public class MacAddress {


    public static String getMacAddress(Context context){ //desde android 6 en adelante te da falsa


            if (Build.VERSION.SDK_INT >= 23) {
                Log.i("macv6 :", getMacAddrV6());

                return getMacAddrV6();
            } else {

                WifiManager manager = (WifiManager) context.getSystemService(WIFI_SERVICE);
                WifiInfo info = manager.getConnectionInfo();
                String address = info.getMacAddress();
                if(address==null){
                    address= getMacAddrV6();
                    Log.i("mac :", address);

                    return address;
                }
                Log.i("mac :", address);
                return address;


            }



    }

    public static String getMacAddrV6() { // mac para dispositivos android 6 en adelante (nivel api 23)
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02x", b & 0xFF));
                    res1.append(":");
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "00000000";
    }

    public static String buildMacAsId(String mac){
        try {
            String[] array = mac.split("");
            StringBuilder builder = new StringBuilder();
            for (int i = 1; i < array.length; i++) {
                if (array[i].equals(":")) {
                    builder.append(array[i + 1]);
                    i = i + 1;
                } else {
                    builder.append(array[i]);
                }
            }
            Log.i("IdMAC", String.valueOf(builder));
            return String.valueOf(builder);
        }catch (IndexOutOfBoundsException e){e.printStackTrace();}catch (Exception e){e.printStackTrace();}
        return "MacIdError";
    }


}
