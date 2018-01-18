package com.openmdmremote.harbor.account.http;

import android.content.Context;

import com.openmdmremote.harbor.account.http.client.ConnectionArguments;
import com.openmdmremote.harbor.account.http.client.HttpClient;
import com.openmdmremote.harbor.account.http.client.OnHttpResponseListener;
import com.openmdmremote.harbor.account.http.dto.LogInRequest;
import com.openmdmremote.harbor.settings.HarborServerSettings;

import org.json.JSONException;

public class CallLogIn implements OnHttpResponseListener {
    private HttpClient httpClient;
    private String url = HarborServerSettings.PATH_LOGIN_ACCOUNT;

    private OnResultListener resultListener;

    public CallLogIn(Context context) {
        ConnectionArguments connectionArguments = new ConnectionArguments();
        httpClient = new HttpClient(this, connectionArguments, context);
    }

    @Override
    public void onServerError(int code, String error) {
        resultListener.onOtherError(error);
    }

    @Override
    public void onGenericAppError(String error) {
        resultListener.onWrongPwd(error);
    }

    @Override
    public void onAppErrorInJSON(String error) {

    }

    @Override
    public void onResult(String jsonResponse) {
        loginSuccess();
    }

    private void loginSuccess() {
        resultListener.onSuccess();
    }

    /*
     * Do not start new request until return the previous.
     */
    public void sendCredentials(OnResultListener onResultListener, String email, String pwd) {
        this.resultListener = onResultListener;

        try {
            LogInRequest request = new LogInRequest(email, pwd);
            httpClient.postJsonData(url, request.toString());
        } catch (JSONException e) {
            onResultListener.onOtherError(e.toString());
        }
    }

    public interface OnResultListener {
        void onWrongPwd(String response);
        void onOtherError(String error);
        void onSuccess();
    }
}
