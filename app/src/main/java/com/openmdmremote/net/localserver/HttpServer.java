package com.openmdmremote.net.localserver;

import android.content.Context;
import android.util.Log;

import com.openmdmremote.service.services.Settings;

import java.io.IOException;
import java.io.InputStream;

import fi.iki.elonen.NanoHTTPD;

public class HttpServer extends NanoHTTPD {
    private Context mContext;

    public HttpServer(int port, Context c) {
        super(null, port);
        this.mContext = c;
    }

    public static final String
            MIME_PLAINTEXT = "text/plain",
            MIME_HTML = "text/html",
            MIME_JS = "application/javascript",
            MIME_CSS = "text/css",
            MIME_PNG = "image/png",
            MIME_GIF = "image/gif",
            MIME_XICON = "image/x-icon",
            MIME_DEFAULT_BINARY = "application/octet-stream",
            MIME_XML = "text/xml",
            MIME_EOT = "application/vnd.ms-fontobject",
            MIME_SVG = "image/svg+xml",
            MIME_OTF = "application/x-font-otf",
            MIME_TTF = "application/x-font-ttf",
            MIME_WOFF = "application/x-font-woff";


    Response.Status HTTP_OK = Response.Status.OK;

    @Override
    public Response serve(IHTTPSession session) {
        InputStream mbuffer = null;

        try {
            String uri = session.getUri(); Log.i("URIhttpserver",uri);
            if(uri!=null){
                if(uri.contains("favicon.ico")) {
                    mbuffer = mContext.getAssets().open("webkit/favicon.ico");
                    return newChunkedResponse(HTTP_OK, MIME_XICON, mbuffer);
                }else if(uri.contains(".js")){
                    mbuffer = mContext.getAssets().open("webkit/"+uri.substring(1));
                    return newChunkedResponse(HTTP_OK, MIME_JS, mbuffer);
                } else if(uri.contains(".css")){
                    mbuffer = mContext.getAssets().open("webkit/"+uri.substring(1));
                    return newChunkedResponse(HTTP_OK, MIME_CSS, mbuffer);
                } else if(uri.contains(".png")){
                    mbuffer = mContext.getAssets().open("webkit/"+uri.substring(1));
                    // HTTP_OK = "200 OK" or HTTP_OK = Status.OK;(check comments)
                    return newChunkedResponse(HTTP_OK, MIME_GIF, mbuffer);
                } else if(uri.contains(".gif")){
                    mbuffer = mContext.getAssets().open("webkit/"+uri.substring(1));
                    // HTTP_OK = "200 OK" or HTTP_OK = Status.OK;(check comments)
                    return newChunkedResponse(HTTP_OK, MIME_DEFAULT_BINARY, mbuffer);
                } else if(uri.contains(".eot")){
                    mbuffer = mContext.getAssets().open("webkit/"+uri.substring(1));
                    // HTTP_OK = "200 OK" or HTTP_OK = Status.OK;(check comments)
                    return newChunkedResponse(HTTP_OK, MIME_EOT, mbuffer);
                } else if(uri.contains(".svg")){
                    mbuffer = mContext.getAssets().open("webkit/"+uri.substring(1));
                    // HTTP_OK = "200 OK" or HTTP_OK = Status.OK;(check comments)
                    return newChunkedResponse(HTTP_OK, MIME_SVG, mbuffer);
                } else if(uri.contains(".ttf")){
                    mbuffer = mContext.getAssets().open("webkit/"+uri.substring(1));
                    // HTTP_OK = "200 OK" or HTTP_OK = Status.OK;(check comments)
                    return newChunkedResponse(HTTP_OK, MIME_TTF, mbuffer);
                } else if(uri.contains(".otf")){
                    mbuffer = mContext.getAssets().open("webkit/"+uri.substring(1));
                    // HTTP_OK = "200 OK" or HTTP_OK = Status.OK;(check comments)
                    return newChunkedResponse(HTTP_OK, MIME_OTF, mbuffer);
                } else if(uri.contains(".woff")){
                    mbuffer = mContext.getAssets().open("webkit/"+uri.substring(1));
                    // HTTP_OK = "200 OK" or HTTP_OK = Status.OK;(check comments)
                    return newChunkedResponse(HTTP_OK, MIME_WOFF, mbuffer);
                } else if(uri.contains("start.sh")){
                    mbuffer = mContext.getAssets().open("webkit/start.sh");
                    // HTTP_OK = "200 OK" or HTTP_OK = Status.OK;(check comments)
                    return newChunkedResponse(HTTP_OK, MIME_WOFF, mbuffer);
                } else {
                    mbuffer = mContext.getAssets().open("webkit/index.html");
                    String data = convertStreamToString(mbuffer);
                    data = data.replace("var WS_PORT=\"\";","var WS_PORT=\":"+new Settings(mContext).getWSport()+"\";");
                    Log.i("DATAHTTPSERVER",String.valueOf(data));
                    return newFixedLengthResponse(HTTP_OK, MIME_HTML, data);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;

    }

    private String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}