package com.openmdmremote.harbor.account.http.client;

public interface OnHttpResponseListener {
    void onServerError(int code, String error);
    void onGenericAppError(String error);
    void onAppErrorInJSON(String error);
    void onResult(String json);
}
