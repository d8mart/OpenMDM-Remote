package com.openmdmremote.harbor.account.http.dto;

public class CommonResponse {
    public class OK {
        public String Message = "";
    }

    public class Error {
        public boolean Error = true;
        public String Message = "";
    }
}
