package com.openmdmremote.nativ.net.exceptions;

public class AlreadyConnectedException extends Exception {
    public AlreadyConnectedException() {
        super("Already connected to the native code");
    }
}
