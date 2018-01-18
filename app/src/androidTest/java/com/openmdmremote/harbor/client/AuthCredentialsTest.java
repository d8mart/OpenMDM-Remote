package com.openmdmremote.harbor.client;

import org.junit.Test;

import static org.junit.Assert.*;

public class AuthCredentialsTest {
    @Test
    public void getSerial() throws Exception {
        AuthCredentials ac = new AuthCredentials();
        String serial = ac.getSerial();
        assertNotNull(serial);
    }

}