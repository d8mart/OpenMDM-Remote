package com.openmdmremote.harbor.account.http;

import android.content.Context;

import com.openmdmremote.harbor.account.http.client.ConnectionArguments;
import com.openmdmremote.harbor.account.http.client.HttpClient;
import com.openmdmremote.harbor.account.http.client.OnHttpResponseListener;
import com.openmdmremote.harbor.account.http.dto.SignUpRequest;
import com.openmdmremote.harbor.settings.HarborServerSettings;

import org.json.JSONException;

public class CallSignUp implements OnHttpResponseListener {

    private final HttpClient httpClient;
    private final String url = HarborServerSettings.PATH_SIGNIN_ACCOUNT;

    private OnResultListener resultListener;

    public CallSignUp(Context context) {
        ConnectionArguments connectionArguments = new ConnectionArguments();
        httpClient = new HttpClient(this, connectionArguments, context);
    }

    @Override
    public void onServerError(int code, String error) {
        resultListener.onOtherError(error);
    }

    @Override
    public void onGenericAppError(String error) {
        resultListener.onNicInUsed(error);
    }

    @Override
    public void onAppErrorInJSON(String error) {

    }

    @Override
    public void onResult(String response) {
        resultListener.onSuccess();
    }

    /*
     * Do not start new request until return the previous.
     */
    public void sendCredentials(OnResultListener onResultListener, String email, String pwd) {
        this.resultListener = onResultListener;
        try {
            SignUpRequest request = new SignUpRequest(email, pwd);
            httpClient.postJsonData(url, request.toString());
        } catch (JSONException e) {
            onResultListener.onOtherError(e.toString());
        }
    }

    public interface OnResultListener {
        void onNicInUsed(String response);
        void onOtherError(String error);
        void onSuccess();
    }
}