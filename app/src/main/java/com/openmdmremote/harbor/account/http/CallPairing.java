package com.openmdmremote.harbor.account.http;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.openmdmremote.harbor.account.http.client.ConnectionArguments;
import com.openmdmremote.harbor.account.http.client.HttpClient;
import com.openmdmremote.harbor.account.http.client.OnHttpResponseListener;
import com.openmdmremote.harbor.account.http.dto.Pairing;
import com.openmdmremote.harbor.settings.HarborServerSettings;

import org.json.JSONException;

public class CallPairing implements OnHttpResponseListener {

    private final static int ERR_RESP_CODE_WRONG_NICK = 1;
    private final static int ERR_RESP_CODE_WRONG_PIN  = 2;

    private final String ERROR_INVALID_UUID = "Invalid uuid";
    private final String ERROR_INVALID_TOKEN = "Invalid remote admin token";

    private final String url = HarborServerSettings.PATH_DEVICE_PAIRING;
    private final HttpClient httpClient;

    private Gson gson = new GsonBuilder().create();

    private CallPairing.OnResultListener onResultListener;

    private String uuid;
    private String pwd;

    public CallPairing(Context context) {
        ConnectionArguments connectionArguments = new ConnectionArguments();
        httpClient = new HttpClient(this, connectionArguments, context);
    }

    @Override
    public void onServerError(int code, String error) {
        onResultListener.onError(error);
    }

    @Override
    public void onGenericAppError(String error) {
        onResultListener.onNicInUsed(error);
    }

    @Override
    public void onAppErrorInJSON(String json) {
        Pairing.ErrorResponse msg = gson.fromJson(json, Pairing.ErrorResponse.class);
        switch(msg.ErrorCode) {
            case ERR_RESP_CODE_WRONG_NICK:
                onResultListener.onNicInUsed(msg.ErrorMessage);
                break;
            case ERR_RESP_CODE_WRONG_PIN:
                onResultListener.onWrongPINCode(msg.ErrorMessage);
                break;
        }
    }

    @Override
    public void onResult(String json) {
        Pairing.Response msg = gson.fromJson(json, Pairing.Response.class);

        uuid = msg.Uuid;
        if (uuid.length() != 36) {
            onResultListener.onError(ERROR_INVALID_UUID);
            return;
        }

        pwd = msg.Pwd;
        if (pwd.isEmpty()) {
            onResultListener.onError(ERROR_INVALID_TOKEN);
            return;
        }

        onResultListener.onSuccess(uuid, pwd);
    }

    public void pairing(CallPairing.OnResultListener onResultListener, String nick, String type, String aid, String pin) {
        this.onResultListener = onResultListener;
        try {
            Pairing.Request request = new Pairing().new Request(nick, type, aid, pin);
            httpClient.postJsonData(url, request.toString());
        } catch (JSONException e) {
            onResultListener.onError(e.toString());
        }
    }

    public interface OnResultListener {
        void onError(String error);
        void onSuccess(String uuid, String remotePWD);
        void onNicInUsed(String message);
        void onWrongPINCode(String message);
    }
}
