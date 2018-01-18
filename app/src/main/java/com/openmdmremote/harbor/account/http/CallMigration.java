package com.openmdmremote.harbor.account.http;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.openmdmremote.harbor.account.http.client.ConnectionArguments;
import com.openmdmremote.harbor.account.http.client.HttpClient;
import com.openmdmremote.harbor.account.http.client.OnHttpResponseListener;
import com.openmdmremote.harbor.account.http.dto.DeviceRegResponse;
import com.openmdmremote.harbor.account.http.dto.MigrationRequest;
import com.openmdmremote.harbor.settings.HarborServerSettings;

import org.json.JSONException;


public class CallMigration implements OnHttpResponseListener {

    private final String url = HarborServerSettings.PATH_DEVICE_MIGRATION;
    private final HttpClient httpClient;

    private final String ERROR_INVALID_UUID = "Invalid uuid";
    private final String ERROR_INVALID_CRED = "Invalid remote credentials";

    private OnResultListener onResultListener;

    private Gson gson = new GsonBuilder().create();

    private String uuid;
    private String pwd;

    private final int PWD_LENGTH = 16;

    public CallMigration(Context context) {
        ConnectionArguments connectionArguments = new ConnectionArguments();
        httpClient = new HttpClient(this, connectionArguments, context);
    }

    @Override
    public void onServerError(int code, String error) {
        onResultListener.onError(error);
    }

    @Override
    public void onGenericAppError(String error) {
        onResultListener.onError(error);
    }

    @Override
    public void onAppErrorInJSON(String error) {

    }

    @Override
    public void onResult(String jsonResponse) {
        DeviceRegResponse msg = gson.fromJson(jsonResponse, DeviceRegResponse.class);

        uuid = msg.Uuid;
        if (uuid.length() != 36) {
            onResultListener.onError(ERROR_INVALID_UUID);
            return;
        }

        pwd = msg.Pwd;
        if (pwd.length() != PWD_LENGTH) {
            onResultListener.onError(ERROR_INVALID_CRED);
            return;
        }

        onResultListener.onSuccess(uuid, pwd);
    }

    public void callMigration(OnResultListener onResultListener, String deviceID) {
        this.onResultListener = onResultListener;
        try {
            MigrationRequest request = new MigrationRequest(deviceID);
            httpClient.postJsonData(url, request.toString());
        } catch (JSONException e) {
            onResultListener.onError(e.toString());
        }
    }

    public interface OnResultListener {
        void onError(String error);
        void onSuccess(String uuid, String remotePWD);
    }
}
