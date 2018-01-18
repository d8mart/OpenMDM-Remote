package com.openmdmremote.service.services;

public class Credentials {
    private String username = "";
    private String password = "";

    public Credentials(String username, String password) {
        this.username = username;
        this.password = password;

    }

    public String getUserName() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
