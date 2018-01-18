package com.openmdmremote.net.remoteserver;

import android.util.Log;

import com.openmdmremote.harbor.client.HarborClient;
import com.openmdmremote.harbor.Visitor;
import com.openmdmremote.net.visitor.VisitorChannel;
import com.openmdmremote.net.visitor.VisitorManager;
import com.openmdmremote.net.visitor.WebkeyVisitor;

public class MyVisitor extends Visitor implements VisitorChannel {

    WebkeyVisitor webkeyVisitor;

    public MyVisitor(String conntrackId, HarborClient harborClient, VisitorManager visitorManager) {
        super(conntrackId, harborClient);
        webkeyVisitor = new WebkeyVisitor(visitorManager, this);
        webkeyVisitor.setHarborClient(harborClient);
    }

    @Override
    public void onMessage(String msgjson) {
        Log.i("onMessage en MyVisitor","Recibido :"+msgjson);
        webkeyVisitor.onMessage(msgjson);
    }

    @Override
    public void onClose() {
        webkeyVisitor.onClose();
    }

    /*From here the visitor's functions; */
    @Override
    public void sendMessage(byte[] msg) {
        Log.i("MyVisitor","send bytes"); //Aqui envia a el server la imagen
        send(msg);
    }

    @Override
    public void sendMessage(String msg) {
        Log.i("MyVisitor","send string");
        send(msg);
    }
}