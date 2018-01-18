package com.openmdmremote.logfender.logger;

import java.io.IOException;

public class AndroidLogger {

    private static AndroidLogger instance;

    private AsyncLoggingWorker loggingWorker;

    private AndroidLogger(String token, String host, int port, boolean secure) throws IOException {
        loggingWorker = new AsyncLoggingWorker(token, host, port, secure);
    }

    public static synchronized AndroidLogger createInstance(String token, String host, int port, boolean secure)
            throws IOException {
        instance = new AndroidLogger(token, host, port, secure);
        return instance;
    }

    public void log(String message) {
        loggingWorker.addLineToQueue(message);
    }

}
