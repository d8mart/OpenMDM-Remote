package com.openmdmremote.nativ.binary;

import android.content.Context;
import android.util.Log;

import com.openmdmremote.WebkeyApplication;
import com.openmdmremote.service.handlers.ScreencapHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

class Binary {
    private final static String LOGTAG = "Binary";

    private final EnvironmentSettings envSettings;
    private String sessionKey;

    Binary(Context context) {
        this.envSettings = new EnvironmentSettings(context);
    }

    private int exec() throws IOException, InterruptedException {
        String shell = envSettings.getInterpreterPath();
        File runningDir = envSettings.getRunningDirectory();

        ProcessBuilder processBuilder = new ProcessBuilder(shell);
        processBuilder.directory(runningDir);
        processBuilder.redirectErrorStream(true);
        processBuilder.environment().put("LD_LIBRARY_PATH", envSettings.getLdLibraryPath());

        Process process = processBuilder.start();
        startCommandOutputReading(process);
        writeOutTheStartCommand(process);

        process.waitFor();
        return process.exitValue();
    }

    private void writeOutTheStartCommand(Process process) {
        PrintWriter pw = null;
        try {
            OutputStream out = process.getOutputStream();
            pw = new PrintWriter(new OutputStreamWriter(out));
            pw.write( getStartCommand() + "\n");
            pw.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (pw != null) {
                pw.close();
            }
        }
    }
    private void startCommandOutputReading(final Process proc) {
        (new Thread() {
            public void run() {
                BufferedReader bufferedReader = null;
                try {
                    bufferedReader = new BufferedReader(
                            new InputStreamReader(proc.getInputStream()));
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        WebkeyApplication.logWeb("Webkey-native", line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if(bufferedReader!=null) {
                            bufferedReader.close();
                        }
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void runBackend(final String sessionKey) {
        WebkeyApplication.log(LOGTAG, "Run backend");
        this.sessionKey = sessionKey;

        if (!envSettings.getUpdateFile().exists()) {
            WebkeyApplication.log(LOGTAG, "Could not find webkey_update binary!");
            return;
        }

        (new Thread() {
            public void run() {
                this.setName("Backend runner");
                int returnCode;
                try {

                    returnCode = exec();

                    if (returnCode == 0) {
                        WebkeyApplication.log(LOGTAG, "Shell exited well");
                        return;
                    }

                    WebkeyApplication.log(LOGTAG, "Shell exited with error: " + Integer.toString(returnCode));
                    ScreencapHandler.backRerunning=0;
                } catch (Exception e) {
                    WebkeyApplication.log(LOGTAG, "Start backend in root mode failed: \n" + e.toString());
                }
            }
        }).start();
    }

    private String getStartCommand() {
        String wk_bin_file_path = envSettings.getWKBinary().getAbsolutePath();
      //  Log.i("wk_bin_file_path",wk_bin_file_path);
        String wk_bin_update_file_path = envSettings.getUpdateFile().getAbsolutePath();
      //  Log.i("wk_bin_update_file_path",wk_bin_update_file_path);
        String lock_file_path = envSettings.getLockFile().getAbsolutePath();
       // Log.i("lock_file_path",lock_file_path);

        return "set -e; export LD_LIBRARY_PATH=" + envSettings.getLdLibraryPath() + "; " +
                "cp " + wk_bin_update_file_path + " " + wk_bin_file_path + "; " +
                wk_bin_file_path +
                " -s " + sessionKey +
                " -l " + lock_file_path +
                " -u " + wk_bin_update_file_path +
                "& " +
                "wait $!; echo The webkey binary exited: $?";
    }

    public static boolean isRunning() {
        Socket s = new Socket();
        try {
            s.bind(new InetSocketAddress("127.0.0.1", 8888));
            s.close();
            return false;
        } catch (IOException e) {
            return true;
        }
    }
}
