package com.openmdmremote.harbor.account.http;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.openmdmremote.harbor.account.http.client.ConnectionArguments;
import com.openmdmremote.harbor.account.http.client.HttpClient;
import com.openmdmremote.harbor.account.http.client.OnHttpResponseListener;
import com.openmdmremote.harbor.account.http.dto.DeviceRegRequest;
import com.openmdmremote.harbor.account.http.dto.DeviceRegResponse;
import com.openmdmremote.harbor.settings.HarborServerSettings;

import org.json.JSONException;

public class CallDeviceReg implements OnHttpResponseListener {

    private final String url = HarborServerSettings.PATH_DEVICE_REGISTRATION;
    private final HttpClient httpClient;

    private final String ERROR_INVALID_UUID = "Invalid uuid";
    private final String ERROR_INVALID_CRED = "Invalid remote credentials";

    private OnResultListener resultListener;

    private Gson gson = new GsonBuilder().create();

    private final int PWD_LENGTH = 16;

    private String uuid;
    private String pwd;

    public CallDeviceReg(Context context) {
        ConnectionArguments connectionArguments = new ConnectionArguments();
        httpClient = new HttpClient(this, connectionArguments, context);
    }

    @Override
    public void onResult(String response) {
        DeviceRegResponse msg = gson.fromJson(response, DeviceRegResponse.class);

        uuid = msg.Uuid;
        if (uuid.length() != 36) {
            resultListener.onOtherError(ERROR_INVALID_UUID);
            return;
        }

        pwd = msg.Pwd;
        if (pwd.length() != PWD_LENGTH) {
            resultListener.onOtherError(ERROR_INVALID_CRED);
            return;
        }

        resultListener.onSuccess(uuid, pwd);
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

    /*
     * Do not start new request until return the previous.
     */
    public void sendCredentials(OnResultListener onResultListener, String deviceNick, String type, String androidid) {
        this.resultListener = onResultListener;
        try {
            DeviceRegRequest request = new DeviceRegRequest(deviceNick, type, androidid);
            httpClient.postJsonData(url, request.toString());
        } catch (JSONException e) {
            onResultListener.onOtherError(e.toString());
        }
    }

    public interface OnResultListener {
        void onNicInUsed(String response);
        void onOtherError(String error);
        void onSuccess(String uuid, String remotePWD);
    }
}