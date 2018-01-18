package com.openmdmremote.harbor.account.http;


import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.openmdmremote.WebkeyApplication;
import com.openmdmremote.harbor.account.http.client.ConnectionArguments;
import com.openmdmremote.harbor.account.http.client.HttpClient;
import com.openmdmremote.harbor.account.http.client.OnHttpResponseListener;
import com.openmdmremote.harbor.account.http.dto.CommonResponse;
import com.openmdmremote.harbor.account.http.dto.ForgotPasswordRequest;
import com.openmdmremote.harbor.settings.HarborServerSettings;

import org.json.JSONException;

public class ForgotPassword implements OnHttpResponseListener {

    private static final String LOGTAG = "ForgotPassword";

    private HttpClient httpClient;
    private final String url = HarborServerSettings.PATH_FORGOT_PASSWORD;;

    private OnForgotPasswordResult onForgotPasswordResult;

    private Gson gson = new GsonBuilder().create();

    public enum Results {
        OK,
        ERROR,
    }

    public ForgotPassword(Context context) {
        ConnectionArguments connectionArguments = new ConnectionArguments();
        httpClient = new HttpClient(this, connectionArguments, context);
    }

    @Override
    public void onResult(String response) {
        CommonResponse.OK resp = gson.fromJson(response, CommonResponse.OK.class);
        WebkeyApplication.log(LOGTAG, "Response: "+resp.Message);
        onForgotPasswordResult.onResult(Results.OK);
    }

    @Override
    public void onServerError(int code, String error) {
        WebkeyApplication.log(LOGTAG, "onServerError: " + error);
        onForgotPasswordResult.onResult(Results.ERROR);
    }

    @Override
    public void onGenericAppError(String error) {
        onForgotPasswordResult.onResult(Results.ERROR);
    }

    @Override
    public void onAppErrorInJSON(String error) {

    }

    /*
     * Do not start new request until return the previous.
     */
    public void sendRequest(OnForgotPasswordResult onForgotPasswordResult, String email) throws JSONException {
        this.onForgotPasswordResult = onForgotPasswordResult;
        ForgotPasswordRequest request = new ForgotPasswordRequest(email);
        httpClient.postJsonData(url, request.toString());
    }

    public interface OnForgotPasswordResult {
        void onResult(Results res);
    }
}