package com.openmdmremote.harbor.account.http.client;

import android.content.Context;
import android.util.Log;

import com.franmontiel.persistentcookiejar.ClearableCookieJar;
import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.openmdmremote.WebkeyApplication;
import com.openmdmremote.harbor.account.http.dto.CommonResponse;
import com.openmdmremote.harbor.ssl.NullHostNameVerifier;
import com.openmdmremote.harbor.ssl.SSLUtile;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;


public class HttpClient {
    private static final String LOGTAG = "HTTPClient";
    private final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient client;
    private final OnHttpResponseListener onHttpResponseListener;
    private final ConnectionArguments args;
    private String prefix;
    private int port;
    private Request.Builder requestBuilder;
    private Gson gson = new GsonBuilder().create();


    private final Context context;

    public HttpClient(OnHttpResponseListener onHttpResponseListener, ConnectionArguments args, Context context) {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        this.context = context;
        this.args = args;
        this.onHttpResponseListener = onHttpResponseListener;

        setPrefix();
        setPort();
        setSessionHandler(clientBuilder);
        setSSL(clientBuilder);

        client = clientBuilder.build();
    }

    private void setSSL(OkHttpClient.Builder clientBuilder) {
        if (args.isSecure()) {
            try {
                clientBuilder.sslSocketFactory(SSLUtile.getSSLContext(context).getSocketFactory());
                clientBuilder.hostnameVerifier(new NullHostNameVerifier());
            } catch (InstantiationException e) {
                WebkeyApplication.log("HttpClient", "ssl settings error, " + e.toString());
            }
        }
    }

    private void setPort() {
        port = args.getPort();
    }

    private void setPrefix() {
        if (args.isSecure()) {
            prefix = "https://";
        } else {
            prefix = "http://";
        }
    }

    private void setURL(String path) {
        String address = prefix + args.getHost() + ":" + Integer.toString(port) + path;
        requestBuilder.url(address);
        Log.i("HttpClientTest",address);

    }

    private void setSessionHandler(OkHttpClient.Builder builder) {
        ClearableCookieJar cookieJar = new PersistentCookieJar(
                new SetCookieCache(),
                new SharedPrefsCookiePersistor(context));

        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        builder.cookieJar(cookieJar);
    }


    public void postJsonData(String path, String data) {
        RequestBody body = RequestBody.create(JSON, data);
        requestBuilder = new Request.Builder();

        Log.i("POSTJSONPATH",path);
        Log.i("POSTJSONDATA",data);


        setURL(path);
        requestBuilder.post(body);

        Request request = requestBuilder.build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                onHttpResponseListener.onServerError(0, e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) {
                if(response.isSuccessful()) {
                    onSuccess(response);
                    return;
                }

                if(response.code() == 400) {
                    try {
                        handleAppLevelError(response.body());
                    } catch (IOException e) {
                        onHttpResponseListener.onServerError(response.code(), "Could not parse msg: " + e.toString());
                    }
                } else {
                   onHttpResponseListener.onServerError(response.code(), "HTTP error: " + Integer.toString(response.code()));
                }
            }
        });
    }

    private void handleAppLevelError(ResponseBody body) throws IOException {
        String responseContent = body.string();
        try {
            onHttpResponseListener.onGenericAppError(parseJsonError(responseContent));
        } catch (JsonSyntaxException e) {
            onHttpResponseListener.onAppErrorInJSON(responseContent);
        }

    }

    private String parseJsonError(String response) throws JsonSyntaxException {
        CommonResponse.Error errorResponse = gson.fromJson(response, CommonResponse.Error.class);
        return errorResponse.Message;
    }

    private void onSuccess(Response response) {
        try {
            String jsonData = response.body().string();
            Log.i("ONSUCCESSHTTPCLI",jsonData);
            onHttpResponseListener.onResult(jsonData);
        } catch (IOException e) {
            onHttpResponseListener.onServerError(response.code(), e.toString());
        }
    }
}
