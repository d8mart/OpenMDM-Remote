package com.openmdmremote.debug;

import android.os.Environment;

import com.openmdmremote.WebkeyApplication;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ServiceLogger {
    File logfile = null;
    BufferedWriter buf = null;
    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    public ServiceLogger() {
        if(logfile != null) {
            return;
        }

        logfile = new File(Environment.getExternalStorageDirectory(),"webkey_service_"+System.currentTimeMillis()+".log");
        try
        {
            buf = new BufferedWriter(new FileWriter(logfile, true));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void log(String text) {
        try {
            Date date = new Date();
            WebkeyApplication.log("webkey-service", text);
            buf.append(dateFormat.format(date) + ": " + text);
            buf.newLine();
            buf.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {

        }
    }

    public void close() {
        try {
            Date date = new Date();
            buf.append(dateFormat.format(date) + ": " + "byebye");
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e){

        }
        buf = null;
        logfile = null;

    }
}
